@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.gradle.PluginInfo
import dev.schlaubi.mikbot.gradle.PluginRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import org.pf4j.update.DefaultUpdateRepository
import java.net.URL
import org.pf4j.update.PluginInfo as PF4JPluginInfo

class MikBotPluginRepository internal constructor(private val plugins: List<PF4JPluginInfo>, url: URL) : DefaultUpdateRepository(generateNonce(), url) {

    override fun getPlugins(): Map<String, PF4JPluginInfo> = plugins.associateBy(PF4JPluginInfo::id)
    companion object {
        suspend operator fun invoke(url: Url): MikBotPluginRepository {
            val info = HttpClient {
                install(ContentNegotiation) {
                    json()
                }
            }.use {
                it.get(url) {
                  url {
                      appendPathSegments("plugins.json")
                  }
                }.body<List<PluginInfo>>()
            }

            return MikBotPluginRepository(info.toPF4J(), url.toURI().toURL())
        }
    }
}

private fun PluginInfo.toPF4J(): PF4JPluginInfo = PF4JPluginInfo().apply {
    id = this@toPF4J.id
    name = this@toPF4J.name
    description = this@toPF4J.description
    projectUrl = this@toPF4J.projectUrl
    releases = this@toPF4J.releases.toPF4J()
}

@JvmName("pluginInfoToPF4J")
private fun List<PluginInfo>.toPF4J() = map(PluginInfo::toPF4J)

@JvmName("pluginReleaseToPF4J")
private fun List<PluginRelease>.toPF4J() = map(PluginRelease::toPF4j)

private fun PluginRelease.toPF4j() = PF4JPluginInfo.PluginRelease().apply {
    version = this@toPF4j.version
    date = this@toPF4j.date
    requires = this@toPF4j.requires
    url = this@toPF4j.url
}
