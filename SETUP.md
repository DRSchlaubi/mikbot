# Mikbot Set up Guide

## Table of Content

- [Requirements](#requirements)
- [Creating a docker-compose file](#creating-a-docker-compose-file)
- [Creating and Configuring .env file](#create-and-configure-env-file)
- [Launching the Bot](#launching-the-bot)
- [Optional Steps](#optional-steps)
  - [Setting an Activity](#setting-an-activity)
  - [Setting up Mikmusic](#setting-up-mikmusic)

## Requirements

This setup guide will require you to have a linux environment with [Docker](https://www.docker.com/) and [docker-compose](https://github.com/docker/compose). \
See how to install [Docker](https://www.docker.com/) and [docker-compose](https://github.com/docker/compose), [here](https://docs.docker.com/engine/install/)

### Creating a docker-compose file

In your home directory create a new folder with the name of your choice, I will be calling mine `music-bot`, and `cd` into it
```bash
mkdir ~/music-bot
cd ~/music-bot
```

now create a new file called `docker-compose.yml` and use your favourite editor to edit this file, I will be using nano
```bash
touch docker-compose.yml
nano docker-compose.yml
```

paste the following into that file
```yaml
version: '3.8'

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
volumes:
  mongo-data: { }
```

Save the file and close the editor. 

*If you also used `nano`, like me, press `Ctrl + O` and `Enter` to save. Now Press `Ctrl + X` to close the editor*

### Create and Configure .env file

In the same directory of the `docker-compose.yml` create a new file called `.env` and open it.
```bash
touch .env
nano .env
```

paste the following into the file and change the values to your needs (read the comments) 

```env
ENVIRONMENT=PRODUCTION # Change this to DEVELOPMENT while you try out the bot, in PRODUCTION mode commands will take up to 1 hour to show up due to a limitation from discord 

SENTRY_TOKEN=<> # You can obtain a token from https://sentry.io/
DISCORD_TOKEN=<> # Put your discord token from https://discord.com/developers/applications

MONGO_URL=mongodb://bot:bot@mongo # Only change this if you know what you're doing
MONGO_DATABASE=bot_prod # Only change this if you know what you're doing

LOG_LEVEL=DEBUG # You can also set this to INFO, for example

# Uncomment this if you are using ENVIRONMENT=DEVELOPMENT, slash commands will only be registered on this server. Remove when switching to PRODUCTION
# TEST_GUILD=<> # Put the ID Discord Server or Test Server here
BOT_OWNERS=<> # Put your Discord ID here
OWNER_GUILD=<> # Put the ID of your Discord Server or Test Server here
```

## Launching the Bot

After you configured the Bot to your wanting run this command to start the bot

```bash
docker-compose up -d
```

To attach to the log output run 

```bash
docker-compose up
```

To stop the bot use 

```bash
docker-compose down
```

# Optional Steps

## Setting an Activity

To set an activity you have to install the `game-animator` plugin from the official plugins repository. \
Add the following variables to your `.env` file. 

Add the following Repository to your `PLUGIN_REPOSITORIES` variable if it's not already there (or add the variable if you don't already have it), seperated by a `,`: `https://storage.googleapis.com/mikbot-plugins/` 

Add the following plugin to your `DOWNLOAD_PLUGINS` variable (or add the variable if you don't already have it), seperated by a `,`: `game-animator`
 
Then add the following

```env
GAMES=<> # Put your activities here 
```

You can find the supported values for the `GAMES` [here](https://github.com/StckOverflw/mikbot/tree/main/core/game-animator)

## Setting up Mikmusic

If you want to host Mikmusic yourself add the following values to your `.env` file

Add the following Repository to your `PLUGIN_REPOSITORIES` variable if it's not already there (or add the variable if you don't already have it), seperated by a `,`: `https://storage.googleapis.com/mikbot-plugins/`

Add the following plugins to your `DOWNLOAD_PLUGINS` variable (or add the variable if you don't already have it), seperated by a `,`: `music` and `gdpr,database-i18n` if you don't have them. Optionally you can add `music-quiz` for the Music Quiz Plugin

```env
# Used to fetch more information about YouTube video, like thumbnails and descriptions (for chapters)
YOUTUBE_API_KEY=<>

# Required for Spotify support
SPOTIFY_CLIENT_SECRET=<>
SPOTIFY_CLIENT_ID=<>

HAPPI_KEY=<token from happi.dev for lyrics>
```
