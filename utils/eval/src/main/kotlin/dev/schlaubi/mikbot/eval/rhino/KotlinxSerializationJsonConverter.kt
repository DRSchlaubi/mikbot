package dev.schlaubi.mikbot.eval.rhino

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import java.util.function.UnaryOperator

class KotlinxSerializationJsonConverter : UnaryOperator<Any> {
    @OptIn(ExperimentalSerializationApi::class)
    override fun apply(value: Any): Any {
        val serializer = serializerOrNull(value::class.java) ?: return value.toString()
        return Json.encodeToString(serializer, value)
    }
}
