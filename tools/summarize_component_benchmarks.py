#!/usr/bin/env python3
"""Summarize raw Window FrameMetrics; elapsed pipeline timings are not CPU profiling samples."""
import argparse
import csv
import json
import math
from pathlib import Path


def quantile(values, fraction):
    if not values:
        return None
    values = sorted(values)
    return values[min(len(values) - 1, max(0, math.ceil(len(values) * fraction) - 1))]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    rows = []
    for path in sorted(args.directory.glob("*.csv")):
        metadata = json.loads(path.with_suffix(".json").read_text())
        with path.open() as stream:
            frames = list(csv.DictReader(stream))
        if not frames:
            raise ValueError(f"No recorded frames in {path}")
        result = {"file": path.name, **metadata}
        for key in ["totalNs", "drawNs", "layoutNs", "gpuNs", "deadlineNs"]:
            values = [int(frame[key]) / 1_000_000 for frame in frames if int(frame[key]) >= 0]
            result[key.replace("Ns", "Ms")] = {
                f"p{percentile}": round(quantile(values, percentile / 100), 3) if values else None
                for percentile in [50, 90, 95, 99]
            }
        deadline_frames = [frame for frame in frames if int(frame["deadlineNs"]) > 0]
        overruns = [(int(frame["totalNs"]) - int(frame["deadlineNs"])) / 1_000_000 for frame in deadline_frames]
        result["overBudgetFrames"] = sum(overrun > 0 for overrun in overruns)
        result["overBudgetPercent"] = round(100 * result["overBudgetFrames"] / len(deadline_frames), 3) if deadline_frames else None
        result["overrunMs"] = {
            f"p{percentile}": round(quantile(overruns, percentile / 100), 3) if overruns else None
            for percentile in [50, 90, 95, 99]
        }
        result["cpuMsPerFrame"] = round(metadata["processCpuMs"] / len(frames), 3)
        result["cpuMsPerSecond"] = round(metadata["processCpuMs"] / metadata["measureSeconds"], 3)
        result["recordedFramesPerSecond"] = round(len(frames) / metadata["measureSeconds"], 3)
        rows.append(result)
    print(json.dumps(rows, indent=2))


if __name__ == "__main__":
    main()
