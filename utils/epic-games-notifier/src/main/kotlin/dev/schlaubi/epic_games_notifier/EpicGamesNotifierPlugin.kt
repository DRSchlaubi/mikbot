package dev.schlaubi.epic_games_notifier

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import org.litote.kmongo.`in`
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours

private const val googleLogo =
    "https://lh3.googleusercontent.com/COxitqgJr1sJnIDe8-jiKhxDx1FrYbtRHKJ9z_hELisAlapwE9LUPh6fcXIfb5vwpbMl4xl9H9TRFPc5NOO8Sb3VSgIBrfRYvW6cUA"

private val LOG = KotlinLogging.logger { }

class EpicGamesNotifierModule : Extension(), CoroutineScope {
    override val name: String = "epic-games-notifier"
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    override suspend fun setup() = startLoop()

    private fun startLoop() {
        launch {
            checkForGames()
            delay(5.hours)
            startLoop()
        }
    }

    private suspend fun checkForGames() {
        val games = HttpRequests.fetchFreeGames().map { it to it.id }
        val gameIds = games.map { (_, id) -> id }
        val failedWebhooks = mutableListOf<Snowflake>()

        WebhookDatabase.webhooks.find()
            .toFlow()
            .onEach {
                try {
                    it.sendGames(
                        kord,
                        games
                            .asSequence()
                            .filter { (_, id) -> id !in it.sentPromotions }
                            .filterNot { (game) -> game.urlSlug.endsWith("mysterygame") }
                            .map { (game) -> game.toEmbed() }
                            .toList(),
                        gameIds
                    )
                } catch (e: Exception) { // ContextException is private :(
                    failedWebhooks += it.id
                    LOG.warn(e) { "Could not call webhook ${it.id}" }
                }
            }.launchIn(this)

        WebhookDatabase.webhooks.deleteMany(Webhook::id `in` failedWebhooks)
    }

    override suspend fun unload() {
        coroutineScope { cancel() }
    }
}

@PluginMain
class EpicGamesNotifierPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::EpicGamesNotifierModule)
    }
}

suspend fun Webhook.sendGames(kord: Kord, games: List<EmbedBuilder>, gameIds: List<String>) {
    if (games.isEmpty()) return

    kord.rest.webhook.executeWebhook(id, token) {
        embeds.addAll(games)

        username = "Epic Games Reporter"
        avatarUrl = googleLogo
    }

    WebhookDatabase.webhooks.save(copy(sentPromotions = (sentPromotions + gameIds).distinct()))
}
