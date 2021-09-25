# Mik Music

Mik's cool self-hosted music-bot.

## Disclaimer

For legal reasons: There is **no** official public instance of this bot, so don't bother asking

# Platform Support

- All native Lavaplayer platforms
- Spotify

# Features

- slash-commands for everything
- Queue songs from context actions ([DEMO](https://rice.by.devs-from.asia/TEzu3/tUVeLizo46.png))
- Music channel (
  Like [Hydra](https://hydra.bot)) ([DEMO](https://cdn.discordapp.com/attachments/890344418320719973/891144736151318568/music_channel_demo.gif))
- Bot internal playlist system (Supports import from other sources, just do `/playlist add name: <name> query: <existing yt/spotify playlist>`)
- i18n (although most strings are only English as of rn)

## Deployment

### Requirements

- [Youtube Data API key](https://console.cloud.google.com/apis/api/youtube/overview)
- [Spotify API application](https://developer.spotify.com/dashboard/applications)
- [One Lavalink instance](https://github.com/freyacodes/lavalink#server-configuration)
- [Sentry](https://sentry.io) (Optional)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Example Environment file

<details>
<summary>.env</summary>
  
```properties
  
SENTRY_TOKEN=<sentry>
DISCORD_TOKEN=<discord>
GAMES=p: some funny games,w: unfunny funny compilations on YouTube,l: to silence
# if you use the default docker-compose.yml
MONGO_URL=mongodb://bot:bot@mongo
MONGO_DATABASE=bot_prod

// See requirements YOUTUBE_API_KEY=<key>
SPOTIFY_CLIENT_SECRET=<key>
SPOTIFY_CLIENT_ID=<key>

// See redeployment REDEPLOY_HOST=<host>
REDEPLOY_TOKEN=<token>
// comma seperated id list 
BOT_OWNERS=416902379598774273

// command /redeploy is on 
OWNER_GUILD=694185276922134619

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

This secion describes how to properly setup `/redeploy`
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

# For developers
  JDK is required it can be obtained [here](https://adoptium.net) (Recommended for Windows but works everywhere) and [here](https://sdkman.io/) (Recommended for Linux/Mac)

Please set the `ENVIRONMENT` env var to `DEVELOPMENT` whilst developing the bot
