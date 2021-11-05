package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.rest.builder.message.create.actionRow
import dev.schlaubi.mikbot.game.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.mikbot.game.uno.game.ui.buttonStyle
import dev.schlaubi.mikbot.game.uno.game.ui.localizedName
import dev.schlaubi.uno.UnoColor

suspend fun DiscordUnoPlayer.pickWildCardColor(): UnoColor {
    val key = awaitResponse("uno.controls.wild_card.done") {
        content = translate("uno.controls.wild_cord.pick_color")

        actionRow {
            UnoColor.values().forEach { color ->
                interactionButton(color.buttonStyle, color.name) {
                    label = color.localizedName
                }
            }
        }
    } ?: return UnoColor.BLUE

    return UnoColor.valueOf(key)
}
