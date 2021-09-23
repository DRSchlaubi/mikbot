package dev.schlaubi.musicbot.utils

import dev.kord.core.behavior.interaction.FollowupMessageBehavior
import dev.kord.rest.builder.message.create.MessageCreateBuilder

typealias MessageBuilder = suspend MessageCreateBuilder.() -> Unit
typealias MessageSender = suspend (MessageBuilder) -> FollowupMessageBehavior
