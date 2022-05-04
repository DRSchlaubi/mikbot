# Mik Bot

[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/DRSchlaubi/mikbot/CI?logo=github&style=flat-square)](https://github.com/DRSchlaubi/mikbot/actions/workflows/ci.yaml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/dev.schlaubi.mikbot.gradle-plugin?logo=gradle&style=flat-square)](https://plugins.gradle.org/plugin/dev.schlaubi.mikbot.gradle-plugin)
[![Latest Version](https://img.shields.io/maven-metadata/v?logo=apache%20maven&metadataUrl=https%3A%2F%2Fschlaubi.jfrog.io%2Fartifactory%2Fmikbot%2Fdev%2Fschlaubi%2Fmikbot-api%2Fmaven-metadata.xml&style=flat-square)](https://schlaubi.jfrog.io/ui/native/mikbot/dev/schlaubi/mikbot-api/)
[![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-blueviolet?style=flat-square&logo=kotlin)](https://kotlinlang.org)

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/DRSchlaubi/mikbot)

A modular framework for building Discord bots in [Kotlin](https://kotlinlang.org)
using [Kordex](https://github.com/Kord-Extensions/kord-extensions/) and [Kord](https://github.com/kordlib).

**If you are here for mikmusic, click [here](music) and [there](mikmusic-bot).**

**If you are here for Votebot, click [here](votebot).**

# Help translating this project
<a href="https://hosted.weblate.org/engage/mikbot/">
<img src="https://hosted.weblate.org/widgets/mikbot/-/287x66-grey.png" alt="Übersetzungsstatus" />
</a>

## Deployment

For a full explanation on how to deploy the bot yourself take a look at [this](./SETUP.md)

### Requirements

- [Sentry](https://sentry.io) (Optional)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Example Environment file

<details>
<summary>.env</summary>

```properties
ENVIRONMENT=PRODUCTION
SENTRY_TOKEN=<>
DISCORD_TOKEN=<>
MONGO_URL=mongodb://bot:bot@mongo
MONGO_DATABASE=bot_prod
LOG_LEVEL=DEBUG
BOT_OWNERS=416902379598774273
OWNER_GUILD=<>
UPDATE_PLUGINS=false #if you want to disable the auto updater
```

</details>

### Starting the bot

Docker image from: https://github.com/DRSchlaubi/mikmusic/pkgs/container/mikmusic%2Fbot

- Clone this repo
- Run `docker-compose up -d`

# Binary repositories

The bot has two repositories for binaries the [binary-repo](https://storage.googleapis.com/mikbot-binaries) containing
the bots binaries and the [plugin-repo](https://storage.googleapis.com/mikbot-plugins) 
([index](https://storage.googleapis.com/mikbot-plugins/plugins.json)) normally you should not need to interact with 
these repositories directly.

# For developers

JDK is required it can be obtained [here](https://adoptium.net) (Recommended for Windows but works everywhere)
and [here](https://sdkman.io/) (Recommended for Linux/Mac)

Please set the `ENVIRONMENT` env var to `DEVELOPMENT` whilst developing the bot.
Also set a `TEST_GUILD` environment variable, for local commands

If you are making any changes to the bots official plugins (aka the plugins in this repo),
please run the `rebuild-plugin-dependency-list.sh` script first, otherwise your plugins won't be loaded properly
