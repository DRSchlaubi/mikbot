package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.Converter
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.parser.StringParser

enum class UnSetableBoolean(val value: Boolean?) {
    TRUE(true),
    FALSE(false),
    UNSET(null)
}

fun Arguments.unSetableBoolean(
    displayName: String,
    description: String,
    validator: Validator<String?> = null
) = unSettableBooleanString(displayName, description, validator).map {
    UnSetableBoolean.valueOf(it.uppercase())
}

@OptIn(ConverterToOptional::class)
fun Arguments.optionalUnSetableBoolean(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: Validator<String?> = null
) = unSettableBooleanString(displayName, description, validator).toOptional(
    outputError = outputError,
    nestedValidator = validator
).map {
    it?.let { UnSetableBoolean.valueOf(it.uppercase()) }
}

private fun Arguments.unSettableBooleanString(
    displayName: String,
    description: String,
    validator: Validator<String?> = null
) = stringChoice(
    displayName, description,
    mapOf(
        "True" to UnSetableBoolean.TRUE.name,
        "False" to UnSetableBoolean.FALSE.name,
        "Unset" to UnSetableBoolean.UNSET.name
    ),
    validator
)

private fun <InputType : Any?, OutputType : Any?, NamedInputType : Any, ResultType : Any, B : Any?> Converter<InputType, OutputType, NamedInputType, ResultType>.map(
    mapper: (OutputType) -> B
): Converter<B, B, NamedInputType, ResultType> =
    object : Converter<B, B, NamedInputType, ResultType>() {
        private var parsedValue: Any? = null
        override var parsed: B
            @Suppress("UNCHECKED_CAST")
            get() = parsedValue as B
            set(value) {}
        override val signatureTypeString: String = this@map.signatureTypeString + ".mapped"

        override suspend fun parse(parser: StringParser?, context: CommandContext, named: NamedInputType?): ResultType {
            val result = this@map.parse(parser, context, named)
            parsedValue = mapper(this@map.parsed)
            return result
        }
    }
