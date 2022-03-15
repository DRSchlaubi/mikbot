package dev.schlaubi.mikbot.eval.language

import dev.schlaubi.mikbot.eval.integration.ExecutionContext
import dev.schlaubi.mikbot.eval.language.javascript.JavaScriptLanguageProvider

interface LanguageProvider {
    val displayName: String
    val id: String

    suspend fun execute(code: String, executionContext: ExecutionContext): ExecutionResult

    companion object {
        val providers = listOf<LanguageProvider>(JavaScriptLanguageProvider())
    }
}
