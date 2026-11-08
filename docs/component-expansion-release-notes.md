# Asgard 2.1.0 release notes

Prepared on `feat/asgard-component-expansion`. Maven publication, tagging and the GitHub release
run automatically after this version bump merges to `main`.

- Add independent multiselect and connected-button shape configuration for single and multiple selections,
  including fixed connected shapes, uniform/custom state shapes and standalone geometry.
- Add docked/floating navigation surfaces, expanding/segmented/standard destinations,
  text-only content, configurable indicators and measured label fallback. Retain existing APIs.
- Add shared sheets/action sheets, a searchable inline/sheet picker, scrolling action toolbar,
  image-loader-independent avatar, draggable list/grid scrollbar and a date/value calendar heatmap.
- Fix disabled search clearing, duplicate settings-switch targets and slider label/value semantics.
- Rebuild the gallery for compact windows with searchable navigation, separate theme controls,
  shareable example URLs, code selection and clipboard completion/error handling.
- Add native Compose interaction tests, calendar/scrollbar/picker math coverage and retained JVM
  entry-point checks. Include production wasm bundling in PR validation.
- Add the permanent Android gallery app (`com.valhalla.asgard.app`), sharing the web catalog,
  with native clipboard, Android Back handling, system font scale and theme/inset support.
- Add reusable emulator tests, build-free reruns of installed test APKs, and an independently
  compiled published-2.0.1 consumer fixture. Add API 35 emulator coverage to PR CI.
- Correct reversed scrollbar progress and use actual item boundaries for Home/End requests.
- Compile Android against SDK 37.2 for the resolved Compose dependency requirements.

See [the API guide](component-expansion.md) and [the implementation validation record](audits/asgard-next-release/implementation-validation.md) for details.
