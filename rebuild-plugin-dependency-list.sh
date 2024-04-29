#!/usr/bin/env sh
./gradlew runtime:exportDependencies :gradle-plugin:clean
rm -rf buildSrc/build
./gradlew
