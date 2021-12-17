# VoteBot

Do polls on your server using Discord buttons

[![Discord Bots](https://top.gg/api/widget/servers/569936566965764126.svg)(https://top.gg/bot/569936566965764126)

**Invite Public Instance Now**: https://look-at.it/votebot
![Demo Video](https://cdn.discordapp.com/attachments/694999866132135996/919396792175579166/vote_bot_update_1.gif)

# Self-hosting

You can host this bot yourself if you want to

- Install the Pie chart service
  using [Docker](https://github.com/Votebot/piechart-service/pkgs/container/piechart-service)
    - Alternately you can use `https://pie-chart-service.nc-01.votebot.space/`

In order to do that, please follow [this](../README.md#starting-the-bot) and set these additional environment variables

```text
DOWNLOAD_PLUGINS=votebot
PIE_CHART_SERVICE_URL=https://pie-chart-service.nc-01.votebot.space/
```

# GitHub rate limit mitigations in /info

The /info commands requests all contributors through the GitHub API, which can hit rate limits, on order to mitigate
that, you can add your
own [personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
using this environment variables. The token doesn't need any specific scopes.

```text
GITHUB_TOKEN=
GITHUB_USERNAME=
```

# For Developers

Please read the [Mikbot developer section](../README.md#for-developers))
