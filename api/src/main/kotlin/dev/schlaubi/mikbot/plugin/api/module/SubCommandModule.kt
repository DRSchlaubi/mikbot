package dev.schlaubi.mikbot.plugin.api.module

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand

public typealias GroupBuilder = suspend SlashGroup.() -> Unit

public abstract class SubCommandModule : Extension() {
    private val ephemeralSubCommandBodies = mutableListOf<EphemeralCommandPair<*>>()
    private val publicSubCommandBodies = mutableListOf<PublicCommandPair<*>>()
    private val groupBodies = mutableListOf<GroupPair>()
    public abstract val commandName: String
    public fun <T : Arguments> ephemeralSubCommand(
        argumentBody: (() -> T),
        body: suspend EphemeralSlashCommand<T>.() -> Unit
    ) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(argumentBody, body))
    }

    public fun ephemeralSubCommand(body: suspend EphemeralSlashCommand<Arguments>.() -> Unit) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(null, body))
    }

    public fun <T : Arguments> publicSubCommand(
        argumentBody: (() -> T),
        body: suspend PublicSlashCommand<T>.() -> Unit
    ) {
        publicSubCommandBodies.add(PublicCommandPair(argumentBody, body))
    }

    public fun publicSubCommand(body: suspend PublicSlashCommand<Arguments>.() -> Unit) {
        publicSubCommandBodies.add(PublicCommandPair(null, body))
    }

    public fun group(name: String, body: GroupBuilder) {
        groupBodies.add(GroupPair(name, body))
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

    public abstract suspend fun overrideSetup()
}

/**
 * Pair of a command configurator and it's arguments.
 *
 * @param T the [Arguments] type
 * @property argumentBody the command's argument body
 * @property commandBody the command body
 */
public class EphemeralCommandPair<T : Arguments>(
    private val argumentBody: (() -> T)?,
    private val commandBody: suspend EphemeralSlashCommand<T>.() -> Unit
) {
    public suspend fun SlashCommand<*, *>.add() {
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
public class PublicCommandPair<T : Arguments>(
    private val argumentBody: (() -> T)?,
    private val commandBody: suspend PublicSlashCommand<T>.() -> Unit
) {
    public suspend fun SlashCommand<*, *>.add() {
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


private data class GroupPair(val name: String, val body: GroupBuilder)
