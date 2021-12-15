package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.MikBotInfo
import dev.schlaubi.mikbot.plugin.api.util.parallelMapNotNull
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import space.votebot.VoteBotInfo
import space.votebot.core.VoteBotConfig
import space.votebot.core.VoteBotModule
import kotlin.collections.set

private val LOG = KotlinLogging.logger { }
private val repositories = listOf("DRSchlaubi/mikbot", "Votebot/piechart-service")
private val gitHubUserCache = mutableMapOf<Long, GitHubUser>()

private val json = Json {
    ignoreUnknownKeys = true
}

private val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
}

@Serializable
private data class GitHubContributor(val id: Long, val url: String)

@Serializable
private data class GitHubUser(
    @SerialName("html_url") val htmlUrl: String,
    val name: String?,
    val id: Long,
    val login: String
)

@OptIn(InternalAPI::class)
private suspend fun findContributors() = repositories.parallelMapNotNull { repository ->
    client.get<List<GitHubContributor>>("https://api.github.com/repos/") {
        url {
            encodedPath += "$repository/contributors"
        }

        if (VoteBotConfig.GITHUB_TOKEN != null) {
            val token = "${VoteBotConfig.GITHUB_USERNAME}:${VoteBotConfig.GITHUB_TOKEN}"
                .encodeBase64()
            header(HttpHeaders.Authorization, "Basic $token")
        }
    }
}
    .flatten()
    .parallelMapNotNull { (id, url) ->
        gitHubUserCache[id] ?: client.get<GitHubUser>(url).also {
            gitHubUserCache[id] = it
        }
    }
    .distinctBy(GitHubUser::id)

suspend fun VoteBotModule.infoCommand() = publicSlashCommand {
    name = "info"
    description = "Displays generic information about this bot"

    action {
        respond {
            embed {
                title = "VoteBot"

                description = translate("commands.info.mikbot")

                field {
                    name = translate("commands.info.contributors")
                    val contributors = runCatching { findContributors() }
                    contributors.exceptionOrNull()?.let {
                        LOG.warn(it) { "An error occurred while fetching Contributors" }
                    }
                    value = contributors.getOrNull()?.joinToString { (url, name, _, login) ->
                        "[${name ?: login}]($url)"
                    } ?: translate("commands.info.contributors.failed")
                    inline = true
                }

                field {
                    name = translate("commands.info.graphics")
                    value = "[Oskar Lang](https://rxs.to)"
                    inline = false
                }


                field {
                    name = translate("commands.info.version.mikbot")
                    value = MikBotInfo.VERSION
                    inline = true
                }

                field {
                    name = translate("commands.info.version.votebot")
                    value = VoteBotInfo.VERSION
                    inline = true
                }

                field {
                    name = translate("commands.info.source_code")
                    value = "https://github.com/DRSchlaubi/mikbot/tree/main/votebot"
                    inline = false
                }
            }
        }
    }
}
