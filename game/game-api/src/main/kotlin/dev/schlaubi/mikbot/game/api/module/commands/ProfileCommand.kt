package dev.schlaubi.mikbot.game.api.module.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.game.api.GameStats
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.setGameApiBundle
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import org.litote.kmongo.div
import org.litote.kmongo.gt

class UnoProfileArguments : Arguments() {
    val target by optionalUser {
        name = "user"
        description = "commands.profile.arguments.user.description"
    }
}

/**
 * Adds a /profile command to this [profileCommand].
 */
suspend fun GameModule<*, *>.profileCommand() = publicSubCommand(::UnoProfileArguments) {
    setGameApiBundle()
    name = "profile"
    description = "Shows a users profile"

    action {
        val target = arguments.target ?: user

        val stats = gameStats.findOneById(target.id)?.stats

        if (stats == null) {
            respond {
                content = translateGlobal("commands.profile.profile.empty")
            }
            return@action
        }

        respond {
            val author = this@profileCommand.kord.getUser(user.id)
            embed {
                author {
                    name = author?.username ?: "<crazy person>"
                    icon = author?.effectiveAvatar
                }

                field {
                    name = translateGlobal("commands.profile.wins")
                    value = stats.wins.toString()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.losses")
                    value = stats.losses.toString()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.ratio")
                    value = stats.ratio.formatPercentage()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.played")
                    value = (stats.wins + stats.losses).toString()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.rank")
                    val otherPlayerCount = gameStats.countDocuments(UserGameStats::stats / GameStats::ratio gt 0.0)
                    value = (otherPlayerCount + 1).toString()
                    inline = true
                }
            }
        }
    }
}
