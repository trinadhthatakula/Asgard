import contextlib
import io
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import update_dependencies as updater


class DependencyUpdaterTest(unittest.TestCase):
    def test_shared_reference_uses_common_version_not_matching_latest(self):
        item = ('catalog', 'shared', '1.0', None,
                [(False, ('g', 'a')), (True, 'plugin')])
        with patch.object(updater, 'consumer_versions', side_effect=[
                {'1.0', '1.1', '1.2', '1.3'}, {'1.0', '1.1', '1.2'}]):
            self.assertEqual(updater.check_artifact_version(item, False),
                             ('catalog', 'shared', '1.0', '1.2', 'Success'))

    def test_qualifiers(self):
        for suffix, channel in {
            'alpha01': 'alpha', 'beta02': 'beta', 'rc1': 'rc',
            'milestone1': 'dev', 'Final': 'stable', 'GA': 'stable',
            'RELEASE': 'stable', 'stable': 'stable', '': 'stable',
            'foobar': 'other', 'archive': 'other', 'alphabet': 'other',
        }.items():
            with self.subTest(suffix=suffix):
                version = '1.0' + ('-' + suffix if suffix else '')
                self.assertEqual(updater.get_channel(version), channel)
                expected = {'alpha': 0, 'dev': 0, 'beta': 1, 'rc': 2,
                            'stable': 3, 'other': 2.5}[channel]
                self.assertEqual(updater.version_sort_key(version)[1], expected)
        self.assertLess(updater.version_sort_key('1.0-alpha02'),
                        updater.version_sort_key('1.0-alpha10'))

    def test_worker_argument_rejected_before_executor(self):
        for value in ('0', '-1', 'abc'):
            with self.subTest(value=value), patch('sys.argv', ['updater', '--max-workers', value]), \
                    patch.object(updater, 'ThreadPoolExecutor') as executor, \
                    contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit) as error:
                updater.main()
            self.assertEqual(error.exception.code, 2)
            executor.assert_not_called()
        self.assertEqual(updater.positive_int('10'), 10)

    def test_shared_reference_checks_all_libraries_and_plugins(self):
        catalog = '''[versions]
compose = "1.0"
[libraries]
runtime = { module = "compose:runtime", version.ref = "compose" }
foundation = { module = "compose:foundation", version.ref = "compose" }
ui = { module = "compose:ui", version.ref = "compose" }
[plugins]
compose = { id = "compose.plugin", version.ref = "compose" }
'''
        available = {
            (False, ('compose', 'runtime')): set(),
            (False, ('compose', 'foundation')): {'1.0', '1.1', '1.2'},
            (False, ('compose', 'ui')): {'1.0', '1.1'},
            (True, 'compose.plugin'): {'1.0', '1.1', '1.2'},
        }
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'libs.versions.toml'
            path.write_text(catalog)
            with patch('sys.argv', ['updater', '--file', str(path)]), \
                    patch.object(updater, 'consumer_versions', side_effect=lambda *key: available[key]) as lookup, \
                    contextlib.redirect_stdout(io.StringIO()):
                updater.main()
            self.assertIn('compose = "1.1"', path.read_text())
            self.assertEqual(lookup.call_count, 4)

    def test_missing_or_disjoint_metadata(self):
        item = ('catalog', 'shared', '1.0', None, [(False, ('g', 'a')), (False, ('g', 'b'))])
        for metadata, status in [([set(), set()], 'Not Found'),
                                 ([{'1.1'}, {'1.2'}], 'Success')]:
            with patch.object(updater, 'consumer_versions', side_effect=metadata):
                self.assertEqual(updater.check_artifact_version(item, False),
                                 ('catalog', 'shared', '1.0', '1.0', status))


class WrapperUpdaterTest(unittest.TestCase):
    checksum = 'a' * 64

    def properties(self, kind='bin', newline='\n'):
        return newline.join([
            '# keep this comment',
            f'distributionUrl=https\\://services.gradle.org/distributions/gradle-8.14.2-{kind}.zip',
            'validateDistributionUrl=true',
            'distributionSha256Sum=' + 'b' * 64, '',
        ])

    def responses(self, checksum=None):
        return [io.BytesIO(json.dumps([{'version': version} for version in
                ('8.14.2', '8.14.3', '9.0.0', '9.1.0-rc-1')]).encode()),
                io.BytesIO((checksum if checksum is not None else self.checksum).encode())]

    def test_bin_all_checksum_and_formatting(self):
        for kind in ('bin', 'all'):
            for newline in ('\n', '\r\n'):
                original = self.properties(kind, newline)
                with patch.object(updater.urllib.request, 'urlopen', side_effect=self.responses()) as lookup:
                    updated, report = updater.wrapper_update(original)
                self.assertEqual(updated, original.replace('8.14.2', '8.14.3').replace('b' * 64, self.checksum))
                self.assertIn('major 9.0.0 skipped', report)
                self.assertIn(f'gradle-8.14.3-{kind}.zip.sha256', lookup.call_args.args[0])

    def test_explicit_major_opt_in(self):
        with patch.object(updater.urllib.request, 'urlopen', side_effect=self.responses()):
            updated, report = updater.wrapper_update(self.properties(), allow_major=True)
        self.assertIn('gradle-9.0.0-bin.zip', updated)
        self.assertIn('[MAJOR]', report)

    def test_missing_checksum_added_and_unescaped_url_preserved(self):
        original = self.properties().replace('\\:', ':').replace('distributionSha256Sum=' + 'b' * 64 + '\n', '')
        with patch.object(updater.urllib.request, 'urlopen', side_effect=self.responses()):
            updated, _ = updater.wrapper_update(original)
        self.assertIn('distributionUrl=https://', updated)
        self.assertIn('distributionSha256Sum=' + self.checksum, updated)

    def test_custom_missing_duplicate_urls_rejected_without_network(self):
        for original in ('validateDistributionUrl=true\n',
                         self.properties().replace('services.gradle.org', 'example.com'),
                         self.properties() + self.properties()):
            with patch.object(updater.urllib.request, 'urlopen') as lookup, self.assertRaises(ValueError):
                updater.wrapper_update(original)
            lookup.assert_not_called()

    def test_dry_run_and_failed_checksum_leave_file_unchanged(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'gradle-wrapper.properties'
            original = self.properties().encode()
            path.write_bytes(original)
            with patch.object(updater.urllib.request, 'urlopen', side_effect=self.responses()), \
                    contextlib.redirect_stdout(io.StringIO()):
                updater.update_wrapper(path, dry_run=True)
            self.assertEqual(path.read_bytes(), original)
            for error_response in (io.BytesIO(b'bad checksum'), OSError('network failure')):
                responses = self.responses()
                responses[1] = error_response
                with patch.object(updater.urllib.request, 'urlopen', side_effect=responses), \
                        self.assertRaises((ValueError, OSError)):
                    updater.update_wrapper(path)
                self.assertEqual(path.read_bytes(), original)

    def test_atomic_file_update(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'gradle-wrapper.properties'
            path.write_text(self.properties())
            with patch.object(updater.urllib.request, 'urlopen', side_effect=self.responses()), \
                    contextlib.redirect_stdout(io.StringIO()):
                updater.update_wrapper(path)
            self.assertEqual(path.read_text(), self.properties().replace('8.14.2', '8.14.3').replace('b' * 64, self.checksum))
            self.assertEqual(list(Path(directory).iterdir()), [path])

    def test_no_update_does_not_fetch_checksum(self):
        response = io.BytesIO(b'[{"version": "8.14.2"}]')
        with patch.object(updater.urllib.request, 'urlopen', return_value=response) as lookup:
            updated, _ = updater.wrapper_update(self.properties())
        self.assertEqual(updated, self.properties())
        lookup.assert_called_once()


if __name__ == '__main__':
    unittest.main()
