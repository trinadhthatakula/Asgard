#!/usr/bin/env python3
"""Copy the repository's Asgard skill to an agent skill directory, or check for drift."""

from __future__ import annotations

import argparse
import os
from pathlib import Path
import tempfile

SOURCE = Path(__file__).resolve().parents[1] / ".claude/skills/asgard-ui"


def differences(source: Path, destination: Path) -> list[Path]:
    files = sorted(path.relative_to(source) for path in source.rglob("*") if path.is_file())
    if Path("SKILL.md") not in files:
        raise ValueError(f"No SKILL.md found in {source}")
    return [relative for relative in files if not (destination / relative).is_file()
            or (source / relative).read_bytes() != (destination / relative).read_bytes()]


def synchronize(source: Path, destination: Path) -> list[Path]:
    changed = differences(source, destination)
    for relative in changed:
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(dir=target.parent, delete=False) as output:
                temporary = Path(output.name)
                output.write((source / relative).read_bytes())
            os.replace(temporary, target)
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)
    return changed


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--destination", type=Path, default=Path(os.environ.get("CODEX_HOME") or Path.home() / ".codex") / "skills/asgard-ui",
                        help="Installed skill directory (default: CODEX_HOME/skills/asgard-ui).")
    parser.add_argument("--check", action="store_true", help="Report drift without writing files.")
    args = parser.parse_args()
    changed = differences(SOURCE, args.destination) if args.check else synchronize(SOURCE, args.destination)
    if changed:
        label = "Out of date" if args.check else "Updated"
        for relative in changed:
            print(f"{label}: {relative}")
        return 1 if args.check else 0
    print("Repository-managed Asgard skill files match the installed copy.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
