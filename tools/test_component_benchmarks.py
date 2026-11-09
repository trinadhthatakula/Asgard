"""Platform-metric availability and unsafe-label regressions for the benchmark tools."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


TOOLS = Path(__file__).resolve().parent


class ComponentBenchmarkToolsTest(unittest.TestCase):
    def summarize(self, directory):
        return subprocess.run([sys.executable, str(TOOLS / "summarize_component_benchmarks.py"), str(directory)],
                              text=True, capture_output=True)

    def test_unsupported_gpu_and_missing_deadlines_do_not_distort_summary(self):
        with tempfile.TemporaryDirectory() as directory:
            directory = Path(directory)
            (directory / "sample.json").write_text(json.dumps({"frames": 3, "processCpuMs": 9, "measureSeconds": 3}))
            (directory / "sample.csv").write_text(
                "totalNs,drawNs,layoutNs,gpuNs,deadlineNs\n"
                "12000000,1000000,0,-1,10000000\n"
                "8000000,1000000,0,-1,10000000\n"
                "20000000,1000000,0,-1,-1\n")
            result = self.summarize(directory)
            self.assertEqual(0, result.returncode, result.stderr)
            summary = json.loads(result.stdout)[0]
            self.assertIsNone(summary["gpuMs"]["p50"])
            self.assertEqual(1, summary["overBudgetFrames"])
            self.assertEqual(50, summary["overBudgetPercent"])
            self.assertEqual(2, summary["overrunMs"]["p95"])

    def test_empty_frame_file_fails_with_an_explanation(self):
        with tempfile.TemporaryDirectory() as directory:
            directory = Path(directory)
            (directory / "empty.json").write_text("{}")
            (directory / "empty.csv").write_text("totalNs,drawNs,layoutNs,gpuNs,deadlineNs\n")
            result = self.summarize(directory)
            self.assertNotEqual(0, result.returncode)
            self.assertIn("No recorded frames", result.stderr)

    def test_label_is_rejected_before_device_or_filesystem_operations(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "must-not-be-created"
            result = subprocess.run([sys.executable, str(TOOLS / "run_component_benchmarks.py"),
                "--serial", "unused", "--apk", "missing.apk", "--label", "unsafe label;", "--out", str(output)],
                text=True, capture_output=True)
            self.assertEqual(2, result.returncode)
            self.assertIn("--label", result.stderr)
            self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
