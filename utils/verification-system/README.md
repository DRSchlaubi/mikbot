# verification-system

Plugin requiring each invite of the bot to be manually confirmed by an owner

## Configuration

This plugin adds the following new env variables.

```
VERIFY_SERVER_URL=<The webserver>
VERIFY_CLIENT_ID=<Discord client id>
VERIFY_CLIENT_SECRET=<Discord client secret (NOT BOT TOKEN)>
VERIFY_SERVER_HOST=0.0.0.0
```

# Setup

If you want to run a "public" instance of this bot, but limit the people who can use it you can turn on verification
mode by installing this plugin

### OAuth2 Grant

Set require oauth 2 grant to true in the Discord bot settings
![](https://rice.by.devs-from.asia/TEzu3/kaqOkeCu74.png)

**Note** It's recommended to setup a [reverse proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)
for the verification server
