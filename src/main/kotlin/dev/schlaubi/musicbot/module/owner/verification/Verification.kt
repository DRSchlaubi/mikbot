package dev.schlaubi.musicbot.module.owner.verification

import dev.schlaubi.musicbot.module.owner.OwnerModule

suspend fun OwnerModule.verification() {
    verificationListeners()
    unVerifyCommand()
    inviteCommand()

    startVerifyServer()
}

private fun OwnerModule.startVerifyServer() { // make ambiguous coroutine scope, no longer ambiguous
    startServer()
}
