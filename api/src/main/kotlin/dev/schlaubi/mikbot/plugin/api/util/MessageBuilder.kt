package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.followup.FollowupMessageBehavior
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder

/**
 * This is a message builder block (aka. the thing a [MessageSender] takes).
 */
public typealias MessageBuilder = suspend MessageCreateBuilder.() -> Unit

/**
 * This is a message edit builder block (aka. the thing a [MessageEditor] takes).
 */
public typealias MessageEditBuilder = suspend MessageModifyBuilder.() -> Unit

/**
 * Function which can send an interaction message (e.g. [respond]), which can be edited.
 */
public typealias EditableMessageSender = suspend (MessageBuilder) -> FollowupMessageBehavior

/**
 * Function which can send an interaction message (e.g. [respond]), which cannot be edited.
 */
public typealias MessageSender = suspend (MessageBuilder) -> Any

/**
 * Function that edits an existing message.
 */
public typealias MessageEditor = suspend (MessageEditBuilder) -> Unit

/**
 * Function translating the key in a group (e.g. [CommandContext.translate]).
 */
public typealias Translator = suspend (key: String, group: String) -> String

/**
 * A function creating a confirmation (e.g. [confirmation]).
 */
public typealias ConfirmationSender = suspend (MessageBuilder) -> Confirmation
