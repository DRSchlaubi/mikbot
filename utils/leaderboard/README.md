# leaderboard

Simple leaderboard

# REST API

This plugin has a very simple REST API if the [ktor](../ktor) plugin is installed.

# GET /leaderboard/{guild_id}

```json5
{
  "guild_name": "Cool Guild",
  // String
  "guild_icon": "https://cdn.discord.com/blablalba.webp",
  // String?
  "members": [
    {
      "user_id": 12345, // Snowflake
      "points": 321, // Long
      "level": 2, // Int
      "last_xp_received": "2022-02-20T13:55:19+0000", // Instant (ISO 1601 timestamp)
      "avatar_url": "https://cdn.discord.com/blablabla" // String
    }
  ]
}
```
