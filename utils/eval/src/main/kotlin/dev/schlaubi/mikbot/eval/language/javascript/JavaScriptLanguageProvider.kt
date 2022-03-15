package dev.schlaubi.mikbot.eval.language.javascript

import dev.schlaubi.mikbot.eval.integration.ExecutionContext
import dev.schlaubi.mikbot.eval.language.ExecutionResult
import dev.schlaubi.mikbot.eval.language.LanguageProvider
import dev.schlaubi.mikbot.eval.language.TypedExecutionResult
import dev.schlaubi.mikbot.eval.rhino.ScriptTimedoutException
import dev.schlaubi.mikbot.eval.rhino.TimeoutContextFactory
import org.mozilla.javascript.Context
import org.mozilla.javascript.RhinoException
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Wrapper
import kotlin.time.Duration.Companion.seconds

class JavaScriptLanguageProvider : LanguageProvider {
    override val displayName: String = "JavaScript (Rhino)"
    override val id: String = "js-rhino"

    override suspend fun execute(code: String, executionContext: ExecutionContext): ExecutionResult {
        val context = TimeoutContextFactory(10.seconds).enterContext()
        return try {
            val scope = context.initSafeStandardObjects()
            applyExecutionContext(executionContext, scope, context)
            when (val result = context.evaluateString(scope, code, "eval.js", 1, null)) {
                is ScriptableObject -> {
                    TypedExecutionResult.Success(result, result.typeOf)
                }
                is Wrapper -> {
                    TypedExecutionResult.Success(result.unwrap(), result.unwrap()::class.java.simpleName)
                }
                else -> {
                    TypedExecutionResult.Success(result, result::class.java.simpleName)
                }
            }
        } catch (exception: ScriptTimedoutException) {
            TypedExecutionResult.Failing("Timed out", null)
        } catch (exception: RhinoException) {
            TypedExecutionResult.Failing(exception.message ?: "Unknown error", exception.scriptStackTrace)
        } finally {
            Context.exit()
        }
    }

    private fun applyExecutionContext(
        executionContext: ExecutionContext,
        scope: ScriptableObject,
        context: Context?
    ) = mapOf(
        "guild" to executionContext.guild,
        "member" to executionContext.member,
        "user" to executionContext.user,
        "interaction" to executionContext.interaction,
        "channel" to executionContext.channel
    ).forEach { (name, value) ->
        val js = Context.javaToJS(value, scope, context)
        ScriptableObject.putProperty(scope, name, js)
    }
}
