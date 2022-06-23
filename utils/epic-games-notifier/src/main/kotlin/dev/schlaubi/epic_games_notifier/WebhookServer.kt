package dev.schlaubi.epic_games_notifier

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import org.pf4j.Extension

@Serializable
@Resource("/webhooks")
class WebhookRoute {
    @Serializable
    @Resource("/thanks")
    data class Thanks(val code: String, val guild_id: String, val parent: WebhookRoute)
}

@Extension
class EpicGamesNotifierWebhookExtension : KtorExtensionPoint, KordExKoinComponent {
    private val kord by inject<Kord>()

    override fun Application.apply() {
        routing {
            get<WebhookRoute.Thanks> { (code) ->
                val webhook = HttpRequests.discordAuthorize(code)
                val obj = Webhook(webhook.id, emptyList(), webhook.token)
                WebhookDatabase.webhooks.insertOne(obj)

                val freeGames = HttpRequests.fetchFreeGames()
                val games = freeGames.map(Game::toEmbed)
                val gameIds = freeGames.map(Game::id)

                obj.sendGames(kord, games, gameIds)

                call.respond("Thanks for adding the Webhook and remember please don't buy Apple products. I AM SERIOUS!!")
            }
        }
    }
}
