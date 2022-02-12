package dev.schlaubi.mikbot.eval.secrets

import org.pf4j.ExtensionPoint

/**
 * Extension point for allowing other plugins to register secrets.
 */
interface SecretExtensionPoint : ExtensionPoint {
    fun provideSecrets(): List<String>
}
