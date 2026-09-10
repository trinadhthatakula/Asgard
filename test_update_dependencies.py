import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import update_dependencies as updater


class DependencyUpdaterTest(unittest.TestCase):
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


if __name__ == '__main__':
    unittest.main()
