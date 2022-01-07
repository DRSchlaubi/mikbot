package space.votebot.core

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.not
import space.votebot.common.models.FinalPollSettings
import space.votebot.common.models.Poll

internal val ExpirationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

suspend fun rescheduleAllPollExpires(kord: Kord) = coroutineScope {
    VoteBotDatabase.polls.find(not(Poll::settings / FinalPollSettings::deleteAfter eq null))
        .toFlow()
        .onEach { poll ->
            poll.addExpirationListener(kord)
        }.launchIn(this)
}

@OptIn(KordUnsafe::class, KordExperimental::class)
fun Poll.addExpirationListener(kord: Kord) {
    val duration = settings.deleteAfter ?: error("This vote does not have an expiration Date")
    val expireAt = createdAt + duration

    ExpirationScope.launch {
        val timeUntilExpiry = expireAt - Clock.System.now()
        if (!timeUntilExpiry.isNegative()) {
            delay(timeUntilExpiry)
        }

        close(kord, guild = kord.unsafe.guild(Snowflake(guildId)))
    }
}
