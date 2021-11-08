# Mik Bot

A modular framework for building Discord bots in [Kotlin](https://kotlinlang.org)
using [Kordex](https://github.com/Kord-Extensions/kord-extensions/) and [Kord](https://github.com/kordlib)

**If you are here for mikmusic, click [here](music) and [there](mikmusic-bot)

## Deployment

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
```

</details>

### Starting the bot

Docker image from: https://github.com/DRSchlaubi/mikmusic/pkgs/container/mikmusic%2Fbot

- Clone this repo
- Run `docker-compose up -d`

# For developers

JDK is required it can be obtained [here](https://adoptium.net) (Recommended for Windows but works everywhere)
and [here](https://sdkman.io/) (Recommended for Linux/Mac)

**Currently you can only use JDK 16, we will migrate to JDK 17 as soon as Kotlin 1.6 becomes stable**
Please set the `ENVIRONMENT` env var to `DEVELOPMENT` whilst developing the bot.
Also set a `TEST_GUILD` environment variable, for local commands
