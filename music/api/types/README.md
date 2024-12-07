# music-api-types

Definitions of the mikmusic api

All bodies (requests and responses) are encoded as JSON

## Types

Types follow the Discord convention for nullability:

- Types ending with a `?` are nullable
- Names ending with a `?` are optional
- when the type and the name ends with a `?` a field is both optional and nullable

Lists are comma-seperated

## Authentication

[Discord OAuth](https://discord.com/developers/docs/topics/oauth2) is used to authenticate with the API, in order to
obtain an authentication token
an [auth_code](https://discord.com/developers/docs/topics/oauth2#authorization-code-grant)
should be sent to the [exchange endpoint](#post-musicauthexchange) after a token expired it can be refreshed using
the [refresh endpoint](#post-musicauthrefresh) unless the user revoked the applications access to their Discord account.

After a token is obtained, it should be provided using the `Authorization` header

```text
Authorization: {token_type} {access_token}
```

## Routes

### POST /music/auth/exchange

Exchanges a Discord auth_code for an access_token

#### Request

| Name   | Type   | Description                                                                                                       |
|--------|--------|-------------------------------------------------------------------------------------------------------------------|
| `code` | String | The [auth code](https://discord.com/developers/docs/topics/oauth2#authorization-code-grant) received from discord |

#### Response

200 OK

| Name            | Type   | Description                                                     |
|-----------------|--------|-----------------------------------------------------------------|
| `access_token`  | String | The access token for the api                                    |
| `token_type`    | String | Always `Bearer`                                                 |
| `expires_in`    | Int    | Duration in seconds until the token expires                     |
| `refresh_token` | String | Token to use for the [refresh endpoint](#post-musicauthrefresh) |

#### Error responses

- `401 Unauthorzied` If the provided token is invalid
- `400 Bad Request` if the provided body is invalid

### POST /music/auth/refresh

Refreshes an expired auth token

#### Request

| Name            | Type   | Description                                                                                                       |
|-----------------|--------|-------------------------------------------------------------------------------------------------------------------|
| `refresh_token` | String | The [auth code](https://discord.com/developers/docs/topics/oauth2#authorization-code-grant) received from discord |

#### Response

Identical to [Exchange Token](#post-musicauthexchange)

#### Error responses

- `401 Unauthorzied` If the provided refresh_token is invalid or Discord returned an error, in this case a new
  [Auth Flow](#authentication) should be started
- `400 Bad Request` if the provided body is invalid

### GET /music/search

Performs a search request (useful for autocomplete and generic search)

#### Parameters

| Name     | Type                                                                                 | Description                      |
|----------|--------------------------------------------------------------------------------------|----------------------------------|
| `query`  | String                                                                               | The query to search for          |
| `types`? | List<[SearchItemType](https://github.com/topi314/LavaSearch?tab=readme-ov-file#api)> | The kinds of items to search for |

#### Response

A [SearchResult](https://github.com/topi314/LavaSearch?tab=readme-ov-file#search-result-object) object

### GET /music/players

Returns a list of a players this user can control

#### Response

Returns a List of [Players]()

### GET /music/players/{guildId}
