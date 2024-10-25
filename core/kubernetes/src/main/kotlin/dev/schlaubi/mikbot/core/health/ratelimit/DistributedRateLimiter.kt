package dev.schlaubi.mikbot.core.health.ratelimit

import dev.kord.rest.ratelimit.RequestRateLimiter
import dev.kord.rest.ratelimit.RequestResponse
import dev.kord.rest.ratelimit.RequestToken
import dev.kord.rest.request.Request
import dev.kord.rest.request.identifier
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.TokensInheritanceStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private val LOG = KotlinLogging.logger { }

private val autoBanBucket = BucketConfiguration.builder()
    .addLimit { it.capacity(25000).refillIntervally(25000, 10.minutes.toJavaDuration()) }
    .build()

private const val globalBucket = "global"

class DistributedRateLimiter(proxyManager: ProxyManager<String>) : RequestRateLimiter {
    private val proxyManager = proxyManager.asAsync()
    private val executor = Executors.newScheduledThreadPool(10)
    private val start = Clock.System.now()

    private suspend fun await(name: String, configuration: BucketConfiguration, expiry: Duration = 1.minutes) {
        proxyManager.getProxy(name) { CompletableFuture.completedFuture(configuration) }
            .asScheduler()
            .tryConsume(1, expiry.toJavaDuration(), executor)
            .await()
    }

    private suspend fun awaitByName(name: String) {
        val requestBucket = proxyManager.getProxyConfiguration(name).await()
        if (requestBucket.isPresent) {
            await(name, requestBucket.get())
        }
    }

    override suspend fun await(request: Request<*, *>): RequestToken {
        val requestIdentifier = request.identifier.toString()
        awaitByName(globalBucket)
        awaitByName(requestIdentifier)
        await("auto_ban", autoBanBucket)

        return object : RequestToken {
            private val deferred = CompletableFuture<Unit>()

            override val completed: Boolean
                get() = deferred.isDone

            override suspend fun complete(response: RequestResponse) {
                if (response is RequestResponse.GlobalRateLimit) {
                    val config = response.toBucketConfiguration()
                    val proxy = proxyManager.getProxy(globalBucket) { CompletableFuture.completedFuture(config) }
                    proxy.replaceConfiguration(config, TokensInheritanceStrategy.AS_IS)
                } else {
                    if (response.rateLimit != null) {
                        val config = response.toBucketConfiguration()
                        val proxy = proxyManager.getProxy(request.identifier.toString()) {
                            CompletableFuture.completedFuture(config)
                        }
                        proxy.replaceConfiguration(config, TokensInheritanceStrategy.AS_IS)
                    }
                }

                deferred.complete(Unit)
            }
        }
    }

    private fun RequestResponse.toBucketConfiguration() = BucketConfiguration.builder()
        .addLimit {
            val limit = rateLimit!!
            val reset = reset!!
            it
                .capacity(limit.remaining.value)
                .refillIntervallyAligned(
                    limit.total.value,
                    (Clock.System.now() - reset.value).toJavaDuration(),
                    start.toJavaInstant()
                )
        }
        .build()
}
