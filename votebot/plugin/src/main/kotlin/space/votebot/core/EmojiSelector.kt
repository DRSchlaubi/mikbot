package space.votebot.core

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.flow.toList
import space.votebot.common.models.FinalPollSettings
import space.votebot.common.models.Poll
import space.votebot.common.models.PollSettings
import java.util.*

private val numbers = buildEmojiList {
    setOf(one, two, three, four, five, six, seven, eight, nine)
}
private val fallbackEmojis = buildEmojiList {
    setOf<DiscordEmoji>(
        greenApple,
        apple,
        pear,
        tangerine,
        lemon,
        hamburger,
        fries,
        hotdog,
        pizza,
        spaghetti,
        soccer,
        baseball,
        football,
        baseball,
        tennis,
        telephone,
        pager,
        cd,
        trackball,
        joystick,
        movieCamera,
        watch,
        mobilePhoneOff,
        alarmClock
    )
}

suspend fun FinalPollSettings.selectEmojis(
    guild: GuildBehavior,
    optionCount: Int,
    poll: Poll? = null
): List<Poll.Option.ActualOption.Emoji> {
    val usedEmojis by lazy {
        poll?.options?.mapNotNull { (it as? Poll.Option.ActualOption)?.emoji }?.toSet() ?: emptySet()
    }
    return when (emojiMode) {
        PollSettings.EmojiMode.OFF -> emptyList()
        PollSettings.EmojiMode.ON -> selectDefaultEmojis(optionCount, usedEmojis)
        PollSettings.EmojiMode.CUSTOM -> selectCustomEmojis(optionCount, usedEmojis, guild)
    }
}

private fun selectDefaultEmojis(
    optionCount: Int, usedEmojis: Set<Poll.Option.ActualOption.Emoji>
): List<Poll.Option.ActualOption.Emoji> {
    val emojis = if (optionCount <= 9) {
        numbers
    } else {
        fallbackEmojis
    }

    return emojis.toPollEmoji() - usedEmojis.toSet()
}

private suspend fun selectCustomEmojis(
    optionCount: Int,
    usedEmojis: Set<Poll.Option.ActualOption.Emoji>,
    guild: GuildBehavior
): List<Poll.Option.ActualOption.Emoji> {
    val guildEmojis = guild.emojis.toList().take(optionCount)
    val filler = fallbackEmojis.take(optionCount - guildEmojis.size)

    return (guildEmojis.map {
        Poll.Option.ActualOption.Emoji(
            it.id.value, it.name
        )
    } + filler.toPollEmoji()) - usedEmojis
}

private fun Iterable<DiscordEmoji>.toPollEmoji() = map { Poll.Option.ActualOption.Emoji(name = it.unicode, id = null) }

private fun <C : Collection<DiscordEmoji>> buildEmojiList(builder: Emojis.() -> C): C = Emojis.builder()

fun Poll.Option.ActualOption.Emoji.toDiscordPartialEmoji() = DiscordPartialEmoji(id?.let { Snowflake(it) }, name)

suspend fun Poll.recalculateEmojis(guild: GuildBehavior): Poll {
    val emojis = settings.selectEmojis(
        guild,
        options.size
    )
    // using a queue doesn't mess up indices
    val emojiQueue = LinkedList(emojis)

    return copy(options = options.map {
        if (it is Poll.Option.ActualOption) {
            it.copy(emoji = emojiQueue.poll())
        } else {
            it
        }
    })
}
