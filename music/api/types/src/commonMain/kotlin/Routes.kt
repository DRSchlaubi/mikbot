package dev.schlaubi.mikmusic.api.types

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import io.ktor.resources.*
import kotlinx.serialization.SerialName

@Resource("/music")
class Routes {
    @Resource("auth")
    data class Auth(val parent: Routes = Routes()) {
        @Post(
            HttpVerb(
                description = "Exchanges a Discord auth code for an access token",
                summary = "Exchange auth code",
                auth = "",
                response = HttpVerb.HttpBody(
                    description = "Discord deemed the provided code invalid",
                    body = OAuth2AccessTokenResponse::class
                ),
                request = HttpVerb.HttpBody(
                    description = "OAuth 2 token request",
                    body = OAuth2TokenRequest::class,
                    mediaType = "application/x-www-form-urlencoded"
                ),
                errors = [
                    HttpVerb.HttpBody(
                        "Discord deemed the provided code invalid",
                        status = 401
                    )
                ]
            )
        )
        @Resource("token")
        data class Token(val parent: Auth = Auth())

        @Post(
            HttpVerb(
                description = "Requests user authorization through Discord",
                summary = "Request user authorization",
                auth = "",
                response = HttpVerb.HttpBody(
                    description = "Redirect to the authorization page",
                    status = 302,
                    body = Nothing::class
                )
            )
        )
        @Resource("authorize")
        data class Authorize(
            @Description("The OAuth2 state")
            val state: String,
            @Description("The uri to redirect to after the request finished")
            @SerialName("redirect_uri")
            val redirectUri: String,
            @Description("The OAuth2 response type")
            @SerialName("response_type")
            val responseType: String,
            val parent: Auth = Auth(),
        )

        @Post(
            HttpVerb(
                description = "Refreshes an expired access token",
                summary = "Refresh expired token",
                response = HttpVerb.HttpBody(
                    description = "Token refreshed successfully",
                    body = OAuth2AccessTokenResponse::class
                ),
                errors = [
                    HttpVerb.HttpBody(
                        "Discord deemed the provided code invalid",
                        status = 401
                    )
                ]
            )
        )
        @Resource("refresh")
        data class Refresh(
            @SerialName("refresh_token")
            val refreshToken: String,
            val parent: Auth = Auth(),
        )

        @Get(
            HttpVerb(
                description = "Retrieve data about the authenticated user",
                summary = "Get authenticated user",
                response = HttpVerb.HttpBody(
                    description = "The authenticated user",
                    body = DiscordUser::class
                ),
                errors = [
                    HttpVerb.HttpBody(
                        "No Authentication provided",
                        status = 401
                    ),
                    HttpVerb.HttpBody(
                        "User Not found",
                        status = 404
                    )
                ]
            )
        )
        @Resource("@me")
        data class UserProfile(val parent: Auth = Auth())
    }

    @Get(
        HttpVerb(
            description = "Performs a search for new track",
            summary = "Search for tracks",
            response = HttpVerb.HttpBody(
                description = "The found tracks",
                body = LoadResult.SearchResult::class
            ), errors = [
                HttpVerb.HttpBody(
                    "No Authentication provided",
                    status = 401
                )
            ]
        )
    )
    @Resource("search")
    data class Search(
        @Description("The query to search for")
        val query: String,
        val parent: Routes = Routes(),
    )

    @Get(
        HttpVerb(
            description = "Retrieves all possible players the current user has access to",
            summary = "Get all players",
            response = HttpVerb.HttpBody(
                description = "All players",
                body = List::class,
                typeParameters = [PlayerState::class]
            ), errors = [
                HttpVerb.HttpBody(
                    "No Authentication provided",
                    status = 401
                )
            ]
        )
    )
    @Resource("players")
    data class Players(val parent: Routes = Routes()) {
        @Get(
            HttpVerb(
                description = "Retrieves the requested player by its id",
                summary = "Get player",
                response = HttpVerb.HttpBody(
                    description = "The specified player",
                    body = PlayerState::class,
                ), errors = [
                    HttpVerb.HttpBody(
                        "Player not available",
                        status = 404
                    ),
                    HttpVerb.HttpBody(
                        "No Authentication provided",
                        status = 401
                    )
                ]
            )
        )
        @Patch(
            HttpVerb(
                description = "Updates the current player state, all fields are optional, ommitted fields will be left unchanged",
                summary = "Update player",
                request = HttpVerb.HttpBody(
                    description = "The new player state",
                    body = UpdatablePlayerState::class
                ),
                response = HttpVerb.HttpBody(
                    status = 202,
                    description = "Player updated",
                ), errors = [
                    HttpVerb.HttpBody(
                        "No Authentication provided",
                        status = 401
                    ),
                    HttpVerb.HttpBody(
                        "User is not allowed to interact with this player",
                        status = 403
                    ),
                ]
            )
        )
        @Delete(
            HttpVerb(
                description = "Deletes the current player (equivalent to /stop)",
                summary = "Update player",
                response = HttpVerb.HttpBody(
                    status = 202,
                    description = "Player updated",
                ), errors = [
                    HttpVerb.HttpBody(
                        "No Authentication provided",
                        status = 401
                    ),
                    HttpVerb.HttpBody(
                        "User is not allowed to interact with this player",
                        status = 403
                    ),
                ]
            )
        )
        @Resource("{guildId}")
        data class Specific(
            @Description("The id of the players guild") val guildId: Snowflake,
            val parent: Players = Players(),
        ) {

            @Get(
                HttpVerb(
                    description = "Returns the current queue",
                    summary = "Get queue",
                    response = HttpVerb.HttpBody(
                        "The current queue",
                        body = List::class,
                        typeParameters = [APIQueuedTrack::class]
                    ), errors = [
                        HttpVerb.HttpBody(
                            "No Authentication provided",
                            status = 401
                        ),
                        HttpVerb.HttpBody(
                            "User is not allowed to interact with this player",
                            status = 403
                        ),
                    ]
                )
            )
            @Put(
                HttpVerb(
                    description = "Adds new tracks to the queue",
                    summary = "Update queue",
                    response = HttpVerb.HttpBody(
                        "The updated Queue",
                        body = List::class,
                        typeParameters = [APIQueuedTrack::class]
                    ),
                    request = HttpVerb.HttpBody(
                        description = "The tracks to add",
                        body = QueueAddRequest::class
                    ),
                    errors = [
                        HttpVerb.HttpBody(
                            "No Authentication provided",
                            status = 401
                        ),
                        HttpVerb.HttpBody(
                            "User is not allowed to interact with this player",
                            status = 403
                        ),
                        HttpVerb.HttpBody(
                            "If the scheduler options contain more than one value being true",
                            status = 400
                        ),
                    ]
                )
            )
            @Delete(
                HttpVerb(
                    description = "Removes tracks from the queue",
                    summary = "Update queue",
                    response = HttpVerb.HttpBody(
                        "The updated Queue",
                        body = List::class,
                        typeParameters = [APIQueuedTrack::class]
                    ),
                    request = HttpVerb.HttpBody(
                        description = "The tracks to remove",
                        body = QueueRemoveRequest::class
                    ),
                    errors = [
                        HttpVerb.HttpBody(
                            "No Authentication provided",
                            status = 401
                        ),
                        HttpVerb.HttpBody(
                            "User is not allowed to interact with this player",
                            status = 403
                        ),
                        HttpVerb.HttpBody(
                            "If the specified range is invalid",
                            status = 400
                        ),
                    ]
                )
            )
            @Resource("queue")
            class Queue(val parent: Specific) {
                constructor(guildId: Snowflake) : this(Specific(guildId))

                operator fun component1() = parent.guildId
            }

            @Get(
                HttpVerb(
                    description = "Returns the channels that are available on this guild",
                    summary = "Get available channels",
                    response = HttpVerb.HttpBody(
                        "The current player state",
                        body = List::class,
                        typeParameters = [Channel::class]
                    ), errors = [
                        HttpVerb.HttpBody(
                            "No Authentication provided",
                            status = 401
                        ),
                        HttpVerb.HttpBody(
                            "User is not allowed to interact with this player",
                            status = 403
                        ),
                    ]
                )
            )
            @Resource("available-channels")
            class AvailableChannels(val parent: Specific) {
                constructor(guildId: Snowflake) : this(Specific(guildId))

                operator fun component1() = parent.guildId
            }
        }
    }

    @Resource("events")
    data class Events(@SerialName("api_key") val apiKey: String, val parent: Routes = Routes())
}
