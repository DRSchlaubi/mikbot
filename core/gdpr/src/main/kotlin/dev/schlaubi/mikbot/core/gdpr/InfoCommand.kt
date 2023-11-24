package dev.schlaubi.mikbot.core.gdpr

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.stdx.core.paginate

fun GDPRModule.infoCommand() = ephemeralSubCommand {
    name = "info"
    description = "commands.gdpr.info.name"

    action {
        val (storedData, anonymizedData, processedData) =
            dataPoints.toDescription(this)

        respond {
            embed {
                title = translate("commands.gdpr.info.title")

                dataPoint(
                    translate("commands.gdpr.info.stored_data"),
                    storedData,
                    translate("gdpr.general.persistent_data.explainer")
                )
                dataPoint(translate("commands.gdpr.info.anonymized_data"), anonymizedData)
                dataPoint(
                    translate("commands.gdpr.info.data_processing"),
                    processedData,
                    translate("gdpr.general.processed_data.explainer")
                )
            }
        }
    }
}

private fun EmbedBuilder.dataPoint(name: String, values: List<String>, explainer: String? = null) {
    val value = (listOfNotNull(explainer) + values)
        .map { "$it \n\n" }
        .paginate(1024)
    value.forEachIndexed { index, text ->
        if (text.isNotBlank()) {
            field {
                this.name = name + if (value.size > 1) " (${index + 1})" else ""
                this.value = text
            }
        }
    }
}

private data class DataPointsDescriptions(
    val storedData: List<String>,
    val anonymizedData: List<String>,
    val processedData: List<String>,
)

private suspend fun List<DataPoint>.toDescription(commandContext: CommandContext): DataPointsDescriptions {
    suspend fun DataPoint.describe() = buildString {
        append(commandContext.translate(descriptionKey, module))
        if (sharingDescriptionKey != null) {
            appendLine()
            append(commandContext.translate("gdpr.general.data_sharing"))
            append(' ')
            append(commandContext.translate(sharingDescriptionKey!!, module))
        }
    }

    val storedData = mutableListOf<String>()
    val anonymizedData = mutableListOf<String>()
    val processedData = mutableListOf<String>()

    forEach {
        when (it) {
            is PermanentlyStoredDataPoint -> storedData.add(it.describe())
            is AnonymizedData -> anonymizedData.add(it.describe())
            is ProcessedData -> processedData.add(it.describe())
        }
    }

    return DataPointsDescriptions(storedData, anonymizedData, processedData)
}
