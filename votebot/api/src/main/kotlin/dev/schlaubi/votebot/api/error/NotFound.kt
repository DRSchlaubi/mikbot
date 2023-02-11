package dev.schlaubi.votebot.api.error

class NotFoundException(message: String) : RuntimeException(message)

fun notFound(message: String): Nothing = throw NotFoundException(message)
