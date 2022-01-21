package dev.schlaubi.mikbot.game.trivia.game

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.Player

class TriviaPlayer(override val user: UserBehavior, var correctAnswers: Int = 0) : Player
