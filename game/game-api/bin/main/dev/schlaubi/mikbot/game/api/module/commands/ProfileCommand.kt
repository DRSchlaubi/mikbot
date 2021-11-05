package dev.schlaubi.mikbot.game.api.module.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.game.api.GameStats
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import org.litote.kmongo.div
import org.litote.kmongo.gt

class UnoProfileArguments : Arguments() {
    val target by optionalUser("user", "The user you want to see the profile of")
}

/**
 * Adds a /profile command to this [profileCommand].
 */
@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun GameModule<*, *>.profileCommand() = publicSubCommand(::UnoProfileArguments) {
    name = "profile"
    description = "Shows a users profile"

    action {
        val target = arguments.target ?: user

        val botUser = database.users.findUser(target)

        if (botUser.unoStats == null) {
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
                    value = botUser.unoStats.wins.toString()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.losses")
                    value = botUser.unoStats.losses.toString()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.ratio")
                    value = botUser.unoStats.ratio.formatPercentage()
                    inline = true
                }

                field {
                    name = translateGlobal("commands.profile.played")
                    value = (botUser.unoStats.wins + botUser.unoStats.losses).toString()
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
