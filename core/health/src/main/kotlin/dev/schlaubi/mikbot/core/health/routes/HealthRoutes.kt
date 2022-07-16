package dev.schlaubi.mikbot.core.health.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/")
class HealthRoutes {
    @Serializable
    @Resource("/healthz") // this is not a typo. See https://stackoverflow.com/questions/43380939/where-does-the-convention-of-using-healthz-for-application-health-checks-come-f
    class Health(val health: HealthRoutes)
}
