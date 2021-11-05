package dev.schlaubi.mikbot.core.redeploy_hook.api

import org.pf4j.ExtensionPoint

interface RedeployExtensionPoint : ExtensionPoint {
    suspend fun beforeRedeploy()
}
