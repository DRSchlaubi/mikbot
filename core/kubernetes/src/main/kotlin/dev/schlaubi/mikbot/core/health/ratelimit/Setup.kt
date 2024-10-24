package dev.schlaubi.mikbot.core.health.ratelimit

import dev.kord.core.builder.kord.KordBuilder
import dev.kord.rest.request.KtorRequestHandler
import dev.schlaubi.mikbot.core.health.Config
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec

fun KordBuilder.setupDistributedRateLimiter() {
    val connection = RedisClient.create(Config.REDIS_URL)
        .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))
    val proxyManager = Bucket4jLettuce
        .casBasedBuilder(connection).build()

    val rateLimiter = DistributedRateLimiter(proxyManager)

    requestHandler {
        KtorRequestHandler(it.token, rateLimiter)
    }
}
