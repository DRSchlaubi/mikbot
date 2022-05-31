package dev.schlaubi.mikmusic.core.audio

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import org.koin.core.component.inject

class LavalinkManager : Extension() {
    override val name: String = "lavalink"
    private lateinit var lavalink: LavaKord
    private val database: Database by inject()
    private val lavalinkServers = database.getCollection<LavalinkServer>("lavalink_servers")

    override suspend fun setup() {
    }

    suspend fun load() {
        lavalink = kord.lavakord()

        lavalinkServers.find().toList().forEach { (url, password) ->
            lavalink.addNode(url, password)
        }
    }

    fun getLink(guild: GuildBehavior) = lavalink.getLink(guild.id)

    override suspend fun unload() {
        lavalink.nodes.forEach(Node::close)
    }
}
