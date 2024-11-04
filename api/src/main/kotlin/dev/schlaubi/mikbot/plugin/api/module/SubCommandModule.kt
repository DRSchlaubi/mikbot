package dev.schlaubi.mikbot.plugin.api.module

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.*
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.extensions.unsafeSubCommand
import dev.schlaubi.mikbot.plugin.api.PluginContext

/**
 * Builder lambda for slash command groups.
 */
public typealias GroupBuilder = suspend SlashGroup.() -> Unit

/**
 * Bot [Extension] which registers all commands als subCommands.
 */
public abstract class SubCommandModule(context: PluginContext) : MikBotModule(context) {
    private val ephemeralSubCommandBodies = mutableListOf<EphemeralCommandPair<*>>()
    private val publicSubCommandBodies = mutableListOf<PublicCommandPair<*>>()
    private val groupBodies = mutableListOf<GroupPair>()
    @OptIn(UnsafeAPI::class)
    private val unsafeSubCommandBodies = mutableListOf<UnsafeCommandPair<*>>()

    /**
     * The name of the root command.
     */
    public abstract val commandName: Key

    public fun <T : Arguments> ephemeralSubCommand(
        argumentBody: (() -> T),
        body: suspend EphemeralSlashCommand<T, *>.() -> Unit,
    ) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(argumentBody, body))
    }

    public fun ephemeralSubCommand(body: suspend EphemeralSlashCommand<Arguments, *>.() -> Unit) {
        ephemeralSubCommandBodies.add(EphemeralCommandPair(null, body))
    }

    public fun <T : Arguments> publicSubCommand(
        argumentBody: (() -> T),
        body: suspend PublicSlashCommand<T, *>.() -> Unit,
    ) {
        publicSubCommandBodies.add(PublicCommandPair(argumentBody, body))
    }

    public fun publicSubCommand(body: suspend PublicSlashCommand<Arguments, *>.() -> Unit) {
        publicSubCommandBodies.add(PublicCommandPair(null, body))
    }

    public fun group(name: String, body: GroupBuilder) {
        groupBodies.add(GroupPair(name, body))
    }

    @UnsafeAPI
    public fun unsafeSubCommand(body: UnsafeSlashCommand<Arguments, *>.() -> Unit) {
        unsafeSubCommandBodies.add(UnsafeCommandPair(null, body))
    }

    public open fun SlashCommand<*, *, *>.commandSettings(): Unit = Unit
    @OptIn(UnsafeAPI::class)
    final override suspend fun setup() {
        overrideSetup()

        ephemeralSlashCommand {
            name = commandName
            description = "<not used>".toKey()
            commandSettings()


            ephemeralSubCommandBodies.forEach { with(it) { add() } }
            publicSubCommandBodies.forEach { with(it) { add() } }
            unsafeSubCommandBodies.forEach { with(it) { add() } }
        }
    }

    /**
     * Additional [setup] instructions.
     */
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
    private val commandBody: suspend EphemeralSlashCommand<T, *>.() -> Unit,
) {
    public suspend fun SlashCommand<*, *, *>.add() {
        if (argumentBody == null) {
            ephemeralSubCommand {
                @Suppress("UNCHECKED_CAST")
                commandBody(this as EphemeralSlashCommand<T, *>)
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
    private val commandBody: suspend PublicSlashCommand<T, *>.() -> Unit,
) {
    public suspend fun SlashCommand<*, *, *>.add() {
        if (argumentBody == null) {
            publicSubCommand {
                @Suppress("UNCHECKED_CAST")
                commandBody(this as PublicSlashCommand<T, *>)
            }
        } else {
            publicSubCommand(argumentBody) {
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
@UnsafeAPI
public class UnsafeCommandPair<T : Arguments>(
    private val argumentBody: (() -> T)?,
    private val commandBody: suspend UnsafeSlashCommand<T, *>.() -> Unit,
) {
    public suspend fun SlashCommand<*, *, *>.add() {
        if (argumentBody == null) {
            unsafeSubCommand {
                @Suppress("UNCHECKED_CAST")
                commandBody(this as UnsafeSlashCommand<T, *>)
            }
        } else {
            unsafeSubCommand(argumentBody) {
                commandBody()
            }
        }
    }
}

private data class GroupPair(val name: String, val body: GroupBuilder)
