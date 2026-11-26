"""Render the gallery's static LLM reference from the maintained skill and preview catalog."""
import argparse
import itertools
import json
from pathlib import Path
import re
import textwrap
from urllib.parse import urljoin

ROOT = Path(__file__).resolve().parents[1]
SKILL = ROOT / ".claude/skills/asgard-ui"
DEMO = ROOT / "demo/src/commonMain/kotlin/com/valhalla/asgard/demo"
OUTPUT = ROOT / "demo/src/wasmJsMain/resources/llms-full.txt"
SITE = "https://trinadhthatakula.github.io/Asgard/"
RAW = "https://raw.githubusercontent.com/trinadhthatakula/Asgard/main/"

# The first four ComponentEntry arguments are literal name, category, description and snippet.
STRING = r'("""[\s\S]*?"""\.trimIndent\(\)|"(?:\\.|[^"\\])*")'
ENTRY = re.compile(r"ComponentEntry\(\s*" + r"\s*,\s*".join([STRING] * 4) + r"\s*[,)]")


def decode_literal(value):
    if value.startswith('"""'):
        return textwrap.dedent(value[3:-len('""".trimIndent()')]).strip("\n")
    return json.loads(value.replace(r"\$", "$"))


def gallery_entries():
    entries = []
    for name in ("AsgardCatalog.kt", "ExpandedCatalog.kt", "GlassSurfaceDemo.kt"):
        source = (DEMO / name).read_text()
        for start in re.finditer(r"\bComponentEntry\(", source):
            match = ENTRY.match(source, start.start())
            if not match:
                raise ValueError(f"Unsupported catalog entry in {name} at offset {start.start()}")
            entries.append(tuple(map(decode_literal, match.groups())))
    return sorted(entries, key=lambda entry: (entry[1], entry[0]))


def render():
    blocks = ["# Asgard Gallery — full agent reference",
              "Use the `asgard-ui` skill for Asgard integration. Read the maintained skill at "
              + RAW + ".claude/skills/asgard-ui/SKILL.md and check the consuming project's pinned version."]
    for path in [SKILL / "SKILL.md", *sorted((SKILL / "references").glob("*.md"))]:
        url = RAW + path.relative_to(ROOT).as_posix()
        markdown = path.read_text().removeprefix("---\n")
        if path.name == "SKILL.md":
            markdown = markdown.split("\n---\n", 1)[1]
        # Relative recipe links in the standalone text resolve to the original source.
        markdown = re.sub(r"\]\(([^)\s]+)\)", lambda m: "](" + urljoin(url, m[1]) + ")", markdown)
        blocks.append(markdown.strip())
    for path in sorted((SKILL / "examples").glob("*.kt")):
        blocks.extend([f"## {path.stem}", f"```kotlin\n{path.read_text().strip()}\n```"])
    entries = gallery_entries()
    blocks.append(f"# Gallery catalog ({len(entries)} examples)")
    for category, group in itertools.groupby(entries, key=lambda entry: entry[1]):
        blocks.append(f"## {category}")
        for name, _, description, snippet in group:
            slug = re.sub("[^a-z0-9]+", "-", name.lower()).strip("-")
            blocks.extend([f"### [{name}]({SITE}#{slug})", description, f"```kotlin\n{snippet}\n```"])
    return "\n\n".join(blocks) + "\n"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    reference = render()
    if args.check:
        if OUTPUT.read_text() != reference:
            parser.exit(1, "Regenerate llms-full.txt with python3 tools/update_gallery_reference.py\n")
    else:
        OUTPUT.write_text(reference)
