# Approved scope for the next Asgard release

Date: 2026-11-08. The user approved **all six recommended new components**, navigation
variations, connected-button customization, correctness fixes and the responsive gallery
as one bundled release. Proposed version: **2.1.0**, provided existing public APIs remain
compatible. This document records requirements; unchecked items have remaining implementation or verification work. Current evidence is in
[the implementation record](implementation-validation.md).

The scrollbar and heatmap are required release deliverables. Their higher effort does not
reduce them to optional follow-ups. The extra recipe/future ideas in the cross-app audit
(for example a FAB-cutout bottom app bar and log pane) are separate from the six recommendations.

## Navigation variations

Placement, destination appearance and label policy are independent configuration choices.
Support combinations such as a floating container with segmented destinations.

- [x] Docked and floating containers, with caller-controlled outer shape, elevation, width,
  spacing, colors and an explicit inset-ownership contract.
- [x] Expanding pills, connected segments and standard icon-over-label destinations.
- [x] Icon/text and text-only segment content, retaining destination/tab semantics.
- [x] Selected-only, always-visible and hidden labels; selected-caption fallback when measured
  available width cannot accommodate the selected inline label and other touch targets.
- [x] Caller-controlled indicator shape independent of container shape and selection colors.
- [x] Shared naming, selected-state semantics, badges, focus and activation behavior for bar
  and rail. Hidden visual labels must retain accessible destination names.
- [x] Derive fit and segment geometry from actual item count, width and label measurements;
  handle empty, singleton and out-of-range selection explicitly.
- [ ] Preserve existing entry points/defaults and document representative compositions from
  Adhan, iScan, Tely, MedFile and lux without moving their route/backstack policy.

## Connected buttons and correctness

- [x] Single- and multiselect share configurable expressive, fixed-connected, uniform and
  custom state shapes. Shape behavior remains independent of color behavior.
- [x] Handle singleton geometry and logical leading/trailing corners in RTL.
- [x] Preserve existing compiled entry points as delegates; new configuration overloads must
  not introduce source-call ambiguity or silently replace old JVM/Compose signatures.
- [x] Disabled search also disables Clear; settings toggles expose one coherent interaction
  target; labeled sliders expose their label and formatted value in semantics.

## All six new components

### Sheet scaffold / action sheet

- [x] Shared modal surface with caller state, dismiss callback, width/shape/colors/insets and
  header/body/footer slots, composed from Material's sheet implementation.
- [x] A bounded scrollable body leaves footer actions reachable in short windows, landscape
  and with the IME visible. Actions wrap when necessary.
- [ ] Animated dismissal and inset ownership are documented and verified. App-lock, purchase
  and permission policy remain in consumers.

### Searchable picker / picker sheet

- [x] Inline picker and sheet composition using the shared scaffold.
- [x] Caller-owned query, filtered items/sections, stable keys, labels, row content and selection
  callback. Recent-history storage, domain filters and locale discovery stay caller-owned.
- [ ] Search/Clear, empty results, selected rows, section headings and query scroll-reset behavior
  remain usable with keyboard, screen-reader semantics and constrained windows.
- [x] Avoid duplicate lazy-list keys when the same domain item appears in multiple sections.

### Action / selection toolbar

- [x] Shared compact renderer for icon/label actions, enabled/selected state, destructive
  emphasis, callbacks and caller-controlled surface style.
- [x] Optional leading selection count/cancel content supports contextual selection workflows.
- [x] Narrow layouts keep every action reachable and named; app privileges and action
  availability are calculated outside Asgard.

### Avatar

- [x] Caller-supplied image content with initials/icon fallback, configurable size/shape/colors
  and optional caller-owned action.
- [x] Explicit decorative versus named semantics and coherent click behavior.
- [x] No required image-loader dependency, contact/account models, URI permissions or video
  lifecycle management. Demonstrate fallback and image-content usage.

### Draggable lazy scrollbar

- [x] Portable commonMain implementation with list and grid state adapters, track/thumb styling,
  visibility policy, range text and localized accessibility labels supplied by the caller.
- [x] Document gutter reservation and physical/logical track placement. Reconcile scroll position
  and thumb size with visible items, variable item sizes and grid spans.
- [ ] Handle empty/short lists, first/last positions, content changes and zero travel without
  division-by-zero, NaN values, out-of-range item targets or a stale drag state.
- [ ] Dragging, keyboard movement and semantics actions work on Android and wasm.
- [x] Implement the reusable interaction pattern in Asgard without relocating or relicensing
  Thor's GPL source file.

### Calendar heatmap

- [x] Actual date/value input with an explicit inclusive range and a documented date type;
  aggregate duplicate dates under a defined policy and distinguish missing values from zero.
- [x] Configurable week start, intensity scale, cell geometry, colors and caller-provided labels.
- [x] Correct mapping across partial weeks, month/year boundaries and leap days. Clamp or reject
  invalid/non-finite values under a documented policy rather than producing misleading colors.
- [x] Constrained-width layout and optional scrolling preserve cell access; selected-cell details,
  keyboard interaction and accessible date/value descriptions are defined.
- [x] MindLoom example uses real data. No deterministic placeholder pattern stands in for analytics.

## Gallery and documentation

- [x] Full-width detail view on compact windows, searchable component navigation and separate
  appearance controls; a sidebar only when sufficient detail width remains.
- [x] Working examples for all six additions, all navigation appearances/placements/label policies,
  connected-button shape modes, list/grid scrolling and heatmap range/data changes.
- [x] Preview controls and usage snippets agree. Static minimal examples are labeled honestly.
- [x] Stable entry IDs, shareable URLs, browser-back behavior, predictable scroll reset and
  visible keyboard focus; a bounded selectable code pane and reachable Copy action.
- [x] Clipboard success is shown only after completion; failure has a usable recovery state.
- [x] Distinct theme chart colors, named controls and demonstrated version/install coordinates.
- [x] Public API reference, integration guidance and draft release notes cover the complete scope.

## Validation and release gate

- [x] Android and wasm library/demo compilation; existing host and tooling tests.
- [x] Meaningful tests for picker selection/key behavior, heatmap date/value mapping and scrollbar
  range calculations, plus focused navigation/button/disabled-input interaction coverage.
- [ ] Source and compiled-consumer compatibility fixtures for retained APIs.
- [ ] Production wasm bundle and browser checks covering 320/360/390/768/1440 widths, light/dark,
  larger text, RTL, keyboard and touch. Record physical-device/Safari checks separately and
  claim only the coverage actually exercised.
- [x] Representative consumer fixtures demonstrate app-owned models, actions and image content
  without introducing Android-only APIs or domain dependencies into commonMain.
- [ ] All scoped components, fixes and examples are complete before preparing the version bump
  and release PR to Asgard's main branch. Verify publication before updating Thor's dependency.

Implementation order: correctness and API compatibility; connected-button/navigation options;
sheet then picker; toolbar/avatar; scrollbar/heatmap; responsive gallery and all examples;
integration validation, documentation and release preparation.
