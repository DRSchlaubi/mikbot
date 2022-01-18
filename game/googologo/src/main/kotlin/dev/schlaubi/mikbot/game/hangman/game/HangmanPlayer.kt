package dev.schlaubi.mikbot.game.hangman.game

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.Player

class HangmanPlayer(override val user: UserBehavior) : Player
