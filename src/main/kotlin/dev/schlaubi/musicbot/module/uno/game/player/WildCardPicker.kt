package dev.schlaubi.musicbot.module.uno.game.player

import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import dev.schlaubi.musicbot.module.uno.game.ui.buttonStyle
import dev.schlaubi.musicbot.module.uno.game.ui.localizedName
import dev.schlaubi.uno.UnoColor

private val colorNames = UnoColor.values().map(UnoColor::name)

suspend fun DiscordUnoPlayer.pickWildCardColor(): UnoColor {
    val picker = response.followUpEphemeral {
        content = translate("uno.controls.wild_cord.pick_color")

        actionRow {
            UnoColor.values().forEach { color ->
                interactionButton(color.buttonStyle, color.name) {
                    label = color.localizedName
                }
            }
        }
    }

    val event = game.kord.waitFor<ComponentInteractionCreateEvent>(unoInteractionTimeout) {
        interaction.message?.id == picker.id && interaction.user == owner && interaction.componentId in colorNames
    } ?: return UnoColor.BLUE
    event.interaction.acknowledgePublicDeferredMessageUpdate()
    picker.edit {
        components = mutableListOf()
        content = translate("uno.controls.wild_card.done")
    }

    return UnoColor.valueOf(event.interaction.componentId)
}
