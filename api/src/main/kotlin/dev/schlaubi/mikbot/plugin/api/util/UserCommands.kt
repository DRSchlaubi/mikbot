package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.commands.application.ApplicationCommand
import dev.kord.common.entity.ApplicationIntegrationType
import dev.kord.common.entity.InteractionContextType

public fun ApplicationCommand<*>.executableEverywhere() {
    allowedInstallTypes.addAll(ApplicationIntegrationType.entries)
    allowedContexts.addAll(InteractionContextType.entries)
}
