"""Verify synchronization detects drift and preserves unrelated installed files."""

from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from tools.sync_asgard_skill import differences, synchronize


class SkillSyncTest(unittest.TestCase):
    def test_copies_changes_and_preserves_unrelated_files(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            source, installed = root / "source", root / "installed"
            (source / "references").mkdir(parents=True)
            installed.mkdir()
            (source / "SKILL.md").write_text("new", encoding="utf-8")
            (source / "references/guide.md").write_text("guide", encoding="utf-8")
            (installed / "SKILL.md").write_text("old", encoding="utf-8")
            (installed / "personal.txt").write_text("keep", encoding="utf-8")
            expected = [Path("SKILL.md"), Path("references/guide.md")]
            self.assertEqual(expected, differences(source, installed))
            self.assertEqual("old", (installed / "SKILL.md").read_text(encoding="utf-8"))
            self.assertEqual(expected, synchronize(source, installed))
            self.assertEqual([], differences(source, installed))
            self.assertEqual("keep", (installed / "personal.txt").read_text(encoding="utf-8"))

    def test_refuses_to_sync_a_missing_skill(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            root.joinpath("source").mkdir()
            with self.assertRaises(ValueError):
                differences(root / "source", root / "installed")


if __name__ == "__main__":
    unittest.main()
