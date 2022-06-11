package dev.schlaubi.mikbot.util_plugins.birthdays.server

import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.pf4j.Extension
import kotlin.collections.set
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.TimeZone as DatetimeTimeZone

private val requestStore = mutableMapOf<String, CompletableDeferred<DatetimeTimeZone>>()
private val lock = Mutex()

@Serializable
@Resource("/birthdays/timezone/{key}")
data class TimeZone(val key: String, @SerialName("time_zone") val timeZone: String? = null)

@Extension
class KtorServer : KtorExtensionPoint {
    override fun Application.apply() {
        routing {
            get<TimeZone> { (key, zone) ->
                if (zone != null) {
                    val deferred =
                        lock.withLock { requestStore[key] } ?: return@get context.respond(HttpStatusCode.NotFound)
                    val timeZone = runCatching { DatetimeTimeZone.of(zone) }.getOrElse { DatetimeTimeZone.UTC }
                    deferred.complete(timeZone)
                    context.respondText("Thank you for using our time zone detection service, your new iPhone will not be sent to your address shortly")
                } else {
                    context.respondHtml {
                        head {
                            title("TimeZone detection service")
                        }

                        body {
                            script {
                                unsafe {
                                    //language=JavaScript
                                    +"""
                                        let zone = Intl.DateTimeFormat().resolvedOptions().timeZone
                                        let encodedZone = encodeURIComponent(zone)
                                
                                        window.location.href = '?time_zone=' + encodedZone
                                    """.trimIndent()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun receiveTimeZone(key: String): DatetimeTimeZone? {
    val result = withTimeoutOrNull(30.seconds) {
        val deferred = CompletableDeferred<DatetimeTimeZone>()
        lock.withLock {
            requestStore[key] = deferred
        }
        return@withTimeoutOrNull deferred.await()
    }
    requestStore.remove(key)
    return result
}
