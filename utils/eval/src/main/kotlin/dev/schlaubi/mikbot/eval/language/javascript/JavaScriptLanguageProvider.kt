package dev.schlaubi.mikbot.eval.language.javascript

import dev.schlaubi.mikbot.eval.language.ExecutionResult
import dev.schlaubi.mikbot.eval.language.LanguageProvider
import dev.schlaubi.mikbot.eval.language.TypedExecutionResult
import dev.schlaubi.mikbot.eval.rhino.ScriptTimedoutException
import dev.schlaubi.mikbot.eval.rhino.TimeoutContextFactory
import org.mozilla.javascript.Context
import org.mozilla.javascript.RhinoException
import kotlin.time.Duration.Companion.seconds

class JavaScriptLanguageProvider : LanguageProvider {
    override val displayName: String = "JavaScript (Rhino)"
    override val id: String = "js-rhino"

    override suspend fun execute(code: String): ExecutionResult {
        val context = TimeoutContextFactory(10.seconds).enterContext()
        return try {
            val scope = context.initSafeStandardObjects()
            val result = context.evaluateString(scope, code, "eval.js", 1, null)
            TypedExecutionResult.Success(result)
        } catch (exception: ScriptTimedoutException) {
            TypedExecutionResult.Failing("Timed out", null)
        } catch (exception: RhinoException) {
            TypedExecutionResult.Failing(exception.message ?: "Unknown error", exception.scriptStackTrace)
        } finally {
            Context.exit()
        }
    }
}
