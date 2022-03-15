package dev.schlaubi.mikbot.haste

public class Haste(public val key: String, public val url: String, private val hasteClient: HasteClient) {
    public suspend fun getContent(): String {
        return hasteClient.getHasteContent(key)
    }
}
