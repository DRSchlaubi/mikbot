package dev.schlaubi.mikbot.eval.secrets

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.config.Config
import org.pf4j.Extension

@Extension
class CoreSecretExtension : SecretExtensionPoint, KordExKoinComponent {
    override fun provideSecrets(): List<String> {
        return buildList {
            add(Config.DISCORD_TOKEN)
            Config.SENTRY_TOKEN?.let(::add)
        }
    }
}
