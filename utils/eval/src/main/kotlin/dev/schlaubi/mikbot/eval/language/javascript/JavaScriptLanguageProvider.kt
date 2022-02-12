package dev.schlaubi.mikbot.eval.language.javascript

import dev.kord.core.behavior.GuildBehavior
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

    override suspend fun execute(code: String, guild: GuildBehavior): ExecutionResult {
        val context = TimeoutContextFactory(10.seconds).enterContext()
        return try {
            val scope = context.initSafeStandardObjects()
            val jsGuild = Context.javaToJS(guild.asGuild(), scope, context)
            ScriptableObject.putProperty(scope, "guild", jsGuild)
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
}
