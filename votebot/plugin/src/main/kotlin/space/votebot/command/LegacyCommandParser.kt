package space.votebot.command

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.Intent
import dev.kord.rest.builder.message.create.embed
import space.votebot.core.VoteBotModule

private const val prefix = "v!"
private val commands = listOf(
    listOf("prefix", "p"),
    listOf("permissions", "perms"),
    listOf("language", "languages", "lang"),
    listOf("blacklist", "bl"),
    listOf("whitelist", "wl"),
    listOf("settings", "preferences", "pref"),
    listOf("customemotes", "ce"),
    listOf("help"),
    listOf("info", "about"),
    listOf("quickcreate", "quick-create", "qc"),
    listOf("vote", "addvote", "v"),
    listOf("close", "delete", "remove"),
    listOf("status", "i", "current"),
    listOf("create", "make", "new", "n", "c"),
    listOf("changeheading", "heading", "ch"),
    listOf("addoption", "add-option", "ao"),
    listOf("removeoption", "remove-option", "ro")
)

suspend fun VoteBotModule.legacyCommandParser() {
    event<MessageCreateEvent> {
        intents.add(Intent.GuildMessages)
        check {
            anyGuild()
            failIfNot { event.message.content.startsWith(prefix) }
        }


        action {
            val commandName = event.message.content.substringAfter(prefix).substringBefore(' ').lowercase()
            val command = commands.firstOrNull { commandName in it }?.first()
                ?: return@action

            val commandExplainer = translate("bot.generic.legacy_commands.$command")

            event.message.reply {
                embed {
                    description = translate("bot.generic.legacy_commands.explainer")
                    field {
                        name = translate("bot.generic-legacy_commands.how_to_now.title")
                        value = commandExplainer
                    }
                }
            }
        }
    }
}
