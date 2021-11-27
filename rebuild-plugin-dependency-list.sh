#!/usr/bin/env sh
./gradlew exportDependencies :gradle-plugin:clean
rm -rf buildSrc/build
./gradlew
