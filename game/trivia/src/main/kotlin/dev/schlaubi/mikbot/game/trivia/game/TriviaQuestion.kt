package dev.schlaubi.mikbot.game.trivia.game

import dev.schlaubi.mikbot.game.trivia.open_trivia.Question
import dev.schlaubi.mikbot.game.multiple_choice.Question as GameQuestion

class TriviaQuestion(val parent: Question, val original: Question?) : GameQuestion by parent
