package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.message

fun Arguments.poll() = message("poll", "The message URL to a message of the poll")
