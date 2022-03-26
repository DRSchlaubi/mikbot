package dev.schlaubi.mikbot.eval

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.Color
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.eval.integration.ExecutionContext
import dev.schlaubi.mikbot.eval.language.converter.language
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.safeMember
import io.ktor.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

private const val codeInputField = "code"

class EvalArguments : Arguments() {
    val language by language {
        name = "language"
        description = "commands.eval.arguments.language.description"
    }
}

@OptIn(UnsafeAPI::class)
suspend fun EvalExtension.evalCommand() = unsafeSlashCommand(::EvalArguments) {
    name = "eval"
    description = "commands.eval.description"
    ownerOnly()

    initialResponse = InitialSlashCommandResponse.None

    action {
        val id = generateNonce()
        val response = event.interaction.modal(translate("command.eval.ui.title"), id) {
            actionRow {
                textInput(TextInputStyle.Paragraph, codeInputField, translate("command.eval.ui.code"))
            }
        }
        val interaction = response.kord.waitFor<ModalSubmitInteractionCreateEvent>(60.seconds.inWholeMilliseconds) {
            interaction.modalId == id
        }?.interaction
        if (interaction == null) {
            response.createEphemeralFollowup {
                embed {
                    description = translate("command.eval.timeout")
                }
            }
            return@action
        }

        val code = interaction.textInputs[codeInputField]!!.value!!
        val language = arguments.language
        val modalResponse = interaction.deferEphemeralResponse()
        val context = ExecutionContext(
            safeGuild.asGuild(),
            safeMember.asMember(),
            user.asUser(),
            event.interaction,
            channel.asChannelOf()
        )
        val execution = measureTimedValue {
            language.execute(code, context)
        }
        modalResponse.respond {
            embed {
                with(execution.value) {
                    applyToEmbed()
                }
                footer {
                    text = translate("command.eval.execution.done", arrayOf(execution.duration))
                }
                color = if (execution.value.wasSuccessful) {
                    title = translate("command.eval.execution.successful", arrayOf(Emojis.whiteCheckMark))
                    Color(0x17cf17)
                } else {
                    title = translate("command.eval.execution.failed", arrayOf(Emojis.x))
                    Color(0xcf3917)
                }
            }
        }
    }
}
