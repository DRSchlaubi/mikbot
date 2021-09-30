package dev.schlaubi.musicbot.module.uno.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.core.io.findUser
import dev.schlaubi.musicbot.module.uno.UnoModule
import dev.schlaubi.musicbot.utils.database

class UnoProfileArguments : Arguments() {
    val target by optionalUser("user", "The user you want to see the profile of")
}

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun UnoModule.profileCommand() = publicSubCommand(::UnoProfileArguments) {
    name = "profile"
    description = "Shows a users profile"

    action {
        val target = arguments.target ?: user

        val user = database.users.findUser(target)

        if (user.unoStats == null) {
            respond {
                content = translate("commands.uno.profile.empty")
            }
            return@action
        }

        respond {
            val author = this@profileCommand.kord.getUser(user.id)
            embed {
                author {
                    name = author?.username ?: "<crazy person>"
                    icon = author?.avatar?.getUrl(Image.Size.Size256)
                }

                field {
                    name = translate("commands.uno.profile.wins")
                    value = user.unoStats.wins.toString()
                }

                field {
                    name = translate("commands.uno.profile.losses")
                    value = user.unoStats.losses.toString()
                    inline = true
                }

                field {
                    name = translate("commands.uno.profile.ratio")
                    value = user.unoStats.ratio.toString()
                    inline = true
                }

                field {
                    name = translate("commands.uno.profile.played")
                    value = (user.unoStats.wins + user.unoStats.losses).toString()
                }

                field {
                    name = translate("commands.uno.profile.rank")
                    value = database.users.find().toList()
                        .count { (it.unoStats?.ratio ?: 0.0) > user.unoStats.ratio }
                        .toString()
                    inline = true
                }
            }
        }
    }
}
