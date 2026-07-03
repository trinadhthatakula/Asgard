# Asgard Component Backlog — Cross-App UI Audit

_A survey of reusable, theme-independent Compose UI across the developer's apps under
`~/StudioProjects`, identifying components worth promoting into **Asgard**
(`com.valhalla.asgard`), the shared, theme-agnostic component library._

**Audit date:** 2026-07-04
**Coverage:** 23 of the developer's own Compose apps (third-party forks `Seal`/`com.junkfood`
and `Android-Native-Root-Detector`/`com.reveny`, plus games, excluded).
**Status:** Tier-1 + Tier-2 components extracted into the library (PR #2); app migration still pending.

---

## Asgard fit criteria

A candidate must satisfy **all** of these:

1. **Theme-independent.** Uses `MaterialTheme.colorScheme` / `typography` / `shapes` for its
   defaults, and exposes any colors/text-styles/dimensions as **parameters that default to
   `MaterialTheme`** — e.g. `containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh`.
   No hardcoded `Color(0x…)`, no app-specific theme/Color objects. It inherits whatever app it
   is dropped into (this is how the existing `StatusChip`, `AsgardActionItem`, etc. are written).
2. **Reusable / generic.** A UI primitive or pattern (row, card, chip, dialog scaffold,
   state view, slider, gauge, tile, banner …) — not a one-off screen or feature section.
3. **Low app-coupling.** No dependency on the app's ViewModels, domain models, navigation, DI,
   or repositories. Pure inputs (data + lambdas + modifiers) → UI. If it currently takes an app
   model/enum, note the small change needed to generify it.
4. **Not already in Asgard.**

### Already in Asgard (do not re-create)

`AsgardNavigationBar`, `AsgardNavigationRail`, `AsgardNavItem`, `ConnectedButtonGroup`,
`StatusChip`, `AsgardHeader`, `AsgardActionItem`, `Motion` (animation specs).

> Note: `ConnectedButtonGroup` is already copy-pasted into **Valy** and the `StatusChip`/nav-bar
> patterns are re-implemented across many apps — confirmation that shared promotion pays off.

---

## Tier 1 — promote first

Strongest cross-app recurrence, theme-safe, clean extraction.

### 1. `AsgardStatTile` — compact metric tile
Small (often uppercase) label + large emphasized value on a rounded tonal surface; optional
leading icon and status dot.
- **Found in (~7):** Thor (`StatCard`/`StatItem`), Aqua (`StatItem`, `WeatherStatItem`),
  PowerMonitor (`StatTile`), MindLoom (`StatCard`), wave (`StatCell`), dailymind (`StatItem`),
  CashNotebook (`TopSummary`/`OverAllSummary`).
- **Exemplar:** MindLoom `…/presentation/insights/InsightsScreen.kt (StatCard)` — fully theme-driven, pure `String` inputs.
- **Generify:** replace PowerMonitor's `MonoNumeral` text-style with a `valueStyle` param; replace its `StatusState`/`StatusDot` enum with an optional `statusDotColor: Color?`; add optional leading `icon`.
- **Proposed API:**
  ```kotlin
  @Composable fun AsgardStatTile(
      label: String, value: String, modifier: Modifier = Modifier,
      icon: ImageVector? = null, statusDotColor: Color? = null,
      containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
      labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
      valueColor: Color = MaterialTheme.colorScheme.onSurface,
      valueStyle: TextStyle = MaterialTheme.typography.headlineSmall,
  )
  ```
- Effort: **small**.

### 2. Pro-gate kit — `AsgardProBadge` + `AsgardLockedOverlay` + `AsgardUpgradeCard`
The recurring freemium/monetization UI shell: a `PRO` badge, a blur/lock overlay over gated
content with a CTA, and an upgrade-prompt card. **Biggest de-dup opportunity in the ecosystem.**
- **Found in (~9):** FuelCheckIndia (`ProGateOverlay`), OmniScale (`ProGate`, `UpgradePromptCard`),
  Adhan (`ProGate`), BatteryWatch (`PremiumFeatureLockDialog`), sudoku, Kut (`ProPaywallSheet`,
  `ProUpsellSheet`, `ProBadge`), Aqua (`TrialOffer*`), lux (gating), PowerMonitor (`ProLockBadge`).
- **Exemplar:** OmniScale `…/ui/UpgradePromptCard.kt` (`UpgradePromptCard`, `ProGate`) + FuelCheckIndia `…/ProGateOverlay.kt`.
- **Note:** the **UI shell is generic and theme-safe**; billing/entitlement logic stays in each app
  (pass `isPro: Boolean` + `onUpgrade: () -> Unit`). Split into three small composables so apps take only what they need.
- **Proposed API (sketch):**
  ```kotlin
  @Composable fun AsgardProBadge(text: String = "PRO", icon: ImageVector? = Icons.Rounded.Lock, …)
  @Composable fun AsgardLockedOverlay(locked: Boolean, onUnlock: () -> Unit, content: @Composable () -> Unit)
  @Composable fun AsgardUpgradeCard(title: String, description: String, cta: String, onUpgrade: () -> Unit, …)
  ```
- Effort: **small–medium** (fold the variants into one superset each).

### 3. `AsgardBanner` — tonal callout / alert card
Container card: optional leading icon + title + optional description + optional trailing action.
Covers error/warning/info/success banners, inline error-with-dismiss, and tip/upgrade callouts —
the anatomy is identical, only container color / icon / action presence vary.
- **Found in (~6):** Aqua (`StatusBanner`, `LocationPermissionBanner`, `TrialOfferBanner`),
  PowerMonitor (`WarningCard`), lux (`ErrorState`), soma (`DinacharyaTipCard`),
  FuelCheckIndia (`ErrorState`), dailymind (`ErrorSnackbar`).
- **Exemplar:** PowerMonitor `…/ui/components/WarningCard.kt` — already `errorContainer`/`onErrorContainer` + typography.
- **Generify:** `containerColor` param (default `errorContainer`), `contentColor = contentColorFor(containerColor)`, optional `icon` and `action` slot. Callers pass `tertiaryContainer`/`secondaryContainer` for tip/upgrade variants.
- **Proposed API:**
  ```kotlin
  @Composable fun AsgardBanner(
      title: String, modifier: Modifier = Modifier, description: String? = null,
      icon: ImageVector? = null,
      containerColor: Color = MaterialTheme.colorScheme.errorContainer,
      contentColor: Color = contentColorFor(containerColor),
      titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
      descriptionStyle: TextStyle = MaterialTheme.typography.bodyMedium,
      action: (@Composable RowScope.() -> Unit)? = null,
  )
  ```
- Effort: **small**. (Distinct from `StatusChip`: this is a full-width callout, not a pill.)

### 4. Settings kit — `AsgardSettingRow` + `AsgardSettingToggleRow` + `AsgardSectionCard`
The standard preferences building blocks: labeled rows (value / switch / trailing slot) grouped
in a titled section card.
- **Found in (~5):** Aqua (`SectionCard`, `ProfileRow`, `GoalBreakdownRow`), wave (`InfoRow`,
  `ToggleRow`, `SettingsSection`, `DurationRow`), MindLoom (`PermissionRow`, `TimeCard`),
  Kut (`ResolutionRow`, `FormatRow`), BatteryWatch (`ScreenAndSleepRow`).
- **Exemplar:** wave `…/presentation/settings/SettingsScreen.kt` (`InfoRow`, `ToggleRow`, `SettingsSection`) — 100% MaterialTheme-driven.
- **Proposed API:**
  ```kotlin
  @Composable fun AsgardSettingRow(
      title: String, modifier: Modifier = Modifier, subtitle: String? = null,
      value: String? = null, icon: ImageVector? = null,
      onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
  )
  @Composable fun AsgardSettingToggleRow(
      title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
      modifier: Modifier = Modifier, subtitle: String? = null, icon: ImageVector? = null,
      iconTint: Color = MaterialTheme.colorScheme.primary,
  )
  @Composable fun AsgardSectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)
  ```
- Effort: **small**. (Standardize the toggle on the M3 `Switch` or a trailing slot.)

### 5. `AsgardListRow` — leading/title/subtitle/trailing list item card
Card row: optional leading icon/avatar, bold title, optional subtitle/caption, optional trailing
action slot. The generic list-item every app re-rolls.
- **Found in (~5):** BatteryWatch (`TopAppRow`), CashNotebook (`TransactionCard`/`TransactionItem`),
  Valy (`ClipItemCard`, `VaultClipCard`), FuelCheckIndia (`CityPriceCard`/`FuelPriceCard`),
  sudoku (`DifficultyRow`), soma (`MetricEntryRow`).
- **Exemplar:** soma `…/presentation/metrics/MetricsScreen.kt (MetricEntryRow)`.
- **Generify:** replace app models with `title: String, subtitle: String? = null, caption: String? = null` + leading `content`/`icon` slot + trailing `content` slot (or `onDelete`).
- Effort: **small**.

### 6. `AsgardBadge` — neutral label / tag pill
Small rounded pill (optional icon + text), filled/outlined variants. For static tags, `PRO`/`NEW`
markers, category chips. **Distinct from the existing `StatusChip`, which encodes semantic state.**
- **Found in (~7):** Kut (`ProBadge`, `TemplateChip`), Aqua (`HcActivityBadge`, `WeatherBonusChip`),
  PowerMonitor (`ProLockBadge`), lux (`PlatformChip`), MindLoom (`FeatureChip`),
  ClearCalc (`CategoryChip`), sudoku (`AssessmentStatChip`).
- **Exemplar:** MindLoom `…/onboarding/FeaturesScreen.kt (FeatureChip)` (has the filled/outlined logic).
- **Proposed API:**
  ```kotlin
  @Composable fun AsgardBadge(
      text: String, modifier: Modifier = Modifier, icon: ImageVector? = null,
      highlighted: Boolean = false, onClick: (() -> Unit)? = null,
      containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
      contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
  )
  ```
- Effort: **trivial**. (Decide: promote as a neutral pill, or extend `StatusChip` with an outlined/onClick variant.)

### 7. `AsgardProgressRing` — circular progress / gauge / dial
Canvas ring: faint track arc + rounded-cap sweep, with a centered content slot for value/label.
- **Found in (~4):** Aqua (`CircularProgressCard`), focus (`SprintDial`), wave (`CircularTimerRing`), soma (`WaterArc`).
- **Exemplar:** wave `…/presentation/common/CircularTimerRing.kt` — dependency-free; colors already default via `Color.Unspecified` sentinels (resolve to `colorScheme.primary`/`surfaceVariant`).
- **Proposed API:**
  ```kotlin
  @Composable fun AsgardProgressRing(
      progress: Float, modifier: Modifier = Modifier,
      size: Dp = 220.dp, strokeWidth: Dp = 8.dp,
      progressColor: Color = MaterialTheme.colorScheme.primary,
      trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
      startAngle: Float = -90f, sweepAngle: Float = 360f,
      content: @Composable BoxScope.() -> Unit = {},
  )
  ```
- Effort: **trivial–small** (consolidates the 270° arc and 360° ring variants).

---

## Tier 2 — good (2+ apps, or a strong single-app generic)

| Proposed | What it is | Seen in |
|---|---|---|
| `AsgardEmptyState` | centered icon + text placeholder for no-content screens | Thor (`EmptyStatePlaceholder`), lux (3 copies), CashNotebook (`EmptyBoxAnim`), FuelCheck |
| `AsgardOnboardingScaffold` + `AsgardFeatureRow` | onboarding page scaffold (step header + actions) and icon+title+body feature/benefit row | Lumina (`OnboardingScaffold`/`StepHeader`/`FeatureRow`), Aqua (`BenefitRow`), lux (`OnboardingPoint`), MindLoom (`FeaturesScreen`) |
| `AsgardAnimatedNumeral` / `AsgardLiveTimer` | `AnimatedContent` number that slides on change / ticking timer text | Thor (`AnimatedCounter`), PowerMonitor (`LiveNumeral`), dailymind (`LiveTimer`), sudoku (`TimerDisplay`) |
| `AsgardDialogScaffold` | titled dialog + body slot + confirm/dismiss actions | Brokk (`ExitDialog`), Aqua (`ExitConfirmDialog`), Thor (`ThankYouDialog` et al.), sudoku (`AssessmentResultDialog`) |
| `AsgardStepperRow` | label + −/value/+ tonal buttons with min/max disabling | wave (`DurationRow`), focus (`DurationSelector`), Kut |
| `AsgardSearchBar` | rounded search field with leading/trailing icons | Thor (`AppSearchBar`) |
| `AsgardLabeledSlider` | labeled M3 slider with value read-out | Aqua (`MeasurementSlider`) |
| `AsgardShimmer` | skeleton/shimmer placeholder box | Lumina (`ShimmerBox`) |
| `AsgardLottie` | themed Lottie raw-resource wrapper — **`Supporter.kt` is copy-pasted across apps** | Thor, CashNotebook, PDFReader |
| `AsgardTonalIconButton` | thin `FilledTonalIconButton` wrapper | lux (`IconAction`), sudoku (`ControlIconButton`) |
| `AsgardLoadingState` | centered spinner + optional caption | lux (`ResolvingState`), FuelCheck (`LoadingState`), dailymind (`GameLoadingContent`) |

---

## Tier 3 — single-app but generic (nice-to-have)

`AsgardArcGauge` (soma `WaterArc`), `AsgardCheckableRow` (soma `HabitItem`),
`AsgardBulletListItem`, `AsgardTimelineItem`, `AsgardTimePickerDialog`,
`AsgardStatusDot` (PowerMonitor `StatusDot` — **⚠ currently hardcodes `Color(0x…)`; make theme-safe
by taking `color: Color` + `size: Dp = 8.dp` instead of the app enum**),
`AsgardSegmentedControl` (CashNotebook `SegmentedAccountSelector`, FuelCheck `FuelTypeSelector` —
**⚠ overlaps `ConnectedButtonGroup`; prefer extending that**).

---

## Explicitly skipped

- **Already in Asgard:** `StatusChip`, `AsgardNavigationBar`/`Rail`, `AsgardHeader`,
  `ConnectedButtonGroup`.
- **App-specific / not theme-generic:** Gita puja items (`AartiThaliItem`, `DiyaItem`, `SankhItem` …,
  hardcoded palettes), Kut `Timeline`/`TrimHandle`/`VideoPreviewSurface` (video editor),
  all AdMob / native-ad wrappers (`NativeAdCompose`, `BannerAd`, `AdMobBanner` …),
  sudoku `NumberPad`, Valy overlay-service composables, PDFReader `SignatureCanvas`,
  Aqua HealthConnect/Weather cards, per-app charts.

---

## Theme-safety notes

Every Tier-1 and Tier-2 candidate already reads `MaterialTheme` and passed the hardcoded-color
check **except `AsgardStatusDot`** (Tier 3, flagged above). For the rest, the only work is lifting
a few app-specific colors / text-styles / enums into `MaterialTheme`-defaulted parameters — exactly
the pattern the existing `StatusChip` and `AsgardActionItem` already use.

---

## Methodology & coverage

- **Apps surveyed (23):** Thor, Aqua, Brokk, Kut, focus (the `com.valhalla.*` core); PowerMonitor,
  lux, soma, MindLoom, wave; BatteryWatch, ClearCalc, CashNotebook, FuelCheckIndia, Gita, Lumina,
  OmniScale, Valy, PDFReader, sudoku, Adhan, LedgerX, dailymind.
- **Method:** each app's `components`/`widgets` packages (and standalone reusable composables) were
  inventoried; candidate files were opened to judge theme-safety (MaterialTheme vs hardcoded colors)
  and app-coupling (imports of app models/VMs/nav/DI). Candidates were clustered by a normalized
  category slug across apps, then ranked by cross-app frequency × reuse value.
- **Excluded:** third-party forks `Seal` (`com.junkfood`) and `Android-Native-Root-Detector`
  (`com.reveny`); games (`IdleGalaxyShooter`, `StarForge`, `ConwayGameOfLife`); and non-Compose /
  tiny projects.

---

## Next steps (pending review)

1. **Review this backlog** and confirm the Tier-1 set.
2. ✅ Extracted Tier-1 + Tier-2 into `com.valhalla.asgard.components` with `@param` KDoc and
   `MaterialTheme` defaults, matching the existing `StatusChip` convention. Previews live in the
   `demo` module, not the library (the library stays preview-free / tooling-dependency-free).
3. Migrate the source apps to the Asgard versions (starting with the `com.valhalla.*` core), deleting
   the local copies (`Supporter.kt`/`AnimateLottieRaw`, `ConnectedButtonGroup` in Valy, per-app
   stat tiles and settings rows).
