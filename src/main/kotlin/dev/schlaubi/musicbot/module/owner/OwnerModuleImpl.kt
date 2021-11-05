package dev.schlaubi.musicbot.module.owner

import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.owner.OwnerExtensionPoint
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class OwnerModuleImpl : OwnerModule() {
    override val name: String = "owner"
    override val bundle: String = "owner"
    val database: Database by inject()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    override val extensionClazz: KClass<out ModuleExtensionPoint<OwnerModule>> = OwnerExtensionPoint::class

    override suspend fun setup() {
        slashCommandCheck {
            failIfNot(translate("checks.owner.failed")) { event.interaction.user.id in Config.BOT_OWNERS }

        }

        super.setup()
    }

    override suspend fun unload() {
        coroutineContext.cancel()
    }
}
