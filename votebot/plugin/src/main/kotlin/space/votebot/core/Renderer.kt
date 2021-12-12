package space.votebot.core

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.request.KtorRequestException
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.embed
import dev.schlaubi.mikbot.plugin.api.util.forEachParallel
import kotlinx.datetime.Clock
import space.votebot.common.models.Poll
import space.votebot.util.toBehavior
import java.text.DecimalFormat

private val percentage = DecimalFormat("#.##%")

const val block = "■"
const val blockBarLength = 30

suspend fun Poll.updateMessages(kord: Kord) = messages.forEachParallel { message ->
    try {
        message.toBehavior(kord).edit {
            content = ""
            embeds = mutableListOf(toEmbed(kord))
            addButtons(this@updateMessages)
        }
    } catch (ignored: KtorRequestException) {
    }
}

fun MessageModifyBuilder.addButtons(poll: Poll) {
    poll.options
        .withIndex()
        .sortedBy { (_, option) -> option.position }
        .chunked(5)
        .forEach { options ->
            actionRow {
                options.forEach { (index, option) ->
                    interactionButton(ButtonStyle.Primary, "vote_$index") {
                        label = option.option
                    }
                }
            }
        }
}

suspend fun Poll.toEmbed(kord: Kord): EmbedBuilder = embed {
    title = this@toEmbed.title

    author {
        val user = kord.getUser(Snowflake(authorId))
        name = user?.username
        icon = user?.effectiveAvatar
    }

    val names = options
        .sortedBy { it.position }
        .joinToString("\n") { "${it.position + 1}. ${it.option}" }

    val totalVotes = votes.sumOf { it.amount }
    val results = options
        .withIndex()
        .sortedBy { (_, option) -> option.position }
        .joinToString(separator = "\n") { (index, option) ->
            val votesForOption = votes
                .asSequence()
                .filter { it.forOption == index }
                .sumOf { it.amount }
            val votePercentage = if (totalVotes == 0) {
                0.0
            } else {
                votesForOption.toDouble() / totalVotes
            }
            val blocksForOption = (votePercentage * blockBarLength).toInt()

            " ${option.position + 1} | ${
                block.repeat(blocksForOption).padEnd(blockBarLength)
            } | (${percentage.format(votePercentage)})"
        }
    description = """
        $names
        
        ```$results```
    """.trimIndent()

    field {
        name = "Total Votes"
        value = totalVotes.toString()
    }

    if (settings.deleteAfter != null) {
        field {
            name = "Will end in"
            value = (Clock.System.now() + settings.deleteAfter!!).toDiscord(TimestampType.RelativeTime)
        }
    }

    timestamp = createdAt
}
