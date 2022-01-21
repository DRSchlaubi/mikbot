package dev.schlaubi.mikbot.game.multiple_choice.player

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.api.Player

/**
 * Default implementation of [Player] for multiple choice games.
 */
open class MultipleChoicePlayer(override val user: UserBehavior) : Player
