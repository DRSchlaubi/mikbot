package space.votebot.commands

import space.votebot.commands.create.createCommands
import space.votebot.core.VoteBotModule

suspend fun VoteBotModule.commands() {
    createCommands()
    closeCommand()
}
