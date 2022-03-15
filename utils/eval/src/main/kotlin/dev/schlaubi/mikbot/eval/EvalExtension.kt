package dev.schlaubi.mikbot.eval

import com.kotlindiscord.kord.extensions.extensions.Extension

class EvalExtension : Extension() {
    override val name: String = "eval"

    override suspend fun setup() {
        evalCommand()
    }
}
