package dev.schlaubi.musicbot.module.owner

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.owner.verification.verification
import dev.schlaubi.musicbot.utils.extension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class OwnerModule : Extension(), CoroutineScope {
    override val name: String = "owner"
    override val bundle: String = "owner"
    val database: Database by inject()
    val musicModule: MusicModule by extension()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    override suspend fun setup() {
        slashCommandCheck {
            failIfNot(translate("checks.owner.failed")) { event.interaction.user.id in Config.BOT_OWNERS }
        }

        redeployCommand()
        if (Config.VERIFIED_MODE) {
            verification()
        }
    }

    override suspend fun unload() {
        coroutineContext.cancel()
    }
}
