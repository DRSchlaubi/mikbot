package dev.schlaubi.mikbot.game.api

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.module.GameModule

abstract class SingleWinnerGame<T : Player>(host: UserBehavior, module: GameModule<T, out AbstractGame<T>>) :
    AbstractGame<T>(host, module) {
    var winner: T? = null
    override val wonPlayers: List<T>
        get() = listOfNotNull(winner)
}
