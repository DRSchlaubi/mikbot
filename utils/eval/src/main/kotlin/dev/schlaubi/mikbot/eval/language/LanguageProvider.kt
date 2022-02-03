package dev.schlaubi.mikbot.eval.language

import dev.schlaubi.mikbot.eval.language.javascript.JavaScriptLanguageProvider

interface LanguageProvider {
    val displayName: String
    val id: String

    fun execute(code: String): ExecutionResult

    companion object {
        val providers = listOf<LanguageProvider>(JavaScriptLanguageProvider())
    }
}
