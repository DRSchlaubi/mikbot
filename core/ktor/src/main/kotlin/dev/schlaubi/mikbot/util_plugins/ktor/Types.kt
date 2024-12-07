package dev.schlaubi.mikbot.util_plugins.ktor

import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.json.schema.SchemaConfigurator
import io.bkbn.kompendium.json.schema.definition.JsonSchema
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure

fun SerialDescriptor.toJsonSchema(): TypeDefinition =
    toJsonSchemaOrNull() ?: error("Could not convert $this to JsonSchema")

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.toJsonSchemaOrNull(): TypeDefinition? {
    if (isInline) {
        return getElementDescriptor(0).toJsonSchemaOrNull()
    }
    return when (kind) {
        PrimitiveKind.BOOLEAN -> TypeDefinition.BOOLEAN
        PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT -> TypeDefinition.INT
        PrimitiveKind.CHAR, PrimitiveKind.STRING -> TypeDefinition.STRING
        PrimitiveKind.DOUBLE -> TypeDefinition.DOUBLE
        PrimitiveKind.FLOAT -> TypeDefinition.FLOAT
        PrimitiveKind.LONG -> TypeDefinition.LONG
        else -> null
    }
}

class KotlinxSerializationSchemaConfigurator(private val delegate: KotlinXSchemaConfigurator = KotlinXSchemaConfigurator()) :
    SchemaConfigurator by delegate {
    override fun sealedObjectEnrichment(implementationType: KType, implementationSchema: JsonSchema): JsonSchema {
        val serializer = (serializerOrNull(implementationType)
            ?: serializer(implementationType.jvmErasure.superclasses.first().createType(implementationType.arguments))).descriptor
        return serializer.toJsonSchemaOrNull()
            ?: delegate.sealedObjectEnrichment(implementationType, implementationSchema)
    }
}
