# profiles

This module adds a profile system with connectable social media accounts and pronouns.

# Discord (required for all)

- Go to you Discord Application
- Go to OAuth -> General
- Add this redirect url: `<value of WEB_SERVER_URL>/profiles/social/callback`
- Set these environment variables to what the Twitch website shows you:

```
DISCORD_CLIENT_ID=<>
DISCORD_CLIENT_SECRET=<>
```

# GitHub

<details>
<summary>GitHub</summary>

- Create a new app [here](https://github.com/settings/applications/new)
  or if you want to use an organization `https://github.com/organizations/<organization name>/settings/applications/new`
    - Name: Whatever you want
    - Homepage URL: Whatever you want (or https://github.com/DRSchlaubi/mikbot)
    - Callback URL: `<value of WEB_SERVER_URL>/profiles/social/connect/github`
- Set these environment variables to what the Twitch website shows you:

```
GITHUB_CLIENT_ID=<>
GITHUB_CLIENT_SECRET=<>
```

</details>

# GitLab

<details>
<summary>GitLab</summary>

- Create a new Application here: https://gitlab.com/-/profile/applications
    - Name: Whatever you want
    - Redirect URL`<value of WEB_SERVER_URL>/profiles/social/connect/gitlab`
    - Scopes: `read_user`
    - Confidential: `Checked`
    - Enable Access Tokens: `Checked`
- Set these environment variables to what the Twitch website shows you:

```properties
GITLAB_CLIENT_ID=<>
GITABB_CLIENT_SECRET=<>
```

</details>

# Twitch

<details>
<summary>Twitch</summary>

- Go to https://dev.twitch.tv/console/apps/create
    - Name: Whatever you want
    - Redirect URI: `<value of WEB_SERVER_URL>/profiles/social/connect/twitch`
    - Category: Whatever you want (I use Application Integration)
- Set these environment variables to what the Twitch website shows you:

```
TWITCH_CLIENT_ID
TWITCH_CLIENT_SECRET
```

</details>

# Twitter

<details>
<summary>Twitter</summary>

(they didn't give me access to their dev portal so I can just tell you this)
Get these environment variables from here: https://developer.twitter.com/en/portal/dashboard

Callback: `<value of WEB_SERVER_URL>/profiles/social/connect/twitter`

```
TWITTER_CONSUMER_KEY
TWITTER_CONSUMER_SECRET
```

</details>
