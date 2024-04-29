./gradlew runtime:exportDependencies :gradle-plugin:clean
Remove-Item -Recurse buildSrc/build
./gradlew
