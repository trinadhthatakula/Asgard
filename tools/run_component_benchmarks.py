#!/usr/bin/env python3
"""Replay warmed release-harness workloads, retaining raw platform timings and device state."""
import argparse
import hashlib
import json
import re
import subprocess
import time
from pathlib import Path

PACKAGE = "com.valhalla.asgard.app.benchmark"
SCENARIOS = ["progress", "shimmer", "charts", "navigation", "scrollbar", "numerals", "press", "connected", "pulse"]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--apk", type=Path, required=True)
    parser.add_argument("--label", required=True)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--scenarios", nargs="+", choices=SCENARIOS, default=SCENARIOS)
    parser.add_argument("--repeats", type=int, default=3)
    parser.add_argument("--seconds", type=int, default=12)
    args = parser.parse_args()
    if args.repeats < 1 or not 3 <= args.seconds <= 30:
        parser.error("Use positive repeats and 3..30 measurement seconds")
    if re.fullmatch(r"[A-Za-z0-9_-]+", args.label) is None:
        parser.error("Use only letters, numbers, underscores or hyphens in --label")
    args.out.mkdir(parents=True, exist_ok=True)
    adb = ["adb", "-s", args.serial]

    def call(*command):
        return subprocess.check_output(adb + list(command), text=True)

    call("install", "-r", str(args.apk))
    call("shell", "cmd", "package", "compile", "-m", "speed", "-f", PACKAGE)
    digest = hashlib.sha256(args.apk.read_bytes()).hexdigest()
    for repeat in range(1, args.repeats + 1):
        for scenario in args.scenarios:
            run = f"{args.label}-{repeat}"
            stem = f"{run}-{scenario}"
            before = {
                "apkSha256": digest,
                "thermal": call("shell", "dumpsys", "thermalservice"),
                "battery": call("shell", "dumpsys", "battery"),
                "display": call("shell", "dumpsys", "display"),
            }
            call("shell", "am", "force-stop", PACKAGE)
            call("shell", "rm", "-f", f"/sdcard/Android/data/{PACKAGE}/files/benchmarks/{stem}.json")
            call("shell", "am", "start", "-W", "-n", f"{PACKAGE}/com.valhalla.asgard.app.ComponentBenchmarkActivity",
                 "--es", "scenario", scenario, "--es", "run", run, "--ei", "seconds", str(args.seconds))
            remote = f"/sdcard/Android/data/{PACKAGE}/files/benchmarks/{stem}"
            deadline = time.monotonic() + args.seconds + 30
            while True:
                result = subprocess.run(adb + ["shell", "test", "-f", remote + ".json"], stdout=subprocess.DEVNULL)
                if result.returncode == 0:
                    break
                if time.monotonic() > deadline:
                    raise RuntimeError(f"Benchmark did not finish: {stem}. Check device lock state and logcat AsgardBenchmark.")
                time.sleep(1)
            for suffix in [".csv", ".json"]:
                call("pull", remote + suffix, str(args.out / (stem + suffix)))
            metadata = json.loads((args.out / (stem + ".json")).read_text())
            if metadata["debuggable"] or metadata["frames"] < 30:
                raise RuntimeError(f"Invalid release measurement: {metadata}")
            before["thermalAfter"] = call("shell", "dumpsys", "thermalservice")
            (args.out / (stem + "-device.json")).write_text(json.dumps(before, indent=2))
            print(f"{stem}: {metadata['frames']} frames, CPU {metadata['processCpuMs']} ms", flush=True)


if __name__ == "__main__":
    main()
