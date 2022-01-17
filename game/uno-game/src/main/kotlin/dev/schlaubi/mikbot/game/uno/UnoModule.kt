package dev.schlaubi.mikbot.game.uno

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.mikbot.game.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.litote.kmongo.coroutine.CoroutineCollection

class UnoArguments : Arguments() {
    val extreme by defaultingBoolean {
        name = "extreme"
        description = "Extreme mode (65% of the times, you draw 0 cards, but you can draw up to 6 cards at once)"
        defaultValue = false
    }
    val flash by defaultingBoolean {
        name = "flash"
        description = "Flash mode (Player order is completely random)"
        defaultValue = false
    }
    val dropIns by defaultingBoolean {
        name = "drop_ins"
        description = "Enable or disable drop-ins (Default: disabled)"
        defaultValue = false
    }
}

class UnoModule : GameModule<DiscordUnoPlayer, DiscordUnoGame>() {
    override val name: String = "uno"
    override val bundle: String = "uno"
    override val commandName: String = "uno"

    override val gameStats: CoroutineCollection<UserGameStats> = database.getCollection("uno_stats")

    @OptIn(PrivilegedIntent::class, KordUnsafe::class, KordExperimental::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMembers)

        startGameCommand(
            "uno.game.title",
            "uno-game",
            ::UnoArguments,
            { welcomeMessage, thread ->
                DiscordUnoGame(
                    user, this@UnoModule, welcomeMessage, thread, translationsProvider,
                    arguments.extreme, arguments.flash, arguments.dropIns
                )
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
    }
}
