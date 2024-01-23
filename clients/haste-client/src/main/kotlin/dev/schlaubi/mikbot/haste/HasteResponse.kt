package dev.schlaubi.mikbot.haste

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class HasteResponse(@JsonNames("path") val key: String)
