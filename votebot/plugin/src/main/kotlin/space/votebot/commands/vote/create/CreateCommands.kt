package space.votebot.commands.vote.create

import space.votebot.core.VoteBotModule

suspend fun VoteBotModule.createCommands() {
    createCommand()
    yesNowCommand()
}
