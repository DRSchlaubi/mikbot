package dev.schlaubi.mikbot.util_plugins.verification

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.Kord
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class VerificationModule : Extension() {
    override val name: String = "verification"

    override suspend fun setup() {
        verificationListeners()
    }
}

@PluginMain
class VerificationPlugin(wrapper: PluginWrapper) : Plugin(wrapper), CoroutineScope, KoinComponent {
    val kord by inject<Kord>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val server = makeServer()

    override fun start() {
        server.start()
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::VerificationModule)
    }

    override fun stop() {
        server.stop(1000, 1000)
        coroutineContext.cancel()
    }
}
