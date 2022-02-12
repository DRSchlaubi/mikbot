package dev.schlaubi.mikbot.eval.language.converter

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.schlaubi.mikbot.eval.language.LanguageProvider

@Converter("language", types = [ConverterType.SINGLE])
class LanguageConverter(override var validator: Validator<LanguageProvider> = null) : ChoiceConverter<LanguageProvider>(
    LanguageProvider.providers.associateBy { it.displayName }
) {
    override val signatureTypeString: String = "converters.language.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        this.parsed = LanguageProvider.providers.providerByNameOrId(arg) ?: return false

        return true
    }

    private fun Collection<LanguageProvider>.providerByNameOrId(arg: String): LanguageProvider? =
        firstOrNull { it.displayName == arg || it.id == arg }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false
        this.parsed = LanguageProvider.providers.providerByNameOrId(optionValue) ?: return false
        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true
            this@LanguageConverter.choices.forEach { choice(it.key, it.value.id) }
        }
}
