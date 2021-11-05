package dev.schlaubi.mikbot.util_plugins.verification

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

suspend fun VerificationModule.verificationListeners() {
    suspend fun GuildBehavior.leaveIfNotVerified() {
        val botGuild = VerificationDatabase.collection.findOneById(this.id)

        if (botGuild?.verified != true) {
            leave()
        }
    }

    event<ReadyEvent> {
        action {
            coroutineScope {
                kord.guilds.onEach {
                    it.leaveIfNotVerified()
                }.launchIn(this)
            }
        }
    }

    event<GuildCreateEvent> {
        action {
            event.guild.leaveIfNotVerified()
        }
    }
}
