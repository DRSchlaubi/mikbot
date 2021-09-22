package dev.schlaubi.musicbot.core.io

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.musicbot.module.settings.BotUser
import org.litote.kmongo.coroutine.CoroutineCollection

suspend fun CoroutineCollection<BotUser>.findUser(user: UserBehavior) =
    findOneById(user.id.value) ?: BotUser().also { save(it) }
