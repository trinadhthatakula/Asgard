# Google Play listing

This Fastlane setup targets the Asgard Gallery Android package `com.valhalla.asgard.app`. The English (US) listing, Play icon, feature graphic and eight ordered phone screenshots are under `metadata/android/en-US/`. The captions are outside the full emulator screen. The captures use the Android gallery on an API 37 emulator in portrait.

## Validate the listing

Install Ruby, Bundler, JDK 21 and the project's Android SDK. From the repository root, run:

```sh
bundle install
bundle exec fastlane android validate_listing
```

## Sign a Play bundle

Use the gallery's upload keystore. Set `ASGARD_UPLOAD_KEYSTORE` (an absolute path), `ASGARD_UPLOAD_ALIAS`, `ASGARD_UPLOAD_STORE_PASSWORD` and `ASGARD_UPLOAD_KEY_PASSWORD` in your local environment or secret store. The lane passes the password variable names to the JDK's signing tool.

```sh
bundle exec fastlane android build_bundle version_code:1
```

The signed file is `androidApp/build/outputs/bundle/release/asgard-gallery-upload.aab`. Choose an unused, increasing version code for each subsequent Play upload. The gallery takes its display version from the project's `VERSION_NAME`.

## Upload to Play Console

For the first release, create the app in Play Console, configure Play App Signing, complete the required app declarations and upload the first signed bundle. Grant a Google Play Android Developer API service account access to this app. Set `SUPPLY_JSON_KEY` to the absolute path of its JSON credentials outside the checkout.

- `bundle exec fastlane android upload_listing` uploads the listing text, icon, feature graphic and phone screenshots. Supply replaces the remote images in the supplied categories.
- `bundle exec fastlane android upload_bundle version_code:2` builds, signs and uploads a draft to the internal track. To use an existing signed bundle, pass `aab:/absolute/path/to/gallery.aab`.
- Set the `track` and `status` lane options when you intend a different release track or rollout status.

[Google Play listing assets](https://support.google.com/googleplay/android-developer/answer/9866151) · [Fastlane supply](https://docs.fastlane.tools/actions/supply/)
