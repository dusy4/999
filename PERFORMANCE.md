# Performance Optimization Guide

## Profiling

### Android Studio Profiler
1. Run the app on a physical device or emulator (API 26+ recommended).
2. Open **View > Tool Windows > Profiler**.
3. Click the **+** button and select your device and app process.
4. Use the **CPU**, **Memory**, and **Energy** timelines to identify hotspots.
   - **CPU**: Record a trace to see method execution times. Look for long-running operations on the main thread.
   - **Memory**: Capture a heap dump to find memory leaks or large objects. Use the "Leaks" filter.
   - **Composition**: In newer Android Studio versions, use the Layout Inspector to see recomposition counts.

### Compose Compiler Metrics
We have enabled Compose Compiler Metrics. After a build, check `app/build/compose_metrics` (and `feature-chat/build/compose_metrics`) for reports.
- `app_release-composables.txt`: Detailed stability information of your composables.
- `app_release-classes.txt`: Stability of your data classes.
- `app_release-module.json`: Summary metrics.

Look for functions that are restartable but not skippable (`restartable, but not skippable`), as these may cause unnecessary recompositions.

## Baseline Profiles
To generate a baseline profile:
1. Ensure you have a `:benchmark` module set up (see [Macrobenchmark documentation](https://developer.android.com/topic/performance/baselineprofiles/overview)).
2. Run the generator:
   ```bash
   ./gradlew :benchmark:pixel6Api33BenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
   ```
3. Copy the generated `baseline-prof.txt` to `app/src/main/baseline-prof.txt`.

## CI Checks
Run the following script to verify code quality and build integrity:
```bash
./scripts/ci_checks.sh
```
This script runs lint, unit tests, and verifies the release build (R8).
