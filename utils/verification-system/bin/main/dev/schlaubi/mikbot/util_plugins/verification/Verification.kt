package dev.schlaubi.mikbot.util_plugins.verification

import dev.schlaubi.mikbot.plugin.api.owner.OwnerExtensionPoint
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import org.pf4j.Extension

@Extension
class VerificationOwnerExtension : OwnerExtensionPoint {
    override suspend fun OwnerModule.apply() {
        unVerifyCommand()
        inviteCommand()
    }
}
