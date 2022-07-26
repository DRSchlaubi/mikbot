# verification-system

Plugin requiring each invite of the bot to be manually confirmed by an owner

**Requirement:** This plugin depends on [ktor](../ktor)

## Configuration

This plugin adds the following new env variables.

```
WEB_SERVER_URL=<The webserver>
DISCORD_CLIENT_ID=<Discord client id>
DISCORD_CLIENT_SECRET=<Discord client secret (NOT BOT TOKEN)>
WEB_SERVER_HOST=0.0.0.0
```

# Setup

If you want to run a "public" instance of this bot, but limit the people who can use it, you can turn on a verification
mode by installing this plugin

### Intial Guild

To add your initial guild set the `OWNER_GUILD` variable and look for this log message

```
2022-07-26 15:38:57.659 [DefaultDispatcher-worker-3] INFO  d.s.m.u.verification.EventListeners - INVITE YOUR BOT HERE: https://schlaubi.cool/invitations/62dfed39c21b0c07cafbeb2c/accept
```

### OAuth2 Grant

Set require oauth 2 grant to true in the Discord bot settings
![](https://rice.by.devs-from.asia/TEzu3/kaqOkeCu74.png)

**Note** It's recommended to setup a [reverse proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)
for the verification server

