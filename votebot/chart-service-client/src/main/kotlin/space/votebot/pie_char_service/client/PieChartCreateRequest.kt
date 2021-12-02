package space.votebot.pie_char_service.client

import kotlinx.serialization.SerialName

/**
 * Request to create a pie chart.@
 *
 * @property title the title of the pie chart
 * @property width the width of the pie chart
 * @property height the height of the pie chart
 * @property votes a list of [votes][Vote] shown in the Pie Chart
 */
public data class PieChartCreateRequest(
    val title: String,
    val width: Int,
    val height: Int,
    val votes: List<Vote>
)

/**
 * Representation of a vote on a poll.
 *
 * @property voteCount how many people voted for this option
 * @property title the title of the option
 */
public data class Vote(@SerialName("vote_count") val voteCount: Int, val title: String)