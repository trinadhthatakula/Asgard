# Component expansion release notes (draft)

Unpublished implementation branch; version/tag/publication have not been changed.

- Add independent connected-button shape configuration for single and multiple selections,
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
- Compile Android against SDK 37.2 for the resolved Compose dependency requirements.

See [the API guide](component-expansion.md) and [the implementation validation record](audits/asgard-next-release/implementation-validation.md) for details.
