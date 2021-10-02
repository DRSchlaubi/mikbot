package dev.schlaubi.musicbot.module

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand

abstract class SubCommandModule : Extension() {
    protected val ephemeralSubCommandBodies = mutableListOf<EphemeralCommandPair<*>>()
    protected val publicSubCommandBodies = mutableListOf<PublicCommandPair<*>>()
    abstract val commandName: String
    fun <T : Arguments> ephemeralSubCommand(
        argumentBody: (() -> T),
        body: suspend EphemeralSlashCommand<T>.() -> Unit
    ) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(argumentBody, body))
    }

    fun ephemeralSubCommand(body: suspend EphemeralSlashCommand<Arguments>.() -> Unit) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(null, body))
    }

    fun <T : Arguments> publicSubCommand(
        argumentBody: (() -> T),
        body: suspend PublicSlashCommand<T>.() -> Unit
    ) {
        publicSubCommandBodies.add(PublicCommandPair(argumentBody, body))
    }

    fun publicSubCommand(body: suspend PublicSlashCommand<Arguments>.() -> Unit) {
        publicSubCommandBodies.add(PublicCommandPair(null, body))
    }

    final override suspend fun setup() {
        overrideSetup()

        ephemeralSlashCommand {
            name = commandName
            description = "<never used>"

            ephemeralSubCommandBodies.forEach { with(it) { add() } }
            publicSubCommandBodies.forEach { with(it) { add() } }
        }
    }

    abstract suspend fun overrideSetup()
}

/**
 * Pair of a command configurator and it's arguments.
 *
 * @param T the [Arguments] type
 * @property argumentBody the command's argument body
 * @property commandBody the command body
 */
class EphemeralCommandPair<T : Arguments>(
    private val argumentBody: (() -> T)?,
    private val commandBody: suspend EphemeralSlashCommand<T>.() -> Unit
) {
    suspend fun SlashCommand<*, *>.add() {
        if (argumentBody == null) {
            ephemeralSubCommand {
                @Suppress("UNCHECKED_CAST")
                commandBody(this as EphemeralSlashCommand<T>)
            }
        } else {
            ephemeralSubCommand(argumentBody) {
                commandBody()
            }
        }
    }
}

/**
 * Pair of a command configurator and it's arguments.
 *
 * @param T the [Arguments] type
 * @property argumentBody the command's argument body
 * @property commandBody the command body
 */
class PublicCommandPair<T : Arguments>(
    private val argumentBody: (() -> T)?,
    private val commandBody: suspend PublicSlashCommand<T>.() -> Unit
) {
    suspend fun SlashCommand<*, *>.add() {
        if (argumentBody == null) {
            publicSubCommand {
                @Suppress("UNCHECKED_CAST")
                commandBody(this as PublicSlashCommand<T>)
            }
        } else {
            publicSubCommand(argumentBody) {
                commandBody()
            }
        }
    }
}
