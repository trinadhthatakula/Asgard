# Component expansion API guide

These APIs are implemented on `feat/asgard-component-expansion` and are **not yet published**.
The existing library entry points are retained; configuration overloads require their new
configuration argument. The planned release remains 2.1.0 after validation and release review.

All layout components read defaults from the caller's MaterialTheme. Localized text, icons,
models, selection, routes, persistence, permissions and image loading remain caller-owned.
The calendar chart exposes kotlinx-datetime LocalDate through an API dependency.

## Connected-button shape behavior

Both the single- and multiselect overloads accept a required `shapes` argument:

```kotlin
ConnectedButtonGroup(
    items = items,
    selectedIndices = selected,
    onSelectionChanged = { selected = it },
    shapes = ConnectedButtonGroupShapes.FixedConnected,
)
```

- `Expressive`: Material state shapes, with standalone geometry for one item.
- `FixedConnected`: leading/middle/trailing base geometry stays fixed during selection/press.
- `Uniform(shape)`: a caller shape for every item/state.
- `Custom(leading, middle, trailing, standalone)`: caller ToggleButtonShapes for each position.

Colors are configured independently. Defaults emphasize selected items with primary/onPrimary.
Material handles pressed-state precedence and shape animation; arbitrary non-corner shapes
may switch rather than morph. Corner positions are logical and follow layout direction.

## Navigation variations

```kotlin
AsgardNavigationBar(
    items = destinations,
    selectedIndex = selected,
    onSelect = { selected = it },
    configuration = AsgardNavigationConfiguration(
        placement = AsgardNavigationPlacement.Floating,
        appearance = AsgardNavigationAppearance.Segmented,
        labels = AsgardNavigationLabels.Always,
        insets = AsgardNavigationInsets.None,
    ),
)
```

The same configuration overload exists for `AsgardNavigationRail`, with header/footer slots.

| Choice | Options |
|---|---|
| Placement | Docked, Floating |
| Destination appearance | Expanding, Segmented, Standard |
| Label policy | Selected, Always, Hidden, SelectedCaptionWhenNeeded |
| Icons | Visible or text-only |
| Insets | None, NavigationBars, SafeDrawing |

Container and indicator shapes, width, padding, floating margin, spacing and elevation are
independent. Colors and typography are passed to the bar/rail as usual. Floating describes the
surface treatment; the app places it in a Scaffold bottomBar or an overlay.

Always-visible inline labels stack under icons when their measured widths cannot fit. Automatic
selected labels fall back to a caption when necessary. Too many destinations for 48dp targets
produce horizontal scrolling. Hidden visual labels retain accessible names and selection state.
Empty bars render nothing; out-of-range selection highlights no destination. Text-only items
always retain visible text. Routes/backstack policy are never part of the library.

## Sheets and searchable pickers

`AsgardSheet` composes Material's modal sheet and supplies header/body/footer slots, caller
SheetState, shape/colors, maximum width/height and inset configuration. The body scrolls by
default; set `scrollBody = false` for bounded lazy content. The header/footer stay outside body
scrolling, and footer actions wrap. `AsgardActionSheet` supplies a title and action-footer layout.

```kotlin
val state = rememberBottomSheetState(
    SheetValue.Hidden,
    enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
)
val dismiss = rememberAsgardSheetDismiss(state) { visible = false }
AsgardActionSheet(
    onDismissRequest = { visible = false },
    title = title,
    sheetState = state,
    actions = { TextButton(onClick = dismiss) { Text(closeLabel) } },
) { Text(message) }
```

The dismiss helper hides before removing the sheet; a cancelled hide does not dismiss it.
The sheet owns its content insets unless callers supply zero insets. App-lock and workflow
policy stay in the host application.

`AsgardPicker` receives caller-filtered `AsgardPickerSection<T>` values, query and callbacks,
item keys/labels, selected predicate and optional row/leading content. Give the inline picker
bounded height. `AsgardPickerSheet` hosts the same bounded picker composition in a sheet.

```kotlin
AsgardPicker(
    sections = filteredSections,
    query = query,
    onQueryChange = { query = it },
    itemKey = { it.id },
    itemLabel = { it.label },
    isSelected = { it.id == selectedId },
    onSelect = { selectedId = it.id },
    modifier = Modifier.height(320.dp),
    searchPlaceholder = searchLabel,
    emptyLabel = emptyLabel,
    clearLabel = clearLabel,
)
```

Section IDs must be unique. Item keys must be unique within a section; the same item can
appear in recent/catalog sections because list keys are namespaced. Query changes reset list
scroll. Recent-history persistence, filtering/exclusion and dismissal on selection stay in apps.
Icons are optional; a text Clear action is supplied when no clear icon is passed.

## Action toolbar and avatar

`AsgardToolbarAction` has an ID, localized label, icon, callback, enabled/selected state and
optional destructive emphasis. `AsgardActionToolbar` renders compact, horizontally scrolling
actions with optional labels and a fixed leading slot for count/cancel content. Consumers
calculate privileges and action availability. Action IDs must be unique.

`AsgardAvatar` accepts initials or a fallback icon, size/shape/colors and optional image-content
and action slots. Image content overlays the fallback, supporting asynchronous image loading
without a library image-loader dependency. Null contentDescription is decorative; clickable
avatars require a nonblank accessible name. Child image/fallback semantics are decorative.

## Draggable list/grid scrollbar

```kotlin
val listState = rememberLazyListState()
Box(Modifier.height(320.dp)) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(end = 48.dp),
    ) { /* app rows */ }
    AsgardLazyScrollbar(
        state = rememberAsgardScrollbarState(listState),
        modifier = Modifier.align(Alignment.CenterEnd),
        label = scrollLabel,
        rangeLabel = { first, last -> localizedRange(first, last) },
    )
}
```

A corresponding adapter accepts LazyGridState. The core also accepts an AsgardScrollbarState
for custom adapters. The component supports vertical layouts, including reverse layouts.
Progress follows the physical top-to-bottom track: in a reversed list, progress zero reaches
the last item and progress one reaches the first. Boundary requests use actual item boundaries;
intermediate positions use the measured content estimate.

Reserve a **48dp end gutter** and bounded height. The overlay does not resize content. Visible
index labels are zero-based inputs to the caller's formatter. Geometry uses Compose's measured
content estimate, so variable item sizes/spans can adjust the thumb position and size.

Thumb dragging and accessibility progress actions share a conflated request queue. Arrow keys
move five percent, PageUp/PageDown use the viewport fraction, and Home/End move to boundaries.
Idle-hidden visibility still retains the keyboard target; focus reveals it. Empty/short content
has no scrollbar, and zero travel/non-finite progress requests are guarded.

This implementation uses Compose's public scroll-indicator/state APIs. Thor's GPL source file
was not relocated or relicensed.

## Calendar heatmap

```kotlin
AsgardCalendarHeatmap(
    entries = listOf(AsgardHeatmapEntry(LocalDate(2026, 11, 8), 2.0)),
    start = LocalDate(2026, 10, 12),
    endInclusive = LocalDate(2026, 11, 8),
    weekStart = DayOfWeek.MONDAY,
    selectedDate = selectedDate,
    onDateSelected = { selectedDate = it },
    weekLabel = { date -> formatWeek(date) },
    dayLabel = { day -> localizedDay(day) },
    cellLabel = { date, value -> accessibleDateValue(date, value) },
)
```

Ranges are inclusive and padded to complete weeks. Duplicate dates sum. Missing values stay
null, distinct from recorded zero. Values must be finite/nonnegative, and sums must stay finite;
invalid input fails explicitly. Out-of-range entries are not displayed. Ranges contain at most
3660 days. The scale defaults to the data maximum (at least one); explicit maxima must be positive
and finite, and values above the maximum use the highest intensity.

Week start, intensity levels, cell size/spacing, shapes, colors and localized date/day/week labels
are configurable. The grid scrolls horizontally instead of compressing cells. Interactive cells
reserve at least 48dp and support focus/keyboard activation; selected-cell details are caller-owned.
`asgardHeatmapCells` exposes the date/value mapping for custom renderers and domain tests.

## Development checks

```sh
./gradlew :asgard:compileAndroidMain :asgard:testAndroidHostTest \
  :asgard:compileKotlinWasmJs :demo:wasmJsBrowserDistribution
python3 tools/check_api_compatibility.py
python3 -m unittest -v test_update_dependencies.py
```

The Android target now compiles against SDK 37.2 to satisfy the resolved Compose AAR metadata.
On this machine Yarn discovers a pnpm packageManager in an ancestor directory; run the Gradle
command with `SKIP_YARN_COREPACK_CHECK=1` if that same ancestor conflict occurs. This is a
per-command environment override, not a change to the user's global package-manager setup.

The retained JVM descriptor check covers the six modified existing API classes at the audit
baseline. It does not replace an independently compiled published-version consumer test.
Physical Android/iOS browser and screen-reader checks must be recorded separately.

The [permanent Android gallery](android-gallery.md) shares these examples and provides native
instrumentation tests, including a separately compiled published-2.0.1 consumer fixture.
