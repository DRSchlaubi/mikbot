package dev.schlaubi.epic_games_notifier

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

object WebhookDatabase : KordExKoinComponent {
    private val database by inject<Database>()

    val webhooks = database.getCollection<Webhook>("epic_games_webhooks")
}

@Serializable
data class Webhook(
    @SerialName("_id")
    val id: Snowflake,
    val sentPromotions: List<String>,
    val token: String
)
