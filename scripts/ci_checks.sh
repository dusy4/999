#!/bin/bash
set -e

echo "Running Lint..."
./gradlew lintDebug

echo "Running Unit Tests..."
./gradlew testDebugUnitTest

echo "Verifying Release Build (R8)..."
./gradlew assembleRelease

echo "CI Checks Completed Successfully."
