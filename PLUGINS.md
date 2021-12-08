# How to write plugins

This Bot provides a plugin system with various extension points

# Default plugin

If you want to make a plugin you should really use [Gradle](https://gradle.org) using Maven is possible, but all official tooling is only provided for Gradle.

build.gradle.kts
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.6.0-1.0.1" // used for plugin-processor
    kotlin("jvm") version "1.6.0"
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.0.3"
}

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    // this one is included in the bot itself, therefore we make it compileOnly
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("dev.schlaubi", "mikbot-api", "2.0.1")
    ksp("dev.schlaubi", "plugin-processor", "1.0.0")
}

mikbotPlugin {
    description.set("This is a cool plugin!")
}

tasks {
    buildRepository {
        repositoryUrl.set("https://plugin-repository.mikbot.schlaubi.net")
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        projectUrl.set("https://github.com/DRSchlaubi/tree/main/${project.path.drop(1).replace(":", "/")}")
    }
}

```

And then you can run `./gradlew assemblePlugin` to get your plugin.zip file.
<details>
<summary>In case of <b>File not found</b> exception</summary>

Set `ksp("dev.schlaubi", "plugin-processor", "2.0.1")` to `implementation("dev.schlaubi", "plugin-processor", "2.0.1")` and reload your dependencies. Then change it back again. _(Workaround)_
    
</details>
Alternatively to generating a zip file, you can also use shadowJar, but make sure to add the manifest is added.
For more information about Packaging read [the packaging documentation](https://pf4j.org/doc/packaging.html) and [the plugins documentation](https://pf4j.org/doc/plugins.html)

# Plugin main
```kotlin
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain // You need the KSP processor in order for this annotation to work
class DatabaseI18NPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override suspend fun ExtensibleBotBuilder.apply() {
        i18n {
            defaultLocale = Config.DEFAULT_LOCALE
            localeResolver { _, _, user ->
                user?.let {
                    LanguageDatabase.collection.findOneById(it.id)?.locale ?: Config.DEFAULT_LOCALE
                }
            }
        }
    }
}
```

# Extension points
There are two extension points built into the API itself for adding things to the Owner and Settings module `SettingsExtensionPoint`, `OwnerExtensionPoint`

Allowing you to add your own commands to these modules

### Example extension
```kotlin
@Extension
class DatabaseI18NSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        languageCommand()
    }
}
```

### Example extension point
```kotlin
/**
 * Extension point for GDPR functionalities.
 */
interface GDPRExtensionPoint : ExtensionPoint {
    /**
     * Provides the [DataPoints][DataPoint] of this module.
     */
    fun provideDataPoints(): List<DataPoint>
}
```

You can then access that point you can use this
```kotlin
pluginSystem.getExtensions<GDPRExtensionPoint>()
```

# Publishing
Please read [this](gradle-plugin/README.md#publishing) to learn how to publish plugins
