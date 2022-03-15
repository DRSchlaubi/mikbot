package dev.schlaubi.mikbot.eval.secrets

import dev.schlaubi.mikbot.plugin.api.config.Config
import org.koin.core.component.KoinComponent
import org.pf4j.Extension

@Extension
class CoreSecretExtension : SecretExtensionPoint, KoinComponent {
    override fun provideSecrets(): List<String> {
        return buildList {
            add(Config.DISCORD_TOKEN)
            Config.SENTRY_TOKEN?.let(::add)
        }
    }
}
