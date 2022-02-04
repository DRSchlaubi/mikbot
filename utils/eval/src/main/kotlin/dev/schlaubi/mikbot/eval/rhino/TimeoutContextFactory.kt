package dev.schlaubi.mikbot.eval.rhino

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.mozilla.javascript.Callable
import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Scriptable
import kotlin.time.Duration

class TimeoutContextFactory(private val timeout: Duration) : ContextFactory() {
    // Custom Context to store execution time.
    private class TimeoutContext(factory: TimeoutContextFactory) : Context(factory) {
        var startTime: Instant = Clock.System.now()
    }

    override fun makeContext(): Context {
        val cx = TimeoutContext(this)
        // Make Rhino runtime to call observeInstructionCount
        // each 10000 bytecode instructions
        cx.instructionObserverThreshold = 10000
        return cx
    }

    override fun observeInstructionCount(cx: Context, instructionCount: Int) {
        val mcx = cx as TimeoutContext
        val currentTime = Clock.System.now()
        if (currentTime - mcx.startTime > timeout) {
            throw ScriptTimedoutException()
        }
    }

    override fun doTopCall(
        callable: Callable,
        cx: Context, scope: Scriptable,
        thisObj: Scriptable, args: Array<Any>?
    ): Any {
        val mcx = cx as TimeoutContext
        mcx.startTime = Clock.System.now()
        return super.doTopCall(callable, cx, scope, thisObj, args)
    }
}
