# Component index and contracts (2.1.1)

Import from `com.valhalla.asgard.components`, except navigation and charts in their respective
packages. The source KDoc has complete parameters; these descriptions help choose an API.

## Structure and content

- `AsgardHeader`: screen heading with optional back navigation, icon, leading and actions slots.
- `AsgardSectionCard`: groups content under a title.
- `AsgardListRow`: title/subtitle/caption with optional icon, click and trailing content.
- `AsgardFeatureRow`: onboarding feature icon and explanatory text.
- `AsgardOnboardingScaffold`: onboarding page with title, content and actions.
- `AsgardAvatar`: caller-supplied image slot over initials or an icon fallback. A clickable avatar
  requires a nonblank `contentDescription`; a null description is decorative. No image loader is
  bundled. Size must be positive.

## Inputs and actions

- `ConnectedButtonGroup`, `ConnectedButtonGroupItem` and `ConnectedButtonGroupShapes`: single- or
  multiselect connected controls; see [navigation](navigation.md).
- `AsgardSearchBar`, `AsgardLabeledSlider`, `AsgardStepperRow`: caller-controlled inputs.
- `AsgardTonalIconButton` and `AsgardActionItem`: icon button and labeled action tile.
- `AsgardActionToolbar` displays `AsgardToolbarAction` items with an optional fixed leading slot.
  Action IDs must be unique. The host supplies localized labels, availability and permissions.
- `AsgardPicker` renders caller-filtered `AsgardPickerSection` items with query, selection and
  callbacks controlled by the host. Give it bounded height and unique section IDs and item keys
  within each section. Items can recur in different sections. The app owns filtering, history and
  whether selection dismisses it. `AsgardPickerSheet` hosts a picker in a modal sheet.

## Sheets and lists

- `AsgardSheet` offers bounded modal header/body/footer slots; `AsgardActionSheet` provides a title
  and wrapping action footer. Sheet visibility and `SheetState` belong to the caller. Body scrolling
  is on by default; set `scrollBody = false` for a bounded lazy list. The sheet owns its content
  insets unless the host passes zero. `rememberAsgardSheetDismiss` hides before invoking removal;
  a cancelled hide does not call the callback.
- `AsgardLazyScrollbar` overlays a 48 dp-wide target beside a vertical lazy list or grid. Use
  `rememberAsgardScrollbarState` for `LazyListState` or `LazyGridState`, or implement
  `AsgardScrollbarState` and `AsgardScrollbarMetrics`. Reserve the end gutter in the content;
  the scrollbar does not resize the list. `AsgardScrollbarVisibility` selects always-visible or
  while-scrolling behavior. Localize its label and visible range.

## Data, feedback and settings

- `AsgardStatCard`, `AsgardStatTile` and `AsgardAnimatedNumeral`: large and compact metrics or
  animated numbers. `AsgardStatCard` uses `iconTint` in 2.x, not the 1.x `iconColor`.
- `AsgardBadge` and `StatusChip`: small filled/outlined or status labels.
- `AsgardProgressRing`: progress gauge with a centered content slot.
- `AsgardBanner`, `AsgardEmptyState`, `AsgardLoadingState`, `AsgardShimmer` and
  `AsgardDialogScaffold`: feedback, empty/loading, skeleton and confirmation UI.
- `AsgardSettingRow` and `AsgardSettingToggleRow`: preference rows with caller-controlled values.
- `AsgardProBadge`, `AsgardUpgradeCard` and `AsgardLockedOverlay`: optional gating presentation;
  the host owns entitlement and purchase decisions. A locked overlay hides underlying semantics
  and intercepts input; blur may not be available on all platforms.

`AsgardDefaults` resolves shared defaults from the theme. `Modifier.expressivePress` and
`Modifier.animateExpressiveResize` expose motion helpers. See [component examples](../examples/ComponentExamples.kt)
for complete recipes; inspect the corresponding version's source for all optional parameters.
