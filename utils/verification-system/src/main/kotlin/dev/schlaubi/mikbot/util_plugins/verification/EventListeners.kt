package dev.schlaubi.mikbot.util_plugins.verification

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.schlaubi.stdx.coroutines.suspendLazy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import dev.schlaubi.mikbot.plugin.api.config.Config as BotConfig

private val LOG = KotlinLogging.logger { }

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
                kord.guilds.onEach(Guild::leaveIfNotVerified).launchIn(this)

                val guild = BotConfig.OWNER_GUILD
                if (guild != null && VerificationDatabase.collection.findOneById(guild) == null) {
                    val newInvite = suspendLazy {
                        Invitation(newId(), guild).also { VerificationDatabase.invites.save(it) }
                    }
                    val invite = VerificationDatabase.invites.findOne(Invitation::guildId eq guild)
                        ?: newInvite()
                    LOG.info { "INVITE YOUR BOT HERE: ${invite.url}" }
                }
            }
        }
    }

    event<GuildCreateEvent> {
        action {
            event.guild.leaveIfNotVerified()
        }
    }
}
