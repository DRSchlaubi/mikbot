package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.common.entity.ApplicationIntegrationType
import dev.kord.common.entity.InteractionContextType
import dev.kordex.core.commands.application.ApplicationCommand

public fun ApplicationCommand<*>.executableEverywhere() {
    scope {
        integrationTypes.addAll(ApplicationIntegrationType.entries)
        contexts.contexts.addAll(InteractionContextType.entries)
    }
}
