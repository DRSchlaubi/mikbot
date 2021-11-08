package dev.schlaubi.musicbot.module.settings

import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import org.koin.core.component.inject
import kotlin.reflect.KClass

class SettingsModuleImpl : SettingsModule() {
    override val name: String = "settings"
    override val extensionClazz: KClass<out ModuleExtensionPoint<SettingsModule>> = SettingsExtensionPoint::class
    override val bundle: String = "settings"
    val database: Database by inject()
}
