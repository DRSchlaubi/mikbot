package dev.schlaubi.mikbot.game.uno.game.player

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.getKoin
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.behavior.interaction.followup.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.followUpEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import dev.schlaubi.mikbot.plugin.api.util.getLocale

@Suppress("UNCHECKED_CAST")
suspend fun DiscordUnoPlayer.translate(key: String, vararg replacements: Any?) =
    game.translationsProvider.translate(
        key,
        discordLocale?.convertToISO()?.asJavaLocale() ?: getKoin().get<ExtensibleBot>().getLocale(game.thread.asChannel(), user.asUser()),
        "uno",
        replacements = replacements as Array<Any?>
    )

suspend fun DiscordUnoPlayer.awaitResponse(
    doneTranslationKey: String,
    messageBuilder: MessageBuilder
): String? {
    val message = response.followUpEphemeral { messageBuilder() }

    val response = awaitResponse { message } ?: return null

    message.edit {
        components = mutableListOf()
        content = translate(doneTranslationKey)
    }

    return response
}

suspend fun DiscordUnoPlayer.awaitResponse(message: () -> FollowupMessageBehavior): String? {

    val response = game.kord.waitFor<ComponentInteractionCreateEvent>(unoInteractionTimeout) {
        interaction.message.id == message().id && interaction.user == user
    } ?: return null

    response.interaction.deferPublicMessageUpdate()

    return response.interaction.componentId
}
