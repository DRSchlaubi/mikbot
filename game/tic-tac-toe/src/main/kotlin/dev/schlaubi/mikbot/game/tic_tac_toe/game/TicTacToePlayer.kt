package dev.schlaubi.mikbot.game.tic_tac_toe.game

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.Player
import dev.schlaubi.mikbot.game.tic_tac_toe.PlayerType

class TicTacToePlayer(
    override val user: UserBehavior,
    val type: PlayerType
) : Player
