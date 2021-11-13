package dev.schlaubi.epic_games_notifier

import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.Id

object WebhookDatabase : KoinComponent {
    private val database by inject<Database>()

    val webhooks = database.getCollection<Webhook>("epic_games_webhooks")
}

@Serializable
data class Webhook(
    @SerialName("_id") @Contextual
    val id: Id<Webhook>,
    val sentPromotions: List<String>,
    val url: String
)
