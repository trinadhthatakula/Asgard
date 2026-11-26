# Google Play listing

This Fastlane setup targets the Asgard Gallery Android package `com.valhalla.asgard.app`. The English (US) listing, Play icon, feature graphic and eight ordered phone screenshots are under `metadata/android/en-US/`. The captions are outside the full emulator screen. The captures use the Android gallery on an API 37 emulator in portrait.

## Validate the listing

Install Ruby, Bundler, JDK 21 and the project's Android SDK. From the repository root, run:

```sh
bundle install
bundle exec fastlane android validate_listing
```

## Sign a Play bundle

Fastlane loads `fastlane/.env` automatically. On a fresh checkout, create it from the tracked template and create a private directory for the keys.

```sh
(umask 077 && mkdir -p fastlane/keys && cp -n fastlane/.env.example fastlane/.env)
```

Place your upload JKS at `fastlane/keys/gallery-upload.jks`, or change `ASGARD_UPLOAD_KEYSTORE` to its location. Set the key alias, keystore password and key password in the local file. Paths may be absolute or relative to the repository root. Shell environment variables take precedence. Git ignores the local file and the entire keys directory. The signing tool receives passwords through the environment.

The release build uses R8 with the Android optimized rules and shrinks unused resources.

```sh
bundle exec fastlane android build_bundle version_code:1
```

The signed file is `androidApp/build/outputs/bundle/release/asgard-gallery-upload.aab`. Choose an unused, increasing version code for each subsequent Play upload. The gallery takes its display version from the project's `VERSION_NAME`.

## Upload to Play Console

For the first release, create the app in Play Console, configure Play App Signing, complete the required app declarations and upload the first signed bundle. Grant a Google Play Android Developer API service account access to this app. Place its JSON credentials at `fastlane/keys/play-service-account.json` or set `SUPPLY_JSON_KEY` in the local file to their location.

- `bundle exec fastlane android upload_listing` uploads the listing text, icon, feature graphic and phone screenshots. Supply replaces the remote images in the supplied categories.
- `bundle exec fastlane android upload_bundle version_code:2` builds, signs and uploads a draft to the internal track. To use an existing signed bundle, pass `aab:/absolute/path/to/gallery.aab`.
- Set the `track` and `status` lane options when you intend a different release track or rollout status.

[Google Play listing assets](https://support.google.com/googleplay/android-developer/answer/9866151) · [Fastlane supply](https://docs.fastlane.tools/actions/supply/)
