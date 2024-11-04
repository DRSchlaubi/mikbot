package dev.schlaubi.mikbot.core.gdpr

import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.commands.CommandContext
import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.GdprTranslations
import dev.schlaubi.stdx.core.paginate

fun GDPRModule.infoCommand() = ephemeralSubCommand {
    name = GdprTranslations.Commands.Gdpr.Info.name
    description = GdprTranslations.Commands.Gdpr.Info.description

    action {
        val (storedData, anonymizedData, processedData) =
            dataPoints.toDescription(this)

        respond {
            embed {
                title = translate(GdprTranslations.Commands.Gdpr.Info.title)

                dataPoint(
                    translate(GdprTranslations.Commands.Gdpr.Info.stored_data),
                    storedData,
                    translate(GdprTranslations.Gdpr.General.Persistent_data.explainer)
                )
                dataPoint(translate(GdprTranslations.Commands.Gdpr.Info.anonymized_data), anonymizedData)
                dataPoint(
                    translate(GdprTranslations.Commands.Gdpr.Info.data_processing),
                    processedData,
                    translate(GdprTranslations.Gdpr.General.Persistent_data.explainer)
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
        append(commandContext.translate(descriptionKey))
        if (sharingDescriptionKey != null) {
            appendLine()
            append(commandContext.translate(GdprTranslations.Gdpr.General.data_sharing))
            append(' ')
            append(
                commandContext.translate(
                    sharingDescriptionKey ?: error("No key defined for sharing on: ${this@describe}")
                )
            )
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
