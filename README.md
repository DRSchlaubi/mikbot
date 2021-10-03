# Mik Music

Mik's cool self-hosted ~~music~~-bot.

## Disclaimer

For legal reasons: There is **no** official public instance of this bot, so don't bother asking

# Platform Support

- All native Lavaplayer platforms
- Spotify

# Features

- slash-commands for everything
- Queue songs from context actions (Also supports Discord file
  uploads) ([DEMO](https://rice.by.devs-from.asia/TEzu3/tUVeLizo46.png))
- Music channel (
  Like [Hydra](https://hydra.bot)) ([DEMO](https://cdn.discordapp.com/attachments/890344418320719973/891144736151318568/music_channel_demo.gif))
- Bot internal playlist system (Supports import from other sources, just
  do `/playlist add name: <name> query: <existing yt/spotify playlist>`)
- i18n (although most strings are only English as of rn)
- Song guizes (Inpired by the [AirConsole](https://www.airconsole.com)
  game [MusicGuess](https://www.airconsole.com/play/battle-games/musicguess))
- UNO as a minigame - Just don't even ask why

## Deployment

### Requirements

- [Youtube Data API key](https://console.cloud.google.com/apis/api/youtube/overview)
- [Spotify API application](https://developer.spotify.com/dashboard/applications)
- [One Lavalink instance](https://github.com/freyacodes/lavalink#server-configuration)
- [Sentry](https://sentry.io) (Optional)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Guild Members Intent (Required to listen for Thread member updates in games)](https://discord.com/developers/docs/topics/gateway#privileged-intents)

**Required permissions**: [`328576854080`](https://finitereality.github.io/permissions-calculator/?v=-2135627712)

## Example Environment file

<details>
<summary>.env</summary>

```properties
ENVIRONMENT=PRODUCTION
SENTRY_TOKEN=<>
DISCORD_TOKEN=<>
GAMES=p: some funny games,w: unfunny funny compilations on YouTube,l: to silence,p: lästert über aktuelle Musik,p: lästert über aktuelle Musik,p: Würde lieber Justin Bieber hören,p: Würde lieber Justin Bieber hören
MONGO_URL=mongodb://bot:bot@mongo
MONGO_DATABASE=bot_prod
LOG_LEVEL=DEBUG
YOUTUBE_API_KEY=<>
SPOTIFY_CLIENT_SECRET=<>
SPOTIFY_CLIENT_ID=<>
REDEPLOY_HOST=<>
REDEPLOY_TOKEN=<>
BOT_OWNERS=416902379598774273
OWNER_GUILD=<>
```

</details>

### Starting the bot

Docker image from: https://github.com/DRSchlaubi/mikmusic/pkgs/container/mikmusic%2Fbot

- Clone this repo
- Run `docker-compose up -d mongo`
- Run `docker-compose exec mongo mongo -u bot -p bot`
- Run this mongo shell commands

```mongo
use bot_prod
db.createCollection("lavalink_servers")
db.lavalink_servers.insertOne({"url": "wss://...", "password": "<password>"})
``` 

- Run `docker-compose up -d`

### Redeployment

Section inspired by [Devcordbot](https://github.com/devcordde/DevcordBot)

This section describes how to properly setup `/redeploy`
Service installation: https://github.com/adnanh/webhook#installation

Env vars `REDEPLOY_HOST`,`REDEPLOY_TOKEN`,`OWNER_GUILD` and `BOT_OWNERS` need to be set

hooks.json

```json
{
  "id": "redeploy-mikmusic",
  "execute-command": "/usr/bin/sh",
  "pass-arguments-to-command": [
    {
      "source": "string",
      "name": "/path/to/mikmusic/redeploy.sh"
    }
  ],
  "command-working-directory": "/path/to/mikmusic",
  "trigger-rule": {
    "match": {
      "type": "value",
      "value": "YOUR_SECRET_TOKEN",
      "parameter": {
        "source": "header",
        "name": "Redeploy-Token"
      }
    }
  }
}
```

redeploy.sh

```shell
#!/usr/bin/env sh
docker-compose pull && docker-compose up -d
```

<details>
<summary>Guild verification</summary>

If you want to run a "public" instance of this bot, but limit the people who can use it you can turn on verification
mode

### .env changes

```
VERIFIED_MODE=true
VERIFY_SERVER_URL=<The webserver>
VERIFY_CLIENT_ID=<Discord client id>
VERIFY_CLIENT_SECRET=<Discord client secret (NOT BOT TOKEN)>
VERIFY_SERVER_HOST=0.0.0.0
```

</details>

### OAuth2 Grant

Set require oauth 2 grant to true in the Discord bot settings
![](https://rice.by.devs-from.asia/TEzu3/kaqOkeCu74.png)

**Note** It's recommended to setup a [reverse proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/) for the verification server

# For developers

JDK is required it can be obtained [here](https://adoptium.net) (Recommended for Windows but works everywhere)
and [here](https://sdkman.io/) (Recommended for Linux/Mac)

Please set the `ENVIRONMENT` env var to `DEVELOPMENT` whilst developing the bot
