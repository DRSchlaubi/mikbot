package space.votebot.core

import space.votebot.common.models.Poll
import java.io.ByteArrayInputStream
import java.io.InputStream


private val header = listOf("user_id", "vote_option", "amount").joinToString(separator = ",")

fun Poll.generateCSV(): String = buildString {
    append(header)
    appendLine()
    votes.forEach {
        append(it.userId)
        append(',')
        append(it.forOption + 1)
        append(',')
        append(it.amount)
        appendLine()
    }
}

fun Poll.generateCSVFile(): InputStream = ByteArrayInputStream(generateCSV().toByteArray())
