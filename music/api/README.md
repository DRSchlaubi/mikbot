# api

This offers a REST API for interacting with the music module.

## Documentation

Rest endpoints are documented here: https://musikus.gutikus.schlau.bi/docs

### WebSocket API

A WebSocket is available to receive real time events at: `wss://host/music/events`

#### Events

#### Event Structure

| Name       | Type                      | Description                           | For type                           |
|------------|---------------------------|---------------------------------------|------------------------------------|
| `guild_id` | snowflake                 | the id of the guild the event is for  | All                                |
| `type`     | [event type](#event-type) | the type of the event                 | All                                |
| `queue`    | list of [queued tracks]() | the queue of the player               | `player_update` and `queue_update` |
| `state`    | [player state]()          | the state of the player               | `player_update`                    |
| `state`    | [voice_state]()           | the voice state of the logged in user | `voice_state_update`               |

#### Event type

The following event types exist:

| Name                 | Description                                                   |
|----------------------|---------------------------------------------------------------|
| `player_update`      | Sent when the player updates (currentTrack, volume, position) |
| `queue_update`       | Sent when the queue gets updated                              |
| `voice_state_update` | Sent when the bots or the users voice state changes           |
