# How to write plugins

This Bot provides a plugin system with various extension points

# Default plugin

If you want to make a plugin you should really use [Gradle](https://gradle.org) using Maven is possible, but all official tooling is only provided for Gradle.

build.gradle.kts
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.6.21-1.0.5" // used for plugin-processor
    kotlin("jvm") version "1.6.21"
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // this one is included in the bot itself, therefore we make it compileOnly
    // Or use: 'kotlin.stdlib.default.dependency=false' in gradle.properties
    compileOnly(kotlin("stdlib-jdk8"))
    mikbot("dev.schlaubi", "mikbot-api", "3.2.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.2.0")
}

mikbotPlugin {
    description.set("This is a cool plugin!")
    provider.set("Schlaubi")
    license.set("MIT")
}

```

And then you can run `./gradlew assemblePlugin` to get your plugin.zip file.
<details>
<summary>In case of <b>File not found</b> exception</summary>

Set `ksp("dev.schlaubi", "plugin-processor", "2.2.0")` to `implementation("dev.schlaubi", "plugin-processor", "2.2.0")` and reload your dependencies. Then change it back again. _(Workaround)_
    
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

# Development
<details>
    <summary>Run with Docker-Compose</summary>
    
```yaml
# dev.docker-compose.yaml
version: "2.0"

services:
  mongo:
    image: mongo
    environment:
      MONGO_INITDB_ROOT_USERNAME: bot
      MONGO_INITDB_ROOT_PASSWORD: bot
    volumes:
      - mongo-data:/data/db
  bot:
    image: ghcr.io/drschlaubi/mikmusic/bot:latest
    env_file:
      - .env
    depends_on:
      - mongo
    volumes:
      - ./plugins:/usr/app/plugins
    ports:
      - "8080:8080"
volumes:
  mongo-data: { }
```
    
Instead of running `gradle assemble`, now run `gradle buildAndCopy` to automatically load it into the plugins folder.
    
Then use `docker-compose -f dev.docker-compose.yaml up`.

</details>

# Running
You can simply run a bot with the plugin installed by running `gradle runBot`, please set all required environment variables like this in a `.test-env` file
```
DISCORD_TOKEN=thetoken
```

If you want to use any core plugins please download them using the `DOWNLOAD_PLUGINS` environment variable

# Publishing
Please read [this](gradle-plugin/README.md#publishing) to learn how to publish plugins
