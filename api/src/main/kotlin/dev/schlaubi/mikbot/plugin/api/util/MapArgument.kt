package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.Converter
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser

public fun <InputType : Any?, OutputType : Any?, NamedInputType : Any, ResultType : Any, B : Any?> Converter<InputType, OutputType, NamedInputType, ResultType>.map(
    mapper: (OutputType) -> B
): Converter<B, B, NamedInputType, ResultType> =
    object : Converter<B, B, NamedInputType, ResultType>() {
        private var parsedValue: Any? = null

        @Suppress("UNUSED_PARAMETER") // the setter is irrelevant here
        override var parsed: B
            @Suppress("UNCHECKED_CAST")
            get() = mapper(this@map.parsed)
            set(value) {}

        override val signatureType: Key
            get() = TODO("Not supported")

        override suspend fun parse(parser: StringParser?, context: CommandContext, named: NamedInputType?): ResultType {
            val result = this@map.parse(parser, context, named)
            if (this@map.parseSuccess) {
                parseSuccess = true
            }
            parsedValue = mapper(this@map.parsed)
            return result
        }

        override suspend fun validate(context: CommandContext) {
            super.validate(context)
        }
    }
