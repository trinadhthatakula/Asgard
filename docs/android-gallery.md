# Android gallery and device tests

`:androidApp` is the permanent native gallery and test host. Its namespace and application ID
are **com.valhalla.asgard.app**. It uses the same `:demo` commonMain catalog as the web gallery,
with native clipboard support, saved last selection and Android Back handling. It honors system
font size and starts with the system theme/layout direction; the gallery's appearance controls
provide additional preview overrides. Content stays clear of status/navigation bars and the IME.

Choose `androidApp` in Android Studio and run it on an emulator/device. No separate consumer app
needs to be scaffolded for future component checks. Changes to Asgard use normal incremental builds.

## Build, install and open

```sh
./gradlew :androidApp:installDebug
adb shell am start -n com.valhalla.asgard.app/.MainActivity
```

An optional `entry` extra opens an example directly (the IDs match the web gallery's URL hashes):

```sh
adb shell am force-stop com.valhalla.asgard.app
adb shell am start -n com.valhalla.asgard.app/.MainActivity --es entry navigation-variations
```

## Device checks

Build/install both APKs and run the suite after changing sources:

```sh
./gradlew :androidApp:connectedDebugAndroidTest
```

Gradle's connected-test task removes its APKs afterward. For persistent installation, run:

```sh
./gradlew :androidApp:installDebug :androidApp:installDebugAndroidTest
```

Then rerun those **installed APKs without a Gradle build**:

```sh
python3 tools/run_android_tests.py --serial emulator-5554
python3 tools/run_android_tests.py --serial emulator-5554 \
  --class com.valhalla.asgard.app.ComponentDeviceTest#reverseGridScrollbarMovesThroughVariableSpans
```

The reuse command tests the installed binaries; rebuild/install when sources change. The helper
returns a nonzero exit status when the instrumentation runner fails, including failures that adb
itself reports with exit code zero. Gradle HTML reports are in
`androidApp/build/reports/androidTests/connected/debug/`.

The debug-only `ComponentTestActivity` hosts focused component tests. The normal launcher always
opens the complete gallery. The test activity is absent from release APKs. Neither the application
nor the compatibility fixture is published to Maven Central.

## Published-consumer compatibility

`:compatibilityConsumer` compiles calls against the actual Maven Central **2.0.1 Android AAR**,
using a strictly pinned compile-only dependency. Its compiled code runs in the native test app
against the current `:asgard` project. It covers all eight modified entry points present in 2.0.1.
The multiselect overload was merged after that published version, so it is verified separately
by native tests and the nine-entry-point descriptor check against the merged pre-expansion baseline.

The fixture is not a substitute for checking unrelated API surfaces or all consumer toolchains.
PR CI builds/lints the app and runs the instrumentation suite on an API 35 emulator; local release
validation additionally uses the available API 37 Magisk emulator.
