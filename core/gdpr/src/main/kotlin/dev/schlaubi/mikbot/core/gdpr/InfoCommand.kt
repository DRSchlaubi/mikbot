package dev.schlaubi.mikbot.core.gdpr

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData

fun GDPRModule.infoCommand() = ephemeralSubCommand {
    name = "info"
    description = "Shows the bots privacy policy"

    action {
        val (storedData, anonymizedData, processedData)
                = dataPoints.toDescription(this)

        respond {
            embed {
                title = translate("commands.gdpr.info.title")

                field {
                    name = translate("commands.gdpr.info.stored_data")
                    val explainer = translate("gdpr.general.persistent_data.explainer")
                    value = (listOf(explainer) + storedData).joinToString("\n\n")
                }
                field {
                    name = translate("commands.gdpr.info.anonymized_data")
                    value = anonymizedData.joinToString("\n\n")
                }
                field {
                    name = translate("commands.gdpr.info.data_processing")
                    val explainer = translate("gdpr.general.processed_data.explainer")
                    value = (listOf(explainer) + storedData).joinToString("\n\n")
                }
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
