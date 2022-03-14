package dev.schlaubi.mikbot.eval

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.waitForMessage
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.eval.integration.ExecutionContext
import dev.schlaubi.mikbot.eval.language.converter.language
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.safeMember
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class EvalArguments : Arguments() {
    val language by language {
        name = "language"
        description = "The language of the code you want to execute."
    }
}

@OptIn(KordPreview::class)
suspend fun EvalExtension.evalCommand() = publicSlashCommand(::EvalArguments) {
    name = "eval"
    description = "Execute some code."
    ownerOnly()

    action {
        val response = respond {
            embed {
                description = "Please enter the code which you want to execute."
            }
        }
        val message = channel.waitForMessage(60.seconds.inWholeMilliseconds) {
            member?.id == safeMember.id
        }
        if (message == null) {
            response.edit {
                embed {
                    description = "You did not provide some code in the given time."
                }
            }
            return@action
        }
        val language = arguments.language
        response.edit {
            embed {
                description = "Running <a:loading:547513249835384833>"
            }
        }
        val context = ExecutionContext(
            safeGuild.asGuild(),
            safeMember.asMember(),
            user.asUser(),
            event.interaction,
            channel.asChannelOf()
        )
        val execution = measureTimedValue {
            language.execute(message.content, context)
        }
        response.edit {
            embed {
                with(execution.value) {
                    applyToEmbed()
                }
                footer {
                    text = "Execution took ${execution.duration}"
                }
                color = if (execution.value.wasSuccessful) {
                    title = "${Emojis.whiteCheckMark} Successful execution"
                    Color(0x17cf17)
                } else {
                    title = "${Emojis.x} Execution failed."
                    Color(0xcf3917)
                }
            }
        }
    }
}
