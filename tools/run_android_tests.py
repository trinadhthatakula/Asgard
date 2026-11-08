"""Rerun Asgard's installed Android instrumentation APKs without invoking Gradle."""
import argparse
import re
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", help="adb device serial (required when multiple devices are connected)")
    parser.add_argument("--class", dest="test_class", help="optional fully qualified test class or class#method")
    args = parser.parse_args()
    command = ["adb"]
    if args.serial:
        command.extend(["-s", args.serial])
    command.extend(["shell", "am", "instrument", "-w", "-r"])
    if args.test_class:
        command.extend(["-e", "class", args.test_class])
    command.append("com.valhalla.asgard.app.test/androidx.test.runner.AndroidJUnitRunner")
    print("Running the already installed Asgard app and test APKs.", flush=True)
    result = subprocess.run(command, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    print(result.stdout, end="")
    # adb/am can return zero even when the Android test runner reports failures.
    passed = re.search(r"^OK \(\d+ tests?\)", result.stdout, re.MULTILINE)
    failed = re.search(r"FAILURES!!!|INSTRUMENTATION_FAILED|Process crashed|shortMsg=", result.stdout)
    return 0 if result.returncode == 0 and passed and not failed else 1


if __name__ == "__main__":
    sys.exit(main())
