package dev.schlaubi.mikbot.eval.language

import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.mikbot.eval.language.javascript.JavaScriptLanguageProvider

interface LanguageProvider {
    val displayName: String
    val id: String

    suspend fun execute(code: String, guild: GuildBehavior): ExecutionResult

    companion object {
        val providers = listOf<LanguageProvider>(JavaScriptLanguageProvider())
    }
}
