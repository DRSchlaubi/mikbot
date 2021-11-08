# redeploy-hook
Plugin adding a /redeploy command, backed by a webhook

## Configuration
This plugin adds the following new env variables.
```shell
REDEPLOY_HOST=<>
REDEPLOY_TOKEN=<>
```

### Setup

Section inspired by [Devcordbot](https://github.com/devcordde/DevcordBot)

Service installation: https://github.com/adnanh/webhook#installation

Env vars `REDEPLOY_HOST`,`REDEPLOY_TOKEN`,`OWNER_GUILD` and `BOT_OWNERS` need to be set

hooks.json

```json
{
  "id": "redeploy-mikmusic",
  "execute-command": "/usr/bin/sh",
  "pass-arguments-to-command": [
    {
      "source": "string",
      "name": "/path/to/mikmusic/redeploy.sh"
    }
  ],
  "command-working-directory": "/path/to/mikmusic",
  "trigger-rule": {
    "match": {
      "type": "value",
      "value": "YOUR_SECRET_TOKEN",
      "parameter": {
        "source": "header",
        "name": "Redeploy-Token"
      }
    }
  }
}
```

redeploy.sh

```shell
#!/usr/bin/env sh
docker-compose pull && docker-compose up -d
```
