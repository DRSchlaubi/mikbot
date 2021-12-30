package space.votebot.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.votebot.common.models.Poll.*

/**
 * Representation of a poll.
 *
 * @property guildId the id of the Guild the poll is on
 * @property authorId the id of the author of the poll.
 * @property title the title of the poll
 * @property options the [options][Option] which are available
 * @property changes the amount of changes per user
 * @property votes a list of [votes][Vote] made by users
 * @property messages a list of [messages][Message] displaying the vote status
 * @property createdAt the [Instant] at which the poll was created
 * @property settings the settings for this poll
 */
@Serializable
public data class Poll(
    @SerialName("_id")
    public val id: String,
    public val guildId: ULong,
    public val authorId: ULong,
    public val title: String,
    public val options: List<Option>,
    public val changes: Map<ULong, Int>,
    public val votes: List<Vote>,
    public val messages: List<Message>,
    public val createdAt: Instant,
    public val settings: FinalPollSettings
) {
    /**
     * All [actual options][Option.ActualOption] sorted by [Option.position].
     */
    @Suppress("UNCHECKED_CAST")
    val sortedOptions: List<RenderableOption>
        get() = options
            .withIndex()
            .filter { (_, option) -> option is Option.ActualOption }
            .sortedBy { (index, option) -> option.position ?: (index + 1) }
            .mapIndexed { positionedIndex, (globalIndex, option) ->
                RenderableOption(positionedIndex, globalIndex, (option as Option.ActualOption).option, option.emoji)
            }

    /**
     * Option which can be rendered easily.
     *
     * @property positionedIndex the index which it has under all actual options (inside [sortedOptions])
     * @property index the actual index inside [options]
     * @property option the option text
     */
    @Serializable
    public data class RenderableOption(
        val positionedIndex: Int,
        val index: Int,
        val option: String,
        val emoji: Option.ActualOption.Emoji?
    )

    /**
     * Representation of a poll option.
     *
     * @property position the position at which the option is displayed
     *                      (this is used so rearranging doesn't change the option index)
     *                      (if null the index will be used)
     */
    @Serializable
    public sealed class Option {
        public abstract val position: Int?

        /**
         * Representation of a poll option.
         *

         * @property option the text of the option
         */
        @Serializable
        @SerialName("actual")
        public data class ActualOption(override val position: Int?, val option: String, val emoji: Emoji?) : Option() {
            @Serializable
            public data class Emoji(val id: ULong?, val name: String?)
        }

        /**
         * Option used as a spacer, to make other options remain correct index.
         */
        @Serializable
        @SerialName("spacer")
        public data class Spacer(override val position: Int?) : Option()

    }

    /**
     * Representation of a user vote.
     *
     * @property forOption the index at [Poll.options] this vote is for
     * @property userId the id of the user who voted
     * @property amount how many times the user voted for this
     */
    @Serializable
    public data class Vote(
        val forOption: Int,
        val userId: ULong,
        val amount: Int
    ) {
        // This exists so kx.ser doesn't infer active to be true, but there still is that constructor
        public constructor(forOption: Int, userId: ULong) : this(forOption, userId, 1)
    }

    /**
     * Representation of a message displaying the poll status.
     *
     * @property messageId the id of the message
     * @property channelId the id of the channel the message is in
     * @property guildId the id of the guild the message is on
     */
    @Serializable
    public data class Message(
        public val messageId: ULong,
        public val channelId: ULong,
        // yes this is duplicated data, but maybe I will allow messages across guilds
        public val guildId: ULong
    )
}

/**
 * Representation of a vote result option.
 *
 * @property option the option
 * @property amount how many people voted for this option
 * @property percentage the percentage of this option
 */
public data class VoteOption(val option: RenderableOption, val amount: Int, val percentage: Double)

/**
 * Sums all votes into a list of [vote options][VoteOption].
 */
public fun Poll.sumUp(): List<VoteOption> {
    val totalVotes = votes.sumOf { it.amount }
    return sortedOptions
        .map { renderableOption ->
            val (_, index, _) = renderableOption
            val votes = votes
                .asSequence()
                .filter { it.forOption == index }
                .sumOf { it.amount }

            VoteOption(renderableOption, votes, votes.toDouble() / totalVotes.coerceAtLeast(1))
        }
}
