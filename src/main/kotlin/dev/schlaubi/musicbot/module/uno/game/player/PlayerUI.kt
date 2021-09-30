package dev.schlaubi.musicbot.module.uno.game.player

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.musicbot.module.uno.game.ui.buttonStyle
import dev.schlaubi.musicbot.module.uno.game.ui.emoji
import dev.schlaubi.musicbot.module.uno.game.ui.translationKey

suspend fun DiscordUnoPlayer.updateControls(active: Boolean) {
    controls.edit {
        val key = if (active) "uno.controls.acvtive.head" else "uno.controls.inacvtive.head"
        content = translate(key)

        val cards = deck
            .mapIndexed { index, card -> card to index } // save origin index
            .chunked(5) // Only 5 buttons per action row

        cards.forEach {
            actionRow {
                it.forEach { (card, index) ->
                    interactionButton(card.buttonStyle, "play_card_$index") {
                        emoji = DiscordPartialEmoji(id = Snowflake(card.emoji))
                        label = translate(card.translationKey)
                        disabled = !active || !card.canBePlayedOn(game.game.topCard)
                    }
                }
            }
        }

        actionRow {
            if (!drawn) {
                interactionButton(ButtonStyle.Danger, drawCardButton) {
                    label = translate("uno.actions.draw_card")
                    disabled = !active
                }
            } else {
                interactionButton(ButtonStyle.Danger, skipButton) {
                    label = translate("uno.actions.skip")
                    disabled = !active || !drawn
                }
            }

            if (deck.size <= 2) {
                interactionButton(if (saidUno) ButtonStyle.Success else ButtonStyle.Primary, sayUnoButton) {
                    label = translate("uno.actions.say_uno")
                    disabled = !active || deck.size <= 1
                }
            }
        }
    }
}
