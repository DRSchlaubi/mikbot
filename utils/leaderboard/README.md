# leaderboard

Simple leaderboard

# REST API

This plugin has a very simple REST API if the [ktor](../ktor) plugin is installed.

# GET /leaderboard/users/{userid_id}

```json5
{
  "data": {
    "809471441719787602": {
      // Snowflake
      "user_id": 416902379598774273,
      // String?
      "username": "Schlaubi",
      // String?
      "effective_name": "ỆᶍǍᶆṔƚÉ2ỆᶍǍᶆṔƚÉ áů     Юлия 3",
      // String?
      "discriminator": "0001",
      // Long
      "points": 24,
      // Int
      "level": 0,
      // Instant (ISO 1601 timestamp)
      "last_xp_received": "2022-06-11T14:34:36.409119750Z",
      // String?
      "avatar_url": "https://cdn.discordapp.com/avatars/416902379598774273/a_077d7d78c656ec51e6f8008c861cfda2.gif"
    }
  }
}

```

# GET /leaderboard/guilds/{guild_id}

the `members` array will be sorted by `points`

```json5
{
  "guild_name": "Server von Schlaubi",
  "guild_icon": null,
  "members": [
    {
      // Snowflake
      "user_id": 416902379598774273,
      // String?
      "username": "Schlaubi",
      // String?
      "effective_name": "ỆᶍǍᶆṔƚÉ2ỆᶍǍᶆṔƚÉ áů     Юлия 3",
      // String?
      "discriminator": "0001",
      // Long
      "points": 24,
      // Int
      "level": 0,
      // Instant (ISO 1601 timestamp)
      "last_xp_received": "2022-06-11T14:34:36.409119750Z",
      // String?
      "avatar_url": "https://cdn.discordapp.com/avatars/416902379598774273/a_077d7d78c656ec51e6f8008c861cfda2.gif"
    }
  ]
}
```
