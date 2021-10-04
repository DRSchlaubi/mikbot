package dev.schlaubi.musicbot.module.gdpr

import dev.kord.rest.builder.component.ActionRowBuilder
import dev.schlaubi.musicbot.module.SubCommandModule
import kotlinx.coroutines.flow.first

class GDPRModule : SubCommandModule() {
    override val name: String = "gdpr"
    override val bundle: String = "gdpr"
    override val commandName: String = "gdpr"

    override suspend fun overrideSetup() {
        infoCommand()
        requestCommand()
        deleteCommand()
    }

    @Suppress("UNREACHABLE_CODE")
    suspend fun ActionRowBuilder.clickCommandButton(name: String, label: String) {
        // https://github.com/discord/discord-api-docs/discussions/3347#discussioncomment-1162191
        return
        val command = kord.globalCommands.first { it.name == name }

        val url = "discord://commands/${kord.resources.applicationId}/${command.id}"

        linkButton(url) {
            this.label = label
        }
    }
}
