package dev.schlaubi.musicbot.module.uno.game.player

import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.behavior.interaction.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.musicbot.core.io.findUser
import dev.schlaubi.musicbot.utils.MessageBuilder

@Suppress("UNCHECKED_CAST")
suspend fun DiscordUnoPlayer.translate(key: String, vararg replacements: Any?) =
    game.translationsProvider.translate(
        key,
        game.database.users.findUser(user).language,
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
        interaction.message?.id == message().id && interaction.user == user
    } ?: return null

    response.interaction.acknowledgePublicDeferredMessageUpdate()

    return response.interaction.componentId
}
