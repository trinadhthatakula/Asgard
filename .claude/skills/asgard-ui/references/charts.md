# Charts (2.1.1)

Import from `com.valhalla.asgard.charts`. Supply localized formatters and accessibility text as
needed; choose theme colors at the call site and give charts bounded dimensions.

- `AsgardLineChart` draws `AsgardLineSeries` of `AsgardChartPoint`; `AsgardLineSmoothing` chooses
  straight or cubic lines. Provide `yValueFormatter` and optional x labels.
- `AsgardStackedBarChart` renders `AsgardBarStack` values with caller-supplied segment colors.
- `AsgardTimelineBar` renders `AsgardTimelineSegment` values within an explicit time window.
- `AsgardChartLegend` and `AsgardLegendItem` render `AsgardLegendEntry` with
  `AsgardLegendSwatch` choices. `AsgardPulseRing` is decorative, not a progress indicator.
- `AsgardCalendarHeatmap` uses `AsgardHeatmapEntry` and kotlinx-datetime `LocalDate` values.
  `asgardHeatmapCells` returns `AsgardHeatmapCell` values for an inclusive, week-padded range.
  Duplicates sum; missing differs from zero. Inputs must be finite and nonnegative, and ranges are
  limited to ten years. Interactive cells reserve at least 48 dp. Provide localized day/cell labels.

See the heatmap example in [ComponentExamples.kt](../examples/ComponentExamples.kt).
