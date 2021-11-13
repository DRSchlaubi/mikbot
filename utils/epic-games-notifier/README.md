# epic-games-notifier

Adds a webhook that notifies users about every free games on [epicgames.com](https://www.epicgames.com)

**Requirement:** This plugin depends on [ktor](../ktor)

## Configuration
This plugin adds the following new env variables.
```shell
// country code for the epic games store pages
COUNTRY_CODE=de
```

# Configuration
Setup is the same as for [verification-system](../verification-system), with the exception being that the redirect url is `/webhooks/thanks`.
