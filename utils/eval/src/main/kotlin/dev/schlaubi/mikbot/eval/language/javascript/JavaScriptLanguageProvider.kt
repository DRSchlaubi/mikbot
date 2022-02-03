package dev.schlaubi.mikbot.eval.language.javascript

import dev.schlaubi.mikbot.eval.language.ExecutionResult
import dev.schlaubi.mikbot.eval.language.LanguageProvider
import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.RhinoException

class JavaScriptLanguageProvider : LanguageProvider {
    override val displayName: String = "JavaScript (Rhino)"
    override val id: String = "js-rhino"

    override fun execute(code: String): ExecutionResult {
        val context = ContextFactory().enterContext()
        return try {
            val scope = context.initSafeStandardObjects()
            val result = context.evaluateString(scope, code, "eval.js", 1, null)
            JavaScriptExecutionResult.Success(result)
        } catch (exception: RhinoException) {
            JavaScriptExecutionResult.Failing(exception.message ?: "Unknown error", exception.scriptStackTrace)
        } finally {
            Context.exit()
        }
    }
}
