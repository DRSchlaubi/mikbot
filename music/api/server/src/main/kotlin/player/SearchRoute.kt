package dev.schlaubi.mikmusic.api.player

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExContext
import dev.schlaubi.lavakord.plugins.lavasearch.rest.search
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikmusic.api.documentation.documentedGet
import dev.schlaubi.mikmusic.api.types.Routes
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRoute() {
    val bot by KordExContext.get().inject<ExtensibleBot>()
    val lavalinkManager by bot.extension<LavalinkManager>()

    documentedGet<Routes.Search> { (query) ->
        call.respond(lavalinkManager.newNode().search(query))
    }
}
