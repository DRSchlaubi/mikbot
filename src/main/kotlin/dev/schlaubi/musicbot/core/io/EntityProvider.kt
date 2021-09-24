package dev.schlaubi.musicbot.core.io

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.musicbot.module.settings.BotGuild
import dev.schlaubi.musicbot.module.settings.BotUser
import org.litote.kmongo.coroutine.CoroutineCollection

suspend fun CoroutineCollection<BotUser>.findUser(user: UserBehavior) =
    findOneById(user.id.value) ?: BotUser(user.id).also { save(it) }

suspend fun CoroutineCollection<BotGuild>.findGuild(guild: GuildBehavior) =
    findOneById(guild.id.value) ?: BotGuild(guild.id).also { save(it) }
