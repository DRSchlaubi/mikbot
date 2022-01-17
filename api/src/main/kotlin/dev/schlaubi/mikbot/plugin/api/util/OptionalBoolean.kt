package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.Converter
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.Validator

public enum class UnSetableBoolean(public val value: Boolean?) {
    TRUE(true),
    FALSE(false),
    UNSET(null)
}

public fun Arguments.unSetableBoolean(
    displayName: String,
    description: String,
    validator: Validator<String?> = null
): Converter<UnSetableBoolean?, UnSetableBoolean?, String, Boolean> = unSettableBooleanString(displayName, description, validator).map {
    it?.uppercase()?.let { UnSetableBoolean.valueOf(it) }
}

@OptIn(ConverterToOptional::class)
public fun Arguments.optionalUnSetableBoolean(
    displayName: String,
    description: String,
    validator: Validator<String?> = null
): Converter<UnSetableBoolean?, UnSetableBoolean?, String, Boolean> = unSettableBooleanString(displayName, description, validator).map {
    it?.let { UnSetableBoolean.valueOf(it.uppercase()) }
}

private fun Arguments.unSettableBooleanString(
    displayName: String,
    description: String,
    validator: Validator<String?> = null
) = optionalStringChoice {
    name = displayName
    this.description = description

    choice("True", UnSetableBoolean.TRUE.name)
    choice("False", UnSetableBoolean.FALSE.name)
    choice("Unset", UnSetableBoolean.UNSET.name)

    validate(validator)
}
