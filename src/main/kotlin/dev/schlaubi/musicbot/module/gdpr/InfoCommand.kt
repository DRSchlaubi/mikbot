package dev.schlaubi.musicbot.module.gdpr

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed

fun GDPRModule.infoCommand() = ephemeralSubCommand {
    name = "info"
    description = "Shows the bots privacy policy"

    action {
        respond {
            embed {
                title = translate("commands.gdpr.info.title")

                field {
                    name = translate("commands.gdpr.info.stored_data")
                    value = translate("commands.gdpr.info.stored_data.description")
                }

                field {
                    name = translate("commands.gdpr.info.anonymized_data")
                    value = translate("commands.gdpr.info.anonymized_data.description")
                }
                field {
                    name = translate("commands.gdpr.info.data_processing")
                    value = translate("commands.gdpr.info.data_processing.description")
                }
            }

//            actionRow {
//                linkButton("https://google.com") {
//                    label = translate("commands.gdpr.info.request")
//                }
//                linkButton("https://google.com") {
//                    label = translate("commands.gdpr.info.delete")
//                }
//            }
        }
    }
}
