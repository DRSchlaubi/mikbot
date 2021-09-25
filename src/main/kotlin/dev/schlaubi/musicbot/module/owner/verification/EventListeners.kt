package dev.schlaubi.musicbot.module.owner.verification

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.owner.OwnerModule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

suspend fun OwnerModule.verificationListeners() {
    suspend fun GuildBehavior.leaveIfNotVerified() {
        val botGuild = database.guildSettings.findGuild(this)

        if (!botGuild.verified) {
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
