package dev.schlaubi.mikbot.game.hangman.game

import dev.schlaubi.mikbot.plugin.api.util.embed
import java.util.*

fun GameState.Guessing.toEmbed() = embed {
    description = buildString {

        append("Word: ")
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
            name = "Wrong chars"
            value = wrongChars.joinToString("`, `", "`", "`") { it.uppercase(Locale.ENGLISH) }
        }
    }

    if (blackList.isNotEmpty()) {
        field {
            name = "Wrong words"
            value = blackList.joinToString("`, `", "`", "`")
        }
    }
}
