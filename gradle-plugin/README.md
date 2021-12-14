# gradle-plugin

This is a Gradle plugin making the development of Mikbot and [PF4J](https://pf4j.org) Gradle plugins easier.

**If you don't make a Mikbot plugin please read [this](#using-outside-mikbot) section**

# Configuration

```kotlin
mikbotPlugin {
    description.set("Description")
}

// For more configuration look at this extension class

/**
 * Extension for configuring plugins for PF4J.
 */
class PluginExtension {
    /**
     * The version of the application this plugin requires (optional).
     */
    val requires: String?

    /**
     * The description of the plugin.
     */
    val description: String

    /**
     * The author of the plugin.
     */
    val provider: String

    /**
     * The license the plugin is licensed under.
     */
    val license: String

    /**
     * Whether to ignore mikbot dependencies.
     * **TL;DR** If you make a Mikbot plugin leave this turned on, if not turn it off
     *
     * This disables the plugin automatic dependency filtering for Mikbot,
     * some transitive dependencies of plugins are shared with mikbot, but not detected as duplicated by
     * Gradle because of version conflicts, including them will result in a runtime class loading error,
     * therefore this plugin removes any duplicates with Mikbot from the output of the `assemblePlugin`
     * task to avoid these issues.
     * However, if you don't make a mikbot plugin this doesn't make sense to do, so you should disable this settings
     */
    val ignoreDependencies: Boolean

    /**
     * The location of the plugins main file.
     *
     * If you use the KSP processor you don't need to worry about this.
     */
    val pluginMainFileLocation: Path
}
```

# Building a plugin

In order to build a plugin you need to execute the `assemblePlugin` Task, which will build a zip file for you compatible
with PF4J

# Usage
```kotlin
plugins {
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.0.0"
}
```

# Publishing

This plugin also supports generating a [pf4j-update](https://github.comm/pf4hj/pf4j-update) repository, therefore you
need to configure the following:

```kotlin
tasks {
    buildRepository {
        // The address your repository is hosted it
        repositoryUrl.set("https://plugin-repository.mikbot.schlaubi.net")
        // The directory the generated repository should be in
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        // The URL of the project
        projectUrl.set("https://github.com/DRSchlaubi/mikbot")
    }
}
```

If there already is something in the target directory, the plugin tries to update the existing repository accordingly.
Therefore, it makes sense to clone the existing repository first. I use [GitHub Pages](https://pages.github.com/) and
this workflow to generate my own repository.

<details>
<summary>GitHub Actions Workflow</summary>

```yaml
  update_repository:
    name: Update repository
    runs-on: ubuntu-20.04
    needs: [ build ]
    if: github.event_name != 'pull_request' && github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          distribution: 'temurin'
          java-version: '16'
      - run: rm -rf .git/ # delete .git here to ignore the parent git repo and only care about plugin-repo
      - uses: actions/checkout@v2
        with:
          ref: plugin-repo
          path: ci-repo
      - uses: gradle/gradle-build-action@v1
        with:
          arguments: copyFilesIntoRepo
      - run: |
          cd ci-repo
          git config --local user.email "actions@github.com"
          git config --local user.name "actions-user"
          git add .
          git commit -m "Update plugin repository"
      - name: Push changes
        uses: ad-m/github-push-action@master
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          branch: plugin-repo # the branch GH pages is on
          directory: ci-repo # the targetDirectory specified above
```

</details>

# Plugin dependencies

To add plugins the `plugin` and `optionalPlugin` dependency configurations exist, please only use them if:

- The plugin is a local Gradle module, built with this plugin
- The plugin is a remote plugin
  - built with this plugin
  - using its artifact name as a plugin id

# Default resource Bundle

If you want to use a specific resource bundle as a fallback you can generate a fallback bundle by executing this task:

```kotlin
task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
  defaultLocale.set(Locale("en", "GB"))
}
```

# Using outside Mikbot

This plugin also works outside Mikbot, therefore you need to configure the following:

Automatically:

```kotlin
mikBotPlugin {
  usePF4J()
}
```

Manually

```kotlin
mikBotPlugin {
    // Disables dependency optimization for Mikbot
    ignoreDependencies.set(true)
    // Path to this plugins main file (this will get patched, so using a build location makes sense)
    pluginMainFileLocation.set(
        buildDir.resolve("resources/main/plugin.properties").toPath()
    )
}
```
