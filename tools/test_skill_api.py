"""Exercise public API discovery and skill-resource drift detection."""

from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from tools.check_skill_api import public_api_names, skill_gaps, version_mismatch


class SkillApiTest(unittest.TestCase):
    def test_discovers_public_declarations_and_detects_missing_names_and_links(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            skill = root / "skill"
            source.mkdir()
            skill.mkdir()
            (source / "Example.kt").write_text(
                "fun AsgardNew() = Unit\n"
                "fun <T> AsgardPicker(value: T) = value\n"
                "fun Modifier.expressivePress() = this\n"
                "@Composable public fun AsgardAnnotated() = Unit\n"
                "data class AsgardItem(val id: String)\n"
                "internal fun AsgardInternal() = Unit\n"
                "private fun AsgardPrivate() = Unit\n"
                "    fun AsgardNested() = Unit\n",
                encoding="utf-8",
            )
            (skill / "SKILL.md").write_text(
                "AsgardNew AsgardPicker expressivePress AsgardAnnotated [guide](references/guide.md)\n",
                encoding="utf-8",
            )
            self.assertEqual(
                {"AsgardNew", "AsgardPicker", "expressivePress", "AsgardAnnotated", "AsgardItem"},
                public_api_names(source),
            )
            self.assertEqual((["AsgardItem"], ["SKILL.md: references/guide.md"]), skill_gaps(source, skill))
            (skill / "references").mkdir()
            (skill / "references/guide.md").write_text("AsgardItem\n", encoding="utf-8")
            self.assertEqual(([], []), skill_gaps(source, skill))

    def test_detects_version_drift(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            properties = root / "gradle.properties"
            skill = root / "SKILL.md"
            properties.write_text("VERSION_NAME=2.2.0\n", encoding="utf-8")
            skill.write_text('metadata:\n  asgard-version: "2.1.1"\n', encoding="utf-8")
            self.assertIn("2.2.0", version_mismatch(properties, skill))
            skill.write_text('metadata:\n  asgard-version: "2.2.0"\n', encoding="utf-8")
            self.assertIsNone(version_mismatch(properties, skill))


if __name__ == "__main__":
    unittest.main()
