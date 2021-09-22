package dev.schlaubi.musicbot.core.audio

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.musicbot.core.io.Database
import org.koin.core.component.inject

class LavalinkManager : Extension() {
    override val name: String = "lavalink"
    private lateinit var lavalink: LavaKord
    private val database: Database by inject()

    override suspend fun setup() {
    }

    public suspend fun load() {
        lavalink = kord.lavakord()

        database.lavalinkServers.find().toList().forEach { (url, password) ->
            lavalink.addNode(url, password)
        }
    }

    fun getLink(guild: GuildBehavior) = lavalink.getLink(guild.id)

    override suspend fun unload() {
        lavalink.nodes.forEach { it.close() }
    }
}
