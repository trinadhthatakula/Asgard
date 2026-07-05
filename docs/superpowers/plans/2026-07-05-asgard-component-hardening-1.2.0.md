# Asgard Component Hardening 1.2.0 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add consistent, additive customization params and fix the 7 correctness bugs across all 27 Asgard components, introduce an `AsgardDefaults` token object, clear packaging debt, and refresh the gallery — shipped as 1.2.0 with zero breaking changes.

**Architecture:** Foundation-first: create the shared `AsgardDefaults` token object and do build-file hygiene, then fan out per-*file* edits (each file is edited by exactly one task so parallel work never conflicts), then update the gallery, then docs/version. Verification is compile-both-targets + visual gallery inspection (this library has no unit-test source set, per spec §3/§9).

**Tech Stack:** Kotlin Multiplatform (Android + wasmJs) from `commonMain`, Compose Multiplatform 1.12.0-alpha01, Material3 1.10.0-alpha05 (Expressive), Gradle (AGP 9.3.0-alpha06), Vanniktech maven-publish.

## Global Constraints

- **Additive only — no breaking changes.** Thor consumes Asgard and must compile against 1.2.0 unchanged.
- **Behavior-preserving defaults.** Every new param defaults to the value the component is currently hardcoded to (e.g. `labelMaxLines: Int = 2` for `AsgardActionItem`). No pixels change unless a bug fix requires it.
- **Naming drift handled via `@Deprecated`**, never rename in place (e.g. add `iconTint`, deprecate `iconColor` with `ReplaceWith`, wire both).
- **Convention:** content params first, `modifier: Modifier = Modifier` second, then optional cosmetics. New cosmetic params go *after* existing params to preserve positional-call compatibility.
- **Text-slot param naming:** `<slot>MaxLines: Int`, `<slot>Overflow: TextOverflow`, `<slot>Style: TextStyle`, `<slot>Color: Color`.
- **Shapes/spacing/icon-sizes** come from `AsgardDefaults` (Task 1); token *default values* equal current hardcoded values.
- **Target version:** `VERSION_NAME=1.2.0`. **Group:** `com.trinadhthatakula`. **Package:** `com.valhalla.asgard`.
- **Opt-ins:** `ExperimentalMaterial3ExpressiveApi` / `ExperimentalMaterial3Api` are already enabled library-wide via `languageSettings`; do not add per-call opt-ins that would leak to consumers.
- **Open-question decisions (from spec §12):** include `AsgardNavItem.badge` in 1.2.0; add an optional `description` param to `AsgardEmptyState` (single-`text` callers keep working).

## Shared implementation patterns

These recurring idioms are referenced by tasks below. Apply the concrete param names each task lists.

**Pattern A — text-slot params.** For a `Text(text)` that is currently `Text(x, style = someStyle)` with optional hardcoded `maxLines`:
```kotlin
// signature additions (after existing params, all defaulted to current behavior):
titleMaxLines: Int = Int.MAX_VALUE,          // or the current hardcoded value
titleOverflow: TextOverflow = TextOverflow.Ellipsis,
titleStyle: TextStyle = MaterialTheme.typography.titleSmall,  // the current style
titleColor: Color = MaterialTheme.colorScheme.onSurface,      // the current color
// call site:
Text(title, style = titleStyle, color = titleColor, maxLines = titleMaxLines, overflow = titleOverflow)
```
Where a component currently sets `maxLines = 1` with no `overflow`, default `overflow = TextOverflow.Ellipsis` (this is the "hard clip" fix for `AsgardBadge`/`StatusChip`).

**Pattern B — clickable accessibility.** For a `Modifier.clickable { onClick() }` that lacks a role:
```kotlin
.clickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = Role.Button,
) { onClick() }
```
Add `onClickLabel: String? = null` and (if absent) `enabled: Boolean = true` params.

**Pattern C — `AsgardDefaults` shape param.** Replace a hardcoded `RoundedCornerShape(N.dp)` with:
```kotlin
shape: Shape = AsgardDefaults.<name>Shape,   // token defaults to RoundedCornerShape(N.dp)
```

**Pattern D — modifier-default correction** (SearchBar, LabeledSlider). Change `modifier: Modifier = Modifier.fillMaxWidth()` to `modifier: Modifier = Modifier`, and apply `.fillMaxWidth()` as the *first* modifier internally so default rendering is unchanged:
```kotlin
Row(modifier = Modifier.fillMaxWidth().then(modifier)) { ... }
```

**Pattern E — icon contentDescription param.** Replace hardcoded `contentDescription = null`/`"Back"` with an overridable param defaulting to the current value.

**Verification note:** because there is no unit-test harness, "test" for every task = the module still compiles. Per-task, the implementer trusts valid Kotlin; **phase-boundary compile gates** (Tasks 1, 27, 30, 33) catch regressions. The final task runs the gallery for visual confirmation.

---

## Phase 0 — Foundation (serial; must complete before Phase 1)

### Task 1: Create `AsgardDefaults` + build-file hygiene

**Files:**
- Create: `asgard/src/commonMain/kotlin/com/valhalla/asgard/AsgardDefaults.kt`
- Modify: `asgard/build.gradle.kts` (deps)

**Interfaces:**
- Produces: `object AsgardDefaults` with the getters below. Every Phase-1 task consumes it for shape/spacing/icon-size defaults.

- [ ] **Step 1: Create `AsgardDefaults.kt`**
```kotlin
package com.valhalla.asgard

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central design tokens for Asgard components. Values default to each component's current
 * radius/size so adopting these tokens changes no pixels; a host may override by wrapping
 * content in its own theme once token-from-MaterialTheme migration lands (post-1.2.0).
 */
object AsgardDefaults {
    // Shapes
    val cardShape: Shape @Composable get() = MaterialTheme.shapes.large
    val tileShape: Shape @Composable get() = RoundedCornerShape(20.dp)
    val sectionCardShape: Shape @Composable get() = RoundedCornerShape(20.dp)
    val bannerShape: Shape @Composable get() = RoundedCornerShape(16.dp)
    val actionItemShape: Shape @Composable get() = RoundedCornerShape(24.dp)
    val upgradeCardShape: Shape @Composable get() = RoundedCornerShape(24.dp)
    val pillShape: Shape get() = CircleShape

    // Spacing / sizing
    val contentPadding: Dp = 16.dp
    val iconSize: Dp = 24.dp
    val iconChipSize: Dp = 48.dp
    val navContainerRadius: Dp = 32.dp
}
```

- [ ] **Step 2: Remove `materialIconsExtended`, promote api scopes** in `asgard/build.gradle.kts` `commonMain.dependencies`:
```kotlin
commonMain.dependencies {
    implementation(libs.compose.runtime)
    api(libs.compose.foundation)   // was implementation — types saturate public API
    api(libs.compose.material3)
    api(libs.compose.ui)           // was implementation — Modifier/Color/Shape/etc are public
    // materialIconsExtended removed: AsgardHeader's ArrowBack is in material-icons-core (transitive via material3)
}
```

- [ ] **Step 3: Verify `ArrowBack` still resolves.** Confirm `AsgardHeader.kt` imports `androidx.compose.material.icons.automirrored.filled.ArrowBack` (core), and grep the module for any other `androidx.compose.material.icons.` import that is NOT under `automirrored.filled` / `filled` core set.
Run: `grep -rn "material.icons" asgard/src`
Expected: only `AsgardHeader.kt` references an icon.

- [ ] **Step 4: Compile gate (Android + wasmJs).**
Run: `./gradlew :asgard:compileKotlinWasmJs :asgard:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL (proves `materialIconsExtended` removal + api-scope change + new file compile).

- [ ] **Step 5: Commit**
```bash
git add asgard/src/commonMain/kotlin/com/valhalla/asgard/AsgardDefaults.kt asgard/build.gradle.kts
git commit -m "feat(asgard): add AsgardDefaults tokens; drop materialIconsExtended; promote api scopes"
```

---

## Phase 1 — Component hardening (each task = one file; parallel-safe)

> Every task in this phase: (a) preserve current defaults, (b) apply the FIX items, (c) add the ADD params using Shared Patterns, (d) keep KDoc `@param` blocks updated for new params, (e) leave the file compiling. Commit per task with `feat(asgard): harden <Component>`.

### Task 2: `AsgardProgressRing.kt`
**Files:** Modify `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardProgressRing.kt`
- [ ] **FIX** non-finite guard — replace `val p = progress.coerceIn(0f, 1f)` with:
```kotlin
val p = (if (progress.isFinite()) progress else 0f).coerceIn(0f, 1f)
```
- [ ] **FIX** drop the redundant inner `Canvas(Modifier.size(size))` sizing — the Canvas already fills the Box; use `Modifier.fillMaxSize()` on the inner Canvas.
- [ ] **ADD** params (after existing, defaults preserve behavior): `cap: StrokeCap = StrokeCap.Round`, `trackStrokeWidth: Dp = strokeWidth`, `progressBrush: Brush? = null`, `animate: Boolean = false`, `animationSpec: AnimationSpec<Float> = spring()`. When `animate`, drive `p` through `animateFloatAsState(p, animationSpec)`. When `progressBrush != null`, use it for the progress arc instead of `progressColor`.
- [ ] **ADD** accessibility: `contentDescription: String? = null`; when non-null, put `Modifier.semantics { this.contentDescription = it; progressBarRangeInfo = ProgressBarRangeInfo(p, 0f..1f) }` on the root.
- [ ] **Verify:** file compiles (checked at Phase gate Task 27). **Commit.**

### Task 3: `AsgardListRow.kt`
**Files:** Modify `.../components/AsgardListRow.kt`
- [ ] **FIX** stale marquee: change `remember { mutableStateOf(false) }` to `remember(subtitle) { mutableStateOf(false) }`.
- [ ] **FIX** clickable a11y: apply Pattern B (add `Role.Button`, `onClickLabel`).
- [ ] **ADD** `enabled: Boolean = true`; `titleStyle`/`subtitleStyle`/`captionStyle` (Pattern A) with current styles as defaults; `titleColor`/`subtitleColor`/`captionColor`; `titleMaxLines: Int = 1`, `subtitleMaxLines: Int = 1`, `captionMaxLines: Int = 1` + `*Overflow = Ellipsis`; `contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)`; `iconContentDescription: String? = null` (Pattern E); `iconSize: Dp = AsgardDefaults.iconSize`; `verticalAlignment: Alignment.Vertical = Alignment.CenterVertically`.
- [ ] **Commit.**

### Task 4: `AsgardActionItem.kt`  *(user's headline example)*
**Files:** Modify `.../components/AsgardActionItem.kt`
- [ ] **FIX** duplicate a11y: set the icon `contentDescription = null` (label Text is already visible) — or expose `iconContentDescription: String? = null` and default null.
- [ ] **ADD** `labelMaxLines: Int = 2` (current), `labelOverflow: TextOverflow = TextOverflow.Ellipsis`, `labelStyle: TextStyle = MaterialTheme.typography.labelSmall`, `labelColor: Color = MaterialTheme.colorScheme.onSurface`, `width: Dp = 72.dp`, `shape: Shape = AsgardDefaults.actionItemShape`, `iconChipSize: Dp = 48.dp`, `iconSize: Dp = 24.dp`, `disabledAlpha: Float = 0.38f`, `onLongClick: (() -> Unit)? = null`.
- [ ] **Commit.**

### Task 5: `AsgardStatCard.kt`
**Files:** Modify `.../components/AsgardStatCard.kt`
- [ ] **ADD** `labelStyle: TextStyle = MaterialTheme.typography.labelSmall`, `labelMaxLines: Int = 1`, `valueMaxLines: Int = 1`, `labelOverflow`/`valueOverflow = Ellipsis`, `textAlign: TextAlign? = null`.
- [ ] **ADD** naming alias: add `iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant`; keep `iconColor` but mark `@Deprecated("Use iconTint", ReplaceWith("iconTint"))` and resolve the effective tint as `iconTint` (wire so the deprecated `iconColor` still works if passed — e.g. thread both, prefer explicitly-set). Simplest additive approach: keep `iconColor` as the real param, add `iconTint` that defaults to `iconColor`, deprecate `iconColor`. Document in KDoc.
- [ ] **FIX** unit spacing: add `unitSeparator: String = " "` param so callers can pass `""` for tight units.
- [ ] **Commit.**

### Task 6: `AsgardStatTile.kt`
**Files:** Modify `.../components/AsgardStatTile.kt`
- [ ] **ADD** `labelMaxLines: Int = 1`, `valueMaxLines: Int = 1` + `*Overflow = Ellipsis`; `iconContentDescription: String? = null`; `iconBadgeSize: Dp = 48.dp`, `iconSize: Dp = 24.dp`, `statusDotSize: Dp = 8.dp`.
- [ ] **FIX** clickable parity: give `onClick` a shared `interactionSource` + `Role.Button` + `expressivePress` (matching `AsgardActionItem`) so tap feedback and a11y are consistent.
- [ ] **Commit.**

### Task 7: `AsgardAnimatedNumeral.kt`
**Files:** Modify `.../components/AsgardAnimatedNumeral.kt`
- [ ] **ADD** `maxLines: Int = 1`, `overflow: TextOverflow = TextOverflow.Clip`, `softWrap: Boolean = false`, `textAlign: TextAlign? = null`, `animationSpec: FiniteAnimationSpec<Int> = tween(durationMillis)`, and `format: (Int) -> String = { it.toString() }` used by the count-up branch so caller number formatting is preserved.
- [ ] **Commit.**

### Task 8: `AsgardBanner.kt`
**Files:** Modify `.../components/AsgardBanner.kt`
- [ ] **FIX** fragile content color: when `containerBrush != null` OR `contentColorFor(containerColor) == Color.Unspecified`, fall back to `MaterialTheme.colorScheme.onSurface` for the default `contentColor`.
- [ ] **ADD** `titleMaxLines: Int = Int.MAX_VALUE`, `descriptionMaxLines: Int = Int.MAX_VALUE` + `*Overflow = Ellipsis`; `shape: Shape = AsgardDefaults.bannerShape`; `contentPadding: PaddingValues = PaddingValues(16.dp)`; `iconContentDescription: String? = null`; `iconSize: Dp = 24.dp`; `iconTint: Color = contentColor`; `verticalAlignment: Alignment.Vertical = Alignment.CenterVertically`; `onClick: (() -> Unit)? = null` (Pattern B when set).
- [ ] **Commit.**

### Task 9: `AsgardProGate.kt` (AsgardProBadge, AsgardUpgradeCard, AsgardLockedOverlay)
**Files:** Modify `.../components/AsgardProGate.kt`
- [ ] **AsgardProBadge FIX/ADD:** forward `onClick: (() -> Unit)? = null`, `outlined: Boolean = false`, `textStyle: TextStyle? = null` through to `AsgardBadge`. (Do NOT reorder existing params — additive only.)
- [ ] **AsgardUpgradeCard FIX:** derive the CTA `ButtonColors` from `contentColor`/`containerColor` so the button matches the card. **ADD** `titleMaxLines`/`descriptionMaxLines` + `*Overflow`, `titleStyle`/`descriptionStyle`, `shape: Shape = AsgardDefaults.upgradeCardShape`, `contentPadding: PaddingValues = PaddingValues(20.dp)`, `ctaColors: ButtonColors? = null`, `leading: (@Composable () -> Unit)? = null`.
- [ ] **AsgardLockedOverlay FIX:** while `locked`, wrap gated content in `Modifier.clearAndSetSemantics {}` so screen readers don't read through the blur. **ADD** `contentAlignment: Alignment = Alignment.Center`, `overlayShape: Shape? = null` (clip scrim/blur when set). (Trailing-lambda reorder is breaking → NOT done here.)
- [ ] **Commit.**

### Task 10: `AsgardDialogScaffold.kt`
**Files:** Modify `.../components/AsgardDialogScaffold.kt`
- [ ] **ADD** `shape: Shape? = null`, `containerColor: Color? = null`, `titleContentColor: Color? = null`, `textContentColor: Color? = null`, `iconContentColor: Color? = null`, `tonalElevation: Dp? = null`, `properties: DialogProperties = DialogProperties()`, `titleStyle`/`textStyle` + `titleMaxLines`/`textMaxLines`, `confirmEnabled: Boolean = true`, `confirmColors: ButtonColors? = null` (for destructive), `iconContentDescription: String? = null`. Pass non-null visual overrides through to `AlertDialog`.
- [ ] **Commit.**

### Task 11: `AsgardTonalIconButton.kt`
**Files:** Modify `.../components/AsgardTonalIconButton.kt`
- [ ] **ADD** `colors: IconButtonColors? = null`, `shape: Shape? = null`, `interactionSource: MutableInteractionSource? = null`, `iconTint: Color? = null`, `iconSize: Dp? = null`. Forward non-null values to `FilledTonalIconButton`/`Icon`.
- [ ] **Commit.**

### Task 12: `ConnectedButtonGroup.kt`
**Files:** Modify `.../components/ConnectedButtonGroup.kt`
- [ ] **FIX** guard: `require(selectedIndex in items.indices || items.isEmpty())` — or clamp; render nothing-selected safely without crash (keep current behavior but documented).
- [ ] **ADD** `enabled: Boolean = true` (group) + per-item `enabled` on `ConnectedButtonGroupItem` variants (additive field, default true); `colors: ToggleButtonColors? = null`; `spacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween`; group `contentDescription: String? = null`; item-label `maxLines: Int = 1`.
- [ ] **DEPRECATE** `menuLabel`/`menuIcon` on `ConnectedButtonGroupItem` with `@Deprecated("Unused; removed in 2.0")`.
- [ ] **Commit.**

### Task 13: `AsgardBadge.kt`
**Files:** Modify `.../components/AsgardBadge.kt`
- [ ] **FIX** hard-clip: add `overflow: TextOverflow = TextOverflow.Ellipsis` on the label Text.
- [ ] **ADD** `shape: Shape = AsgardDefaults.pillShape`, `textStyle: TextStyle = MaterialTheme.typography.labelMedium`, Pattern B (`onClickLabel`, `Role.Button`, `enabled`), `iconContentDescription: String? = null`, `iconSize: Dp = 14.dp`, `contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp)`, `borderWidth: Dp = 1.dp`, `borderColor: Color = contentColor`.
- [ ] **Commit.**

### Task 14: `StatusChip.kt`
**Files:** Modify `.../components/StatusChip.kt`
- [ ] **FIX** add `overflow = TextOverflow.Ellipsis`.
- [ ] **ADD** `textStyle: TextStyle = MaterialTheme.typography.labelSmall`, `shape: Shape = AsgardDefaults.pillShape`, `contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp)`, `onClick: (() -> Unit)? = null`, `leadingIcon: (@Composable () -> Unit)? = null`.
- [ ] **Commit.**

### Task 15: `AsgardHeader.kt`
**Files:** Modify `.../components/AsgardHeader.kt`
- [ ] **ADD** `titleMaxLines: Int = 1`, `titleOverflow: TextOverflow = TextOverflow.Ellipsis`, `titleStyle: TextStyle? = null`, `titleColor: Color? = null` (default keeps `primary`), `contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp)`, `backContentDescription: String = "Back"`, `iconContentDescription: String? = null`, `leading: (@Composable () -> Unit)? = null` (optional custom leading, takes precedence over `icon`/back).
- [ ] **Commit.**

### Task 16: `AsgardStepperRow.kt`
**Files:** Modify `.../components/AsgardStepperRow.kt`
- [ ] **FIX** symmetry: add `valueColor: Color = MaterialTheme.colorScheme.onSurface`.
- [ ] **ADD** `labelMaxLines: Int = 1`, `valueMaxLines: Int = 1` + `*Overflow = Ellipsis`, `labelStyle`/`valueStyle` (current defaults), `buttonColors: IconButtonColors? = null`, `decrementContentDescription: String? = null`, `incrementContentDescription: String? = null`, `valueMinWidth: Dp = 48.dp`.
- [ ] **Commit.**

### Task 17: `AsgardShimmer.kt`
**Files:** Modify `.../components/AsgardShimmer.kt`
- [ ] **ADD** `shape: Shape? = null` (supersedes `cornerRadius`; when null use `RoundedCornerShape(cornerRadius)`), and `@Deprecated` note on `cornerRadius` in KDoc pointing to `shape`; `animate: Boolean = true` (when false, render static base color — reduced-motion escape hatch).
- [ ] **Commit.**

### Task 18: `AsgardSettings.kt` (AsgardSectionCard, AsgardSettingRow, AsgardSettingToggleRow)
**Files:** Modify `.../components/AsgardSettings.kt`
- [ ] **FIX** disabled-dimming consistency: apply `Modifier.alpha(0.5f)` to `AsgardSettingToggleRow` when `!enabled` (match `AsgardSettingRow`).
- [ ] **AsgardSectionCard ADD** `titleMaxLines: Int = 1` + `titleOverflow`, `titleStyle`, `shape: Shape = AsgardDefaults.sectionCardShape`, `contentPadding: PaddingValues = PaddingValues(vertical = 8.dp)`, `titleTrailing: (@Composable () -> Unit)? = null`.
- [ ] **AsgardSettingRow ADD** `valueMaxLines: Int = 1` + `valueOverflow`, `valueStyle`, `valueColor`.
- [ ] **AsgardSettingToggleRow ADD** `switchColors: SwitchColors? = null`.
- [ ] **Commit.**

### Task 19: `AsgardEmptyState.kt` (AsgardEmptyState, AsgardLoadingState)
**Files:** Modify `.../components/AsgardEmptyState.kt`
- [ ] **AsgardEmptyState ADD** `description: String? = null` (rendered under `text` when set; single-`text` callers unchanged), `textStyle: TextStyle = MaterialTheme.typography.bodyMedium`, `maxLines: Int = Int.MAX_VALUE` + `overflow = Ellipsis`, `iconSize: Dp = 48.dp`, `iconContentDescription: String? = null`, `contentPadding: PaddingValues = PaddingValues(24.dp)`.
- [ ] **AsgardLoadingState ADD** `textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant`, `textStyle: TextStyle = MaterialTheme.typography.bodyMedium`, `indicatorSize: Dp? = null`, `strokeWidth: Dp? = null`, `contentPadding: PaddingValues = PaddingValues(24.dp)`.
- [ ] **Commit.**

### Task 20: `AsgardOnboarding.kt` (AsgardFeatureRow, AsgardOnboardingScaffold)
**Files:** Modify `.../components/AsgardOnboarding.kt`
- [ ] **AsgardFeatureRow ADD** `titleMaxLines`/`descriptionMaxLines` + `*Overflow`, `titleStyle`/`descriptionStyle`, `titleColor`/`descriptionColor`, `badgeSize: Dp = 44.dp`, `iconSize: Dp = 24.dp`, `spacing: Dp = 16.dp`, `badgeShape: Shape = CircleShape`, `onClick: (() -> Unit)? = null`, `iconContentDescription: String? = null`.
- [ ] **AsgardOnboardingScaffold ADD** `titleMaxLines`/`subtitleMaxLines` + `*Overflow`, `titleStyle`/`subtitleStyle`, `titleColor`/`subtitleColor`, `contentPadding: PaddingValues = PaddingValues(24.dp)`, `actionsArrangement: Arrangement.Horizontal = Arrangement.End`, `horizontalAlignment: Alignment.Horizontal = Alignment.Start`.
- [ ] **Commit.**

### Task 21: `AsgardSearchBar.kt`
**Files:** Modify `.../components/AsgardSearchBar.kt`
- [ ] **FIX** modifier default via Pattern D.
- [ ] **ADD** `enabled: Boolean = true`, `isError: Boolean = false`, `supportingText: (@Composable () -> Unit)? = null`, `keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)`, `focusRequester: FocusRequester? = null`, `leadingContentDescription: String? = null`, `clearContentDescription: String = "Clear"`, `placeholderStyle: TextStyle? = null`.
- [ ] **Commit.**

### Task 22: `AsgardLabeledSlider.kt`
**Files:** Modify `.../components/AsgardLabeledSlider.kt`
- [ ] **FIX** modifier default via Pattern D.
- [ ] **ADD** `enabled: Boolean = true`, `colors: SliderColors? = null`, `labelStyle`/`valueStyle` (current defaults), `labelColor`/`valueColor`, `labelMaxLines: Int = 1` + `labelOverflow`, `interactionSource: MutableInteractionSource? = null`.
- [ ] **Commit.**

### Task 23: `navigation/AsgardNavItem.kt`
**Files:** Modify `.../navigation/AsgardNavItem.kt`
- [ ] **ADD** KDoc with `@param` for every field. **ADD** additive field `badge: String? = null` (notification/count dot; default null = no badge) — decision per Global Constraints.
- [ ] **Commit.**

### Task 24: `navigation/AsgardNavigationBar.kt`
**Files:** Modify `.../navigation/AsgardNavigationBar.kt`
- [ ] **FIX** guard `selectedIndex` (clamp/ignore out-of-range without crash).
- [ ] **ADD** KDoc. **ADD** `containerColor: Color = MaterialTheme.colorScheme.surfaceContainer`, `selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer`, `selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer`, `unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant`, `unselectedAlpha: Float = 0.7f`, `shape: Shape? = null` (default keeps top-32.dp), `labelStyle: TextStyle? = null`. Render `AsgardNavItem.badge` as a small dot/count when non-null.
- [ ] **Commit.**

### Task 25: `navigation/AsgardNavigationRail.kt`
**Files:** Modify `.../navigation/AsgardNavigationRail.kt`
- [ ] **FIX** drop the outer `Surface.animateContentSize()` (it fights per-item `animateExpressiveResize()`); if resize animation is wanted on the surface, use `animateExpressiveResize()` instead. **FIX** guard `selectedIndex`.
- [ ] **ADD** KDoc + the same color/shape/alpha/label params as Task 24; `header: (@Composable ColumnScope.() -> Unit)? = null`, `footer: (@Composable ColumnScope.() -> Unit)? = null`; badge rendering.
- [ ] **Commit.**

### Task 26: `Motion.kt`
**Files:** Modify `.../Motion.kt`
- [ ] **FIX** migrate `Modifier.expressivePress` off deprecated `Modifier.composed {}` to a `Modifier.Node`-based factory (or `graphicsLayer` + `collectIsPressedAsState` without `composed`). Preserve exact behavior.
- [ ] **ADD** optional `pressSpec`/`releaseSpec: AnimationSpec<Float>` params to `expressivePress`; `animateExpressiveResize` gains no signature change (documented).
- [ ] **Commit.**

### Task 27: Phase-1 compile gate
- [ ] Run: `./gradlew :asgard:compileKotlinWasmJs :asgard:compileDebugKotlinAndroid`
- [ ] Expected: BUILD SUCCESSFUL. Fix any compile errors introduced by Tasks 2–26 before proceeding.
- [ ] Commit any fixes: `git commit -m "fix(asgard): resolve compile errors from hardening pass"`

---

## Phase 2 — Gallery ("demonstrate + fix")

### Task 28: `demo/.../AsgardCatalog.kt`
**Files:** Modify `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/AsgardCatalog.kt`
- [ ] **FIX** Shimmer preview: change the live preview to `AsgardShimmer(Modifier.fillMaxWidth().height(24.dp))` (matches its snippet; drops the `.size(320.dp)` override).
- [ ] **ADD** multi-variant previews demonstrating new params: `AsgardStatTile` (status-dot / icon-badge / `secondaryValue` / `animateValue` / `border`); `AsgardStatCard` (`iconInlineWithLabel` / clickable / `border`); `ConnectedButtonGroup` (a second entry or extra items using `Icon` and `IconWithLabel` variants); `AsgardActionItem` (custom `labelMaxLines`); `AsgardBanner` (overflow + `containerBrush`). Update each entry's `code` snippet to match its live preview.
- [ ] **Verify:** `./gradlew :demo:compileKotlinWasmJs` → BUILD SUCCESSFUL. **Commit.**

### Task 29: `demo/.../GalleryApp.kt`
**Files:** Modify `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/GalleryApp.kt`
- [ ] **ADD** category grouping: group the sidebar `LazyColumn` by `ComponentEntry.category` with sticky headers (`stickyHeader`), preserving current selection behavior.
- [ ] **Verify:** `./gradlew :demo:compileKotlinWasmJs` → BUILD SUCCESSFUL. **Commit.**

### Task 30: Phase-2 compile gate
- [ ] Run: `./gradlew :demo:compileKotlinWasmJs` → BUILD SUCCESSFUL.

---

## Phase 3 — Docs, packaging & version

### Task 31: Docs & hygiene
**Files:** Modify `README.md`, `gradle/libs.versions.toml`, `.gitignore` (if it ignores `kotlin-js-store`), `COMPONENT_BACKLOG.md`
- [ ] **README:** bump install-snippet versions `1.1.0` → `1.2.0`; add a short "Release checklist" note (bump README version on release). Align coordinate references to `com.trinadhthatakula:asgard`.
- [ ] **libs.versions.toml:** remove unused `compose-uiToolingPreview` and `compose-componentsResources` entries.
- [ ] **Commit** each logical change (`docs(readme): ...`, `chore(catalog): remove unused entries`).

### Task 32: Commit the wasm yarn lock
**Files:** `kotlin-js-store/wasm/yarn.lock`, `.gitignore`
- [ ] Ensure `kotlin-js-store` is NOT gitignored; `git add kotlin-js-store/wasm/yarn.lock`.
- [ ] **Commit** `chore(build): commit wasm yarn.lock for reproducible builds`.

### Task 33: Version bump
**Files:** Modify `gradle.properties`
- [ ] Set `VERSION_NAME=1.2.0`.
- [ ] **Commit** `chore(release): 1.2.0`.

---

## Phase 4 — Final verification

### Task 34: Full build + gallery visual check
- [ ] Run: `./gradlew :asgard:compileKotlinWasmJs :asgard:compileDebugKotlinAndroid :demo:compileKotlinWasmJs`
- [ ] Expected: BUILD SUCCESSFUL (all targets).
- [ ] Run the wasmJs gallery (`./gradlew :demo:wasmJsBrowserDevelopmentRun` or existing run task) and visually confirm in light + dark: hardened components render unchanged by default; new multi-variant previews render; category-grouped sidebar works; Shimmer fills width.
- [ ] Confirm `git grep -n "materialIconsExtended"` returns nothing in build scripts.
- [ ] **Optional (recommended, out of scope unless requested):** stand up `commonTest` with semantics smoke tests for nav / ConnectedButtonGroup / ProGate scrim / SearchBar clear.

---

## Self-Review

**Spec coverage:** ✅ Every spec §6 component maps to a Phase-1 task (Tasks 2–26); §5 `AsgardDefaults` → Task 1; §7 hygiene → Tasks 1, 31–33; §8 gallery → Tasks 28–29; §9 verification → Tasks 27, 30, 34; §12 open questions → Global Constraints (badge in Task 23, EmptyState `description` in Task 19).

**Placeholder scan:** No "TBD/handle edge cases/add validation" — each task lists exact params with exact defaults and the specific FIX. Bug fixes include the actual code.

**Type consistency:** Text-slot params follow the `<slot>MaxLines/Overflow/Style/Color` pattern uniformly; `AsgardDefaults` getter names (`actionItemShape`, `tileShape`, `sectionCardShape`, `bannerShape`, `upgradeCardShape`, `pillShape`) are referenced consistently by Tasks 4, 6, 8, 9, 13, 14, 18.

**Sequencing:** Task 1 (AsgardDefaults + build) precedes all consumers; Phase-1 tasks each own a distinct file (parallel-safe); compile gates at Tasks 27/30/34.
