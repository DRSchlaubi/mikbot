package dev.schlaubi.mikbot.core.health.check

import com.mongodb.ReadPreference
import dev.kordex.core.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.util.IKnowWhatIAmDoing
import dev.schlaubi.mikbot.plugin.api.util.database
import org.pf4j.Extension

@Extension
class DatabaseHealthCheck : HealthCheck, KordExKoinComponent {
    @OptIn(IKnowWhatIAmDoing::class)
    override suspend fun checkHealth(): Boolean {
        if (Config.MONGO_DATABASE != null && Config.MONGO_URL != null) {
            val cluster = database.client.client.clusterDescription
            return cluster.hasReadableServer(ReadPreference.nearest())
                && cluster.hasWritableServer()
        }
        return true
    }
}
