#!/usr/bin/env python3
"""Check that the Asgard skill names each public top-level common API and local resource."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "asgard/src/commonMain/kotlin/com/valhalla/asgard"
GLASS_SOURCE = ROOT / "asgard-glass/src/commonMain/kotlin/com/valhalla/asgard/glass"
SKILL = ROOT / ".claude/skills/asgard-ui"

# Kotlin declarations in this library are top-level at column zero. This deliberately excludes
# private/internal declarations and nested implementation types; overloads share one API name.
ANNOTATIONS = r"(?:@[\w.]+(?:\([^\n]*?\))?[ \t]+)*"
MODIFIERS = r"(?:(?:public|inline|suspend|operator|infix|tailrec|external|expect|actual)[ \t]+)*"
DECLARATION = re.compile(
    rf"^{ANNOTATIONS}{MODIFIERS}(?:(?:data|sealed|enum|value)[ \t]+)?(?:class|interface|object|typealias)[ \t]+(\w+)"
    rf"|^{ANNOTATIONS}{MODIFIERS}fun[ \t]+(?:<[^>\n]+>[ \t]*)?(?:\w+\.)?(\w+)[ \t]*\("
    rf"|^{ANNOTATIONS}{MODIFIERS}(?:val|var)[ \t]+(?:\w+\.)?(\w+)",
    re.MULTILINE,
)
LOCAL_LINK = re.compile(r"\[[^\]]+\]\(([^)#]+)(?:#[^)]*)?\)")


def public_api_names(source: Path) -> set[str]:
    names: set[str] = set()
    for path in source.rglob("*.kt"):
        for match in DECLARATION.finditer(path.read_text(encoding="utf-8")):
            names.add(match.group(1) or match.group(2) or match.group(3))
    return names


def check_glass_scores(sources: tuple[Path, ...], guide: Path) -> tuple[int, list[str]]:
    # The public component composables follow Compose's capitalized function convention.
    components = {
        name for source in sources for path in source.rglob("*.kt")
        for match in DECLARATION.finditer(path.read_text(encoding="utf-8"))
        if (name := match.group(2)) and name[0].isupper()
    }
    rows = re.findall(r"^\| `(\w+)` \| [1-5]/5 \| [^|\n]+ \|$", guide.read_text(encoding="utf-8"), re.MULTILINE)
    issues = [f"Missing glass score for {name}" for name in sorted(components - set(rows))]
    issues.extend(f"Unknown glass component {name}" for name in sorted(set(rows) - components))
    issues.extend(f"Duplicate glass score for {name}" for name in sorted(set(rows)) if rows.count(name) > 1)
    return len(components), issues


def version_mismatch(properties: Path, skill_entry: Path) -> str | None:
    project = re.search(r"(?m)^VERSION_NAME\s*=\s*(\S+)\s*$", properties.read_text(encoding="utf-8"))
    skill = re.search(r'(?m)^  asgard-version:\s*["\']?([^"\'\s]+)["\']?\s*$', skill_entry.read_text(encoding="utf-8"))
    if not project or not skill:
        return "Missing VERSION_NAME or skill metadata.asgard-version."
    if project.group(1) != skill.group(1):
        return f"Skill targets {skill.group(1)}, but VERSION_NAME is {project.group(1)}."
    return None


def skill_gaps(source: Path, skill: Path, *other_sources: Path) -> tuple[list[str], list[str]]:
    markdown = list(skill.rglob("*.md"))
    text = "\n".join(path.read_text(encoding="utf-8") for path in markdown)
    names = public_api_names(source).union(*(public_api_names(path) for path in other_sources))
    missing = sorted(name for name in names if not re.search(rf"\b{re.escape(name)}\b", text))
    broken = []
    for path in markdown:
        for target in LOCAL_LINK.findall(path.read_text(encoding="utf-8")):
            if "://" not in target and not target.startswith("mailto:") and not (path.parent / target).exists():
                broken.append(f"{path.relative_to(skill)}: {target}")
    return missing, sorted(broken)


def main() -> int:
    names = public_api_names(SOURCE) | public_api_names(GLASS_SOURCE)
    if not names:
        print("No public APIs found; check the source path and declaration parser.")
        return 1
    missing, broken = skill_gaps(SOURCE, SKILL, GLASS_SOURCE)
    components, score_issues = check_glass_scores((SOURCE, GLASS_SOURCE), SKILL / "references/glass.md")
    version_error = version_mismatch(ROOT / "gradle.properties", SKILL / "SKILL.md")
    if missing:
        print("Public APIs missing from the skill:", ", ".join(missing))
    if broken:
        print("Broken skill resource links:", ", ".join(broken))
    if version_error:
        print(version_error)
    for issue in score_issues:
        print(issue)
    if missing or broken or version_error or score_issues:
        return 1
    print(f"Skill references all {len(names)} public top-level common API names and rates all {components} components. Version and local links match.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
