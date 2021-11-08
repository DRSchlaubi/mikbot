# music

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
- Ability to skip thorugh [YouTube Chapters](https://support.google.com/youtube/answer/9884579?hl=en) with `/next`
- Song guizes (Inpired by the [AirConsole](https://www.airconsole.com)
  game [MusicGuess](https://www.airconsole.com/play/battle-games/musicguess))

### Requirements

Adding this plugin adds the following requirements:

- [Youtube Data API key](https://console.cloud.google.com/apis/api/youtube/overview)
- [Spotify API application](https://developer.spotify.com/dashboard/applications)
- [One Lavalink instance](https://github.com/freyacodes/lavalink#server-configuration)
- [Happi.dev API key (For lyrics)](https://happi.dev/panel)

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
```

# Setup
Using this plugin requires addional first-time setup

Run this commands before running `docker-compose up -d`
- Run `docker-compose up -d mongo`
- Run `docker-compose exec mongo mongo -u bot -p bot`
- Run this mongo shell commands

```mongo
use bot_prod
db.createCollection("lavalink_servers")
db.lavalink_servers.insertOne({"url": "wss://...", "password": "<password>"})
``` 
