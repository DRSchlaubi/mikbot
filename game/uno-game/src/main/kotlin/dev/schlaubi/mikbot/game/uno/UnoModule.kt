package dev.schlaubi.mikbot.game.uno

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
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
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.litote.kmongo.coroutine.CoroutineCollection

class UnoArguments : Arguments() {
    val extreme by defaultingBoolean {
        name = "extreme"
        description = "commands.start.arguments.extreme.description"
        defaultValue = false
    }
    val flash by defaultingBoolean {
        name = "flash"
        description = "commands.start.arguments.flash.description"
        defaultValue = false
    }
    val dropIns by defaultingBoolean {
        name = "drop_ins"
        description = "commands.start.arguments.drop_ins.description"
        defaultValue = false
    }
    val drawUntilPlayable by defaultingBoolean {
        name = "draw_until_playable"
        description = "commands.start.arguments.draw_until_playable.description"
        defaultValue = false
    }
    val forcePlay by defaultingBoolean {
        name = "force_play"
        description = "commands.start.arguments.force_play.description"
        defaultValue = false
    }

    val enableDrawCardStacking by defaultingBoolean {
        name = "card_stacking"
        description = "commands.start.arguments.card_stacking.description"
        defaultValue = true
    }

    val stackAllDrawingCards by defaultingBoolean {
        name = "stack_all_drawing_cards"
        description = "commands.start.arguments.stack_all_drawing_cards.description"
        defaultValue = false
    }

    val enableBluffing by defaultingBoolean {
        name = "bluffing"
        description = "commands.start.arguments.bluffing.description"
        defaultValue = false
    }

    val useSpecial7and0 by defaultingBoolean {
        name = "0-7"
        description = "commands.start.arguments.seven_and_zero.description"
        defaultValue = false
    }
}

class UnoModule : GameModule<DiscordUnoPlayer, DiscordUnoGame>() {
    override val name: String = "uno"
    override val bundle: String = "uno"
    override val commandName: String = "uno"

    override val gameStats: CoroutineCollection<UserGameStats> = database.getCollection("uno_stats")

    @OptIn(PrivilegedIntent::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMembers)
        intents.add(Intent.GuildPresences)

        startGameCommand(
            "uno.game.title",
            "uno-game",
            ::UnoArguments,
            {
                if (arguments.flash && arguments.useSpecial7and0) {
                    discordError(translate("commands.uno.start_game.special_7_and_0.incompatible.flash"))
                }
            },
            { _, welcomeMessage, thread ->
                DiscordUnoGame(
                    user, this@UnoModule, welcomeMessage, thread, translationsProvider,
                    arguments.extreme, arguments.flash, arguments.dropIns, arguments.drawUntilPlayable,
                    arguments.forcePlay, arguments.enableDrawCardStacking, arguments.stackAllDrawingCards,
                    arguments.enableBluffing, arguments.useSpecial7and0
                )
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
        bluffingCommand()
    }
}
