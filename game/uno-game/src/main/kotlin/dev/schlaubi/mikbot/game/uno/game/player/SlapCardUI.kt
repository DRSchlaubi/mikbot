package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.uno.cards.SlapContext

suspend fun DiscordUnoPlayer.openSlapCardUI(slapContext: SlapContext) {
    val messageBuilder: MessageBuilder = {
        content = translate("game.ui.slap_card.description")
    }

    val (slapped) = confirmation(
        {
            controls.edit {
                val builder = FollowupMessageCreateBuilder(false)
                builder.it()

                content = builder.content
                components = builder.components
            }
        },
        hasNoOption = false,
        messageBuilder = messageBuilder,
        translate = game.translationsProvider::translate,
        yesWord = translate("game.ui.slap_card.slap"),
        timeout = null
    )

    if (slapped) {
        slapContext.slap(this)
        updateControls(false) // show cards again
    }
}
