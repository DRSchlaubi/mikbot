package dev.schlaubi.musicbot.module.uno.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder

const val leaveGameButton = "leave_game"

fun ActionRowBuilder.leaveButton() = interactionButton(ButtonStyle.Danger, leaveGameButton) {
    label = "Leave game"
}
