# Plugins guide

**This document neither explains [Kotlin](https://kotlinlang.org/docs/home.html),
[Kord](https://github.com/kordlib/kord/wiki), nor [Kord-Extensions](https://kordex.kotlindiscord.com/), if you don't
know these things stop reading now before complaining about these things not getting explained**

# Index

- [What is a plugin](#what-is-a-plugin)
- [How to make a plugin](#how-to-write-a-plugin)
- [How to assemble a plugin](#how-to-assemble-the-plugin)
- [How to run the bot](#how-to-run-the-bot)
- [How to dockerize the bot](#how-to-dockerize-the-bot)
- [What are Extension Points](#what-are-extension-points)
- [The mikbot-api](#the-mikbot-api)
  - [The database api](#the-database-api)
  - [The config api](#the-config-api)
  - [The all shards ready event](#the-all-shards-ready-event)
  - [Predefined modules](#predefined-modules)
  - [Documentation](#documentation)
- [Featured plugins](#featured-plugins)
  - [GDPR](#gdprcoregdpr)
  - [ktor](#ktorutilsktor)

# What is a plugin

To Mikbot a plugin is essentially a single file changing KordEx configurations, everything else is KordEx and therefor
not
explained here.

Kord-Extensions already provides "plugins", but KordEx calls them Extensions, a Mikbot plugin is simply one or more
KordEx extensions + optional additional KordEx configuration housed in a zip file, therefore the plugin interface also
has only 2 main functions

- `ExtensibleBotBuilder.ExtensionsBuilder.addExtensions()` which is to add Extensions
- `ExtensibleBotBuilder.apply()` which is to change the KordEx/ExtensibleBot configuration

And since you probably ignored the big warning at the top, you can read more about those
[here](https://kordex.kotlindiscord.com/en/concepts/extensions)

# How to write a plugin

If you want to make a plugin you should really use [Gradle](https://gradle.org) using Maven, or no build tool at all
is possible, but all official tooling is only provided for Gradle.


<details>
    <summary>Example build.gradle.kts</summary>

**This is the most basic configuration, for more options read the [Gradle plugin documentation](gradle-plugin)**

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

</details>

**Example** Plugin main class

```kotlin
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class MyCoolPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override suspend fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::MyExtension123)
    }
}
```

# How to assemble the plugin

You have 3 ways of building/distributing your plugin

- The `assemblePlugin` task: Can be used to generate a single plugin.zip file (Found in `build/plugin`)
- The `assembleBot` task: Can be used to generate your own distribution of the bot bundeling the plugin (Found
  in `build/bot`)
- Plugin publishing: You can read more about that [here](gradle-plugin/README.md#publishing)

<details>
<summary>In case of <b>File not found</b> exception</summary>

Set `ksp("dev.schlaubi", "plugin-processor", "2.2.0")` to `implementation("dev.schlaubi", "plugin-processor", "2.2.0")`
and reload your dependencies. Then change it back again. _(Workaround)_

</details>
Alternatively to generating a zip file, you can also use shadowJar, but make sure to add the manifest is added.
For more information about Packaging read [the packaging documentation](https://pf4j.org/doc/packaging.html) and [the plugins documentation](https://pf4j.org/doc/plugins.html)

# How to run the bot

There are two ways to run the bot
- Running it from a distribution
  - [either your own](#how-to-assemble-the-plugin)
  - [docker](https://github.com/DRSchlaubi/mikbot/pkgs/container/mikmusic%2Fbot)
  - [binary](https://storage.googleapis.com/mikbot-binaries)
- Running through Gradle
  - You can read more about that [here](gradle-plugin/README.md#run-the-bot)

# How to dockerize the bot

If you want to dockerize a bot with bundled plugins, you can use this `Dockerfile`

```Dockerfile
FROM gradle:jdk18 as builder
WORKDIR /usr/app
COPY . .
RUN gradle --no-daemon installBotArchive

FROM ibm-semeru-runtimes:open-18-jre-focal

WORKDIR /usr/app
COPY --from=builder /usr/app/build/bot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
```

# What are Extension points

Extension points are interfaces plugins can provide for other plugins to implement.
A good example is the [gdpr](core/gdpr) plugin, which provides an interface for other plugins to add GDPR data points

The GDPR plugin has an extension point like this.

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

Then another plugin can implement this data point like this

**Implementations of ExtensionPoints are called Extensions, but have nothing to do with KordExteensions Extensions,
which are the extensions added in the plugin main**

@Extension
```kotlin
class Connect4GDPRExtensionPoint : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(Connect4StatsDataPoint, Connect4ProcessDataPoint)
}
```

The GDPR plugin can then access the data points like this
```kotlin
pluginSystem.getExtensions<GDPRExtensionPoint>().flatMap { it.provideDataPoints() } 
```

# The mikbot-api
The mikbot api itself provides some utilities itself in extension to kordex

## The database API

The bot has a Connection to [MongoDB](https://mongodb.org) using [KMongo](https://github.com/Litote/kmongo) if database
credentials are present, you can use these like this

```kotlin
object PluginDatabase : KoinComponent {
    val stats = database.getCollection<PluginStats>("plugin_stats")
}
```

## The config api

The bot bundles [stdx-envconf](https://github.com/DRSchlaubi/stdx.kt/tree/main/envconf) for configuration, you can find
the bots own configuration file
[here](https://github.com/DRSchlaubi/mikbot/blob/main/api/src/main/kotlin/dev/schlaubi/mikbot/plugin/api/config/Config.kt)

## The all shards ready event

The bot has an AllShardsReadyEvent which is fired once all shards fired a ReadyEvent

## Predefined modules

The bot also has a Settings and Owner module, so you can group all related commands into a single KordExtensions Extension.

You can add commands to these extensions like this

```kotlin
@Extension
class YourPluginSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        disableBansCommand()
    }
}
```

For the OwnerModule use the `OwnerExtensionPoint`

Both modules also have permission helpers
- `SlashCommand<*, *>.ownerOnly()` - registers the command on `OWNER_GUILD` and requires the Permission `ADMINISTRATOR`
- `SlashCommand<*, *>.guildAdminOnly()` - disables the command in dms and requires `MANAGE_SERVER`

## Documentation

There is currently no hosted documentation for all the utility classes, but they all have doc comments, you can read
[here](https://github.com/DRSchlaubi/mikbot/tree/main/api/src/main/kotlin/dev/schlaubi/mikbot/plugin/api/util)

# Featured plugins

## [GDPR](core/gdpr)
Plugin adding functionality to comply with the [GDPR](https://gdpr.eu/)
Dependency: `plugin("dev.schlaubi", "mikbot-gdpr", "<version>")`
Docs: [README.md](core/gdpr/README.md)

## [ktor](utils/ktor)
An API to have a webserver in multiple plugins on the same port powered by [Ktor](https://ktor.io)
Dependency: `plugin("dev.schlaubi", "mikbot-ktor", "<version>")`
Docs: [README.md](utils/ktor/README.md)

# What's next
- Read the [Setup Guide](SETUP.md) to learn how to operate the bot runtime
- Read the [Game API guide](game/game-api/GUIDE.md) to learn how to make a game

