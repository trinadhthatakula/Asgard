"""Compare retained Compose/JVM entry points against the pre-expansion compiled baseline."""
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
baseline = json.loads((root / "api/android-97903e9.json").read_text())
classpath = root / "asgard/build/classes/kotlin/android/main"
missing = []
count = 0
for name, expected in baseline.items():
    output = subprocess.check_output(["javap", "-classpath", str(classpath), "-public", "-s", name], text=True)
    actual = []
    method = None
    for line in output.splitlines():
        if line.strip().startswith("public static "):
            method = line.strip().split("(")[0].split()[-1]
        elif line.strip().startswith("descriptor:") and method:
            actual.append([method, line.strip().split(":", 1)[1].strip()])
            method = None
    for entry in expected:
        count += 1
        if entry not in actual:
            missing.append((name, entry))
if missing:
    raise SystemExit(f"Missing compiled entry points: {missing}")
print(f"Preserved all {count} baseline JVM entry points across {len(baseline)} modified API classes.")
