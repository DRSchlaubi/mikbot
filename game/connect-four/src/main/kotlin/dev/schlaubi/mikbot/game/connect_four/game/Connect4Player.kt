package dev.schlaubi.mikbot.game.connect_four.game

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.Player
import dev.schlaubi.mikbot.game.connect_four.Connect4

data class Connect4Player(override val user: UserBehavior, val type: Connect4.Player) : Player
