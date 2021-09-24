package dev.schlaubi.musicbot.module.owner

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.schlaubi.musicbot.config.Config

class OwnerModule : Extension() {
    override val name: String = "owner"
    override val bundle: String = "owner"

    override suspend fun setup() {
        slashCommandCheck {
            failIfNot(translate("checks.owner.failed")) { event.interaction.user.id in Config.BOT_OWNERS }
        }

        redeployCommand()
    }
}
