package space.votebot.core

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.request.KtorRequestException
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.embed
import dev.schlaubi.stdx.coroutines.forEachParallel
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import space.votebot.common.models.Poll
import space.votebot.common.models.sumUp
import space.votebot.pie_char_service.client.PieChartCreateRequest
import space.votebot.pie_char_service.client.PieChartServiceClient
import space.votebot.pie_char_service.client.Vote
import space.votebot.transformer.TransformerContext
import space.votebot.transformer.transformMessage
import space.votebot.util.toBehavior
import space.votebot.util.toPollMessage
import java.text.DecimalFormat

private val percentage = DecimalFormat("#.##%")

const val block = "■"
const val blockBarLength = 30

private val pieChartService = PieChartServiceClient(VoteBotConfig.PIE_CHART_SERVICE_URL)

private val LOG = KotlinLogging.logger { }

suspend fun Poll.addMessage(
    channel: MessageChannelBehavior,
    guild: GuildBehavior,
    addButtons: Boolean,
    addToDatabase: Boolean,
): Message {
    val message = channel.createMessage {
        embeds.add(toEmbed(channel.kord, guild, false))
        if (addButtons) {
            components.addAll(makeButtons(channel.kord, guild))
        }
    }

    if (addToDatabase) {
        VoteBotDatabase.polls.save(copy(messages = messages + message.toPollMessage()))
    }

    return message
}

suspend fun Poll.updateMessages(
    kord: Kord,
    guild: GuildBehavior,
    removeButtons: Boolean = false,
    highlightWinner: Boolean = false,
    showChart: Boolean? = null,
) {
    val pieChart = if (highlightWinner && showChart ?: settings.showChartAfterClose && votes.isNotEmpty()) {
        pieChartService
            .createPieChart(toPieChartCreateRequest(kord, guild))
            .toInputStream()
    } else {
        null
    }

    val failedMessages = mutableListOf<Poll.Message>()

    messages.forEachParallel { message ->
        try {
            message.toBehavior(kord).edit {
                if (pieChart != null) {
                    addFile("chart.png", pieChart)
                } else {
                    content = ""
                    embeds = mutableListOf(toEmbed(kord, guild, highlightWinner))
                }
                components = if (removeButtons) {
                    mutableListOf()
                } else {
                    makeButtons(kord, guild).toMutableList()
                }
            }
        } catch (ignored: KtorRequestException) {
            LOG.debug(ignored) { "An error occurred whilst updating a poll message" }
            failedMessages += message
        }
    }

    if (failedMessages.isNotEmpty()) {
        VoteBotDatabase.polls.save(copy(messages = messages - failedMessages.toSet()))
    }
}

private suspend fun Poll.makeButtons(kord: Kord, guild: GuildBehavior): List<MessageComponentBuilder> =
    sortedOptions
        .chunked(5)
        .map { options ->
            ActionRowBuilder().apply {
                options.forEach { (_, index, option, emoji) ->
                    interactionButton(ButtonStyle.Primary, "vote_$index") {
                        label = transformMessage(option, TransformerContext(guild, kord, false))
                        this.emoji = emoji?.toDiscordPartialEmoji()
                    }
                }
            }
        }

suspend fun Poll.toEmbed(
    kord: Kord,
    guild: GuildBehavior,
    highlightWinner: Boolean = false,
    overwriteHideResults: Boolean = false,
): EmbedBuilder = embed {
    title = transformMessage(this@toEmbed.title, TransformerContext(guild, kord, false))

    author {
        val user = kord.getUser(Snowflake(authorId))
        name = user?.username
        icon = user?.effectiveAvatar
    }

    val names = sortedOptions
        .map { (index, _, value, emoji) ->
            val prefix = emoji?.toDiscordPartialEmoji()?.mention ?: "${index + 1}"
            "$prefix. ${transformMessage(value, TransformerContext(guild, kord, true))}"
        }.joinToString(separator = "\n")

    val totalVotes = votes.sumOf { it.amount }
    val results = if (!settings.hideResults || highlightWinner || overwriteHideResults) {
        val resultsText = sumUp()
            .joinToString(separator = "\n") { (option, _, votePercentage) ->
                val blocksForOption = (votePercentage * blockBarLength).toInt()

                " ${option.positionedIndex + 1} | ${
                    block.repeat(blocksForOption).padEnd(blockBarLength)
                } | (${percentage.format(votePercentage)})"
            }
        """```$resultsText```"""
    } else {
        "The results will be hidden until the Poll is over"
    }

    description = """
        $names
        
        $results
    """.trimIndent()

    if (settings.deleteAfter != null) {
        val deleteAt = createdAt + settings.deleteAfter!!
        if (deleteAt > Clock.System.now()) {
            field {
                name = "Will end in"
                value = deleteAt.toDiscord(TimestampType.RelativeTime)
            }
        }
    }

    if (highlightWinner) {
        val options = sumUp().groupBy { it.amount }
        val maxVotes = options.keys.maxOrNull()

        val winners = maxVotes?.let { options[it] } ?: emptyList()

        field {
            name = if (winners.size > 1) "Winners" else "Winner"
            value = if (winners.isEmpty()) "No one voted" else winners.map {
                transformMessage(
                    it.option.option,
                    TransformerContext(guild, kord, false)
                )
            }
                .joinToString(", ")
        }
    }

    if (settings.publicResults) {
        field {
            name = "Privacy Notice"
            value = "The author of this Poll will be able to see, what you have voted for."
        }
    }

    field {
        name = "Total Votes"
        value = totalVotes.toString()
    }

    timestamp = createdAt
}

private suspend fun Poll.toPieChartCreateRequest(kord: Kord, guild: GuildBehavior): PieChartCreateRequest {
    val votes = sumUp()

    return PieChartCreateRequest(
        title,
        512, 512,
        votes.map { (option, count) ->
            Vote(
                count,
                transformMessage(option.option, TransformerContext(guild, kord, false))
            )
        }
    )
}

private val DiscordPartialEmoji.mention: String
    get() =
        if (id == null) {
            name!! // unicode
        } else {
            "<:$name:$id>" // custom emote
        }
