# music (formerly mikmusic)

Plugin providing full music functionality for the bot

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
- i18n
- Ability to skip through [YouTube Chapters](https://support.google.com/youtube/answer/9884579?hl=en) with `/next`
- Song guizes (Inspired by the [AirConsole](https://www.airconsole.com)
  game [MusicGuess](https://www.airconsole.com/play/battle-games/musicguess)) (now housed [here](../game/music-quiz))
- Embed song cover color using [image-color-service](https://github.com/mikbot/image-color-service)

### Requirements

Adding this plugin adds the following requirements:

- [Youtube Data API key](https://console.cloud.google.com/apis/api/youtube/overview)
- [Spotify API application](https://developer.spotify.com/dashboard/applications)
- [One Lavalink instance](https://github.com/freyacodes/lavalink#server-configuration)
- [Happi.dev API key (For lyrics)](https://happi.dev/panel)
- [image-color-service](https://github.com/mikbot/image-color-service)

**Required permissions**: [`328576854080`](https://finitereality.github.io/permissions-calculator/?v=-2135627712)

## Configuration

This plugin adds the following new env variables.

```shell
// Used to fetch more information about YouTube video, like thumbnails and descriptions (for chapters)
YOUTUBE_API_KEY=<>

// Required for Spotify support
SPOTIFY_CLIENT_SECRET=<>
SPOTIFY_CLIENT_ID=<>

HAPPI_KEY=<token from happi.dev for lyrics>

IMAGE_COLOR_SERVICE_URL=<image color service url>
```

## Development

You can install [image-color-service](https://github.com/mikbot/image-color-service) for development via [Crates.io](https://crates.io).

```
cargo install image-color-service
image-color-service
```

Or use docker-compose in this directory:

```
docker-compose up -d
```

# Plugins

If you want to deploy this Bot I recommend these plugins

```shell
DOWNLOAD_PLUGINS=gdpr,database-i18n,game-animator,music,music-quiz
```

I personally run my instance with these plugins

```shell
DOWNLOAD_PLUGINS=redeploy-hook,gdpr,database-i18n,game-animator,music,game-api,music-quiz,uno-game,ktor,verification-system,epic-games-notifier
```

For additional configuration please check [redeploy-hook](../core/redeploy-hook)
and [verification-system](../utils/verification-system)

# Setup

For a full Setup guide, if you don't know how to set up mikbot take a look at [this](../SETUP.md) and view the "Setting
up Mikmusic" section of the document under the Optional Steps

Using this plugin requires additional first-time setup

Run this commands before running `docker-compose up -d`

- Run `docker-compose up -d mongo`
- Run `docker-compose exec mongo mongo -u bot -p bot`
- Run this mongo shell commands

```mongo
use bot_prod
db.createCollection("lavalink_servers")
db.lavalink_servers.insertOne({"url": "wss://...", "password": "<password>"})
```

# Redeploy

This plugin supports a /redeploy command restarting player sessions after updating (click [here](../core/redeploy-hook)
for more)
