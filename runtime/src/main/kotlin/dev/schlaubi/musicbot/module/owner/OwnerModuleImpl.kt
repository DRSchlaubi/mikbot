package dev.schlaubi.musicbot.module.owner

import dev.kordex.core.extensions.slashCommandCheck
import dev.schlaubi.mikbot.plugin.api.MikBotTranslations
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.owner.OwnerExtensionPoint
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class OwnerModuleImpl(pluginSystem: PluginContext) : OwnerModule(pluginSystem) {
    override val name: String = "owner"
    val database: Database by inject()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    override val extensionClazz: KClass<out ModuleExtensionPoint<OwnerModule>> = OwnerExtensionPoint::class

    override suspend fun setup() {
        slashCommandCheck {
            failIfNot(MikBotTranslations.Checks.Owner.failed) { event.interaction.user.id in Config.BOT_OWNERS }
        }

        super.setup()
    }

    override suspend fun unload() {
        coroutineContext.cancel()
    }
}
