package dev.schlaubi.mikbot.game.hangman.game

import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.plugin.api.util.embed
import java.util.*

suspend fun GameState.Guessing.toEmbed(game: HangmanGame) = embed {
    description = buildString {

        append(game.translate("game.ui.word"))
        append("```")
        word.forEach {
            if (it.uppercaseChar() in chars || it.isWhitespace()) {
                append(it)
            } else {
                append('_')
            }
        }
        append("```")

        if (wrongChars.isNotEmpty() || blackList.isNotEmpty()) {
            repeat(2) {
                appendLine()
            }

            append(HangmanGame.googologo.take(wrongChars.size + blackList.size).joinToString(" ") { it.mention })
        }
    }

    if (wrongChars.isNotEmpty()) {
        field {
            name = game.translate("game.ui.wrong_characters")
            value = wrongChars.joinToString("`, `", "`", "`") { it.uppercase(Locale.ENGLISH) }
        }
    }

    if (blackList.isNotEmpty()) {
        field {
            name = game.translate("game.ui.wrong_words")
            value = blackList.joinToString("`, `", "`", "`")
        }
    }
}
