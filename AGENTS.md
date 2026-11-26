# Working on Asgard

Follow the [contribution and review rules](CONTRIBUTING.md) for every change. Use the repository's [asgard-ui skill](.claude/skills/asgard-ui/SKILL.md) when working on Asgard APIs, integration examples or the galleries. Its reference files document component contracts and platform support.

The `:asgard` and `:asgard-glass` modules supply the libraries. The Android app in `:androidApp` and the Kotlin/Wasm web gallery share the catalog in `:demo`. Run the relevant [local checks](CONTRIBUTING.md#local-checks). When the skill, recipes or samples change, run `python3 tools/update_gallery_reference.py`, then `python3 tools/update_gallery_reference.py --check`. Commit the updated web reference with its source.

## Dependency updates

Run these commands from the repository root.

```sh
python3 update_dependencies.py --dry-run
python3 update_dependencies.py
python3 update_dependencies.py --wrapper --dry-run
```

The first command previews updates to `gradle/libs.versions.toml`. The second writes the catalog changes. The third also previews Gradle wrapper distribution and checksum updates. Remove `--dry-run` to apply them. Use `--allow-major` for a major wrapper upgrade and `--pre-releases` to opt stable dependencies into prereleases. Review the diff and build both supported targets when changing shared versions. Keep the published compatibility fixture pinned.

The [Fastlane guide](fastlane/README.md) covers Android release signing, Play listings and upload lanes.
