package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.contexts.UnsafeSlashCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondPublic
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.followup.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val yes = "yes"
private const val no = "no"

@Suppress("DataClassCanBeRecord")
public data class Confirmation(
    val value: Boolean,
    val response: FollowupMessageBehavior,
    val interaction: ComponentInteraction?,
) :
    FollowupMessageBehavior by response

/**
 * Initiates a button based confirmation form for a [EphemeralSlashCommandContext].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
public suspend fun EphemeralSlashCommandContext<*, *>.confirmation(
    yesWord: String? = null,
    noWord: String? = null,
    timeout: Duration = 30.seconds,
    acknowledge: Boolean = true,
    messageBuilder: MessageBuilder,
): Confirmation = confirmation(yesWord, noWord, { respond { it() } }, timeout, acknowledge, messageBuilder)

/**
 * Initiates a button based confirmation form for a [PublicSlashCommandContext].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
public suspend fun PublicSlashCommandContext<*, *>.confirmation(
    yesWord: String? = null,
    noWord: String? = null,
    timeout: Duration = 30.seconds,
    acknowledge: Boolean = true,
    messageBuilder: MessageBuilder,
): Confirmation = confirmation(yesWord, noWord, { respond { it() } }, timeout, acknowledge, messageBuilder)

/**
 * Initiates a button based confirmation form for a [UnsafeSlashCommandContext].
 * Sends an ephemeral message to the user.
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
@UnsafeAPI
public suspend fun UnsafeSlashCommandContext<*, *>.ephemeralConfirmation(
    yesWord: String? = null,
    noWord: String? = null,
    timeout: Duration = 30.seconds,
    acknowledge: Boolean = true,
    messageBuilder: MessageBuilder,
): Confirmation =
    unsafeConfirmation(yesWord, noWord, messageBuilder, timeout, acknowledge) { respondEphemeral { it() } }

/**
 * Initiates a button based confirmation form for a [UnsafeSlashCommandContext].
 * Sends a public message to the user.
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
@UnsafeAPI
public suspend fun UnsafeSlashCommandContext<*, *>.publicConfirmation(
    yesWord: String? = null,
    noWord: String? = null,
    timeout: Duration = 30.seconds,
    acknowledge: Boolean = true,
    messageBuilder: MessageBuilder,
): Confirmation = unsafeConfirmation(yesWord, noWord, messageBuilder, timeout, acknowledge) { respondPublic { it() } }

@OptIn(UnsafeAPI::class)
private suspend fun UnsafeSlashCommandContext<*, *>.unsafeConfirmation(
    yesWord: String?,
    noWord: String?,
    messageBuilder: MessageBuilder,
    timeout: Duration,
    acknowledge: Boolean = true,
    sendMessage: EditableMessageSender,
): Confirmation = confirmation(yesWord, noWord, sendMessage, timeout, acknowledge, messageBuilder)

private suspend fun CommandContext.confirmation(
    yesWord: String? = null,
    noWord: String? = null,
    sendMessage: EditableMessageSender,
    timeout: Duration = 30.seconds,
    acknowledge: Boolean = true,
    messageBuilder: MessageBuilder,
): Confirmation = confirmation(sendMessage, timeout, messageBuilder, translate = ::translate, yesWord = yesWord, noWord = noWord, acknowledge = acknowledge)

/**
 * Bare bone confirmation implementation.
 */
public suspend fun confirmation(
    sendMessage: EditableMessageSender,
    timeout: Duration? = 30.seconds,
    messageBuilder: MessageBuilder,
    hasNoOption: Boolean = true,
    translate: Translator,
    yesWord: String? = null,
    noWord: String? = null,
    acknowledge: Boolean = true,
): Confirmation {
    val message = sendMessage {
        messageBuilder()

        components {
            actionRow {
                interactionButton(ButtonStyle.Success, yes) {
                    label = yesWord ?: translate("general.yes", "general")
                }

                if (hasNoOption) {
                    interactionButton(ButtonStyle.Danger, no) {
                        label = noWord ?: translate("general.no", "general")
                    }
                }
            }
        }
    }

    val response = message.kord.waitFor<InteractionCreateEvent>(timeout?.inWholeMilliseconds) {
        (interaction as? ComponentInteraction)?.let {
            it.message.id == message.id
        } == true
    } ?: return Confirmation(false, message, null)

    when (message) {
        is EphemeralFollowupMessage -> message.edit { components = mutableListOf() }
        is PublicFollowupMessage -> message.edit { components = mutableListOf() }
    }

    val interaction = response.interaction as ComponentInteraction
    if (acknowledge) {
        interaction.deferEphemeralMessageUpdate()
    }

    val button = interaction.componentId

    return Confirmation(button == yes, message, interaction)
}
