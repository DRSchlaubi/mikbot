# botblock

Plugin posting server stats to botlists using [BotBlock](https://botblock.org)

# Config
```
// minutes between updates
BOTBLOCK_DELAY=10
SUPPORTED_BOT_LISTS=top.gg,other-list
```

### Tokens
How to convert the bot list name, to the env var name:
- if it is a digit, keep it
- if it is a letter, capitalize it
- if it is something else use an `_`

-> `top.gg` becomes `TOP_GG`

For a list of all supported sites check [this](https://botblock.org/lists/)
