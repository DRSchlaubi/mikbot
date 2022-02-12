package dev.schlaubi.mikbot.eval

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.live.channel.live
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.eval.language.converter.language
import dev.schlaubi.mikbot.plugin.api.settings.botOwnerOnly
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeoutOrNull
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
    botOwnerOnly()

    action {
        val response = respond {
            embed {
                description = "Please enter the code which you want to execute."
            }
        }
        val result = withTimeoutOrNull(60.seconds) {
            channel.asChannelOf<TextChannel>().live()
                .events
                .filterIsInstance<MessageCreateEvent>()
                .filter { it.member?.id == member?.id }
                .take(1)
                .single()
        }
        if (result == null) {
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
        val execution = measureTimedValue {
            language.execute(result.message.content)
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
                    title = ":white_check_mark: Successful execution"
                    Color(0x17cf17)
                } else {
                    title = ":x: Execution failed."
                    Color(0xcf3917)
                }
            }
        }
    }
}
