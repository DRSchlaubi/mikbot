package dev.schlaubi.mikbot.eval

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI

class EvalExtension : Extension() {
    override val name: String = "eval"
    override val bundle: String = "eval"

    @Suppress("UnnecessaryOptInAnnotation") // otherwise, it gives a compiler error
    @OptIn(UnsafeAPI::class)
    override suspend fun setup() {
        evalCommand()
    }
}
