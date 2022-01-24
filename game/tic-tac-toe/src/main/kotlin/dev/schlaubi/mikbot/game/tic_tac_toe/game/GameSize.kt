package dev.schlaubi.mikbot.game.tic_tac_toe.game

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

@Suppress("EnumEntryName")
enum class GameSize(val size: Int, override val readableName: String) : ChoiceEnum {
    `3_BY_3`(3, "3x3"),
    `4_BY_4`(4, "4x4"),
    `5_BY_5`(5, "5x5")
}
