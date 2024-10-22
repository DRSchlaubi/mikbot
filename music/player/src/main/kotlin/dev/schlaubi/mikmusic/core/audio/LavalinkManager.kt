package dev.schlaubi.mikmusic.core.audio

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.audio.internal.AbstractLavakord
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.lavasrc.LavaSrc
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import org.koin.core.component.inject

class LavalinkManager(context: PluginContext) : MikBotModule(context) {
    override val name: String = "lavalink"
    private lateinit var lavalink: LavaKord
    private val database: Database by inject()
    private val lavalinkServers = database.getCollection<LavalinkServer>("lavalink_servers")

    override suspend fun setup() {
    }

    suspend fun load() {
        lavalink = kord.lavakord {
            plugins {
                install(Sponsorblock)
                install(LavaSrc)
            }
        }

        lavalinkServers.find().toList().forEach { (url, password) ->
            lavalink.addNode(url, password)
        }
    }

    fun getLink(guild: GuildBehavior) = lavalink.getLink(guild.id)

    @Suppress("INVISIBLE_REFERENCE")
    fun newNode() = (lavalink as AbstractLavakord).loadBalancer.determineBestNode(Snowflake.min.value)
        ?: error("No node found")

    override suspend fun unload() {
        lavalink.nodes.forEach(Node::close)
    }
}
