package space.votebot.commands.create

import space.votebot.core.VoteBotModule

suspend fun VoteBotModule.createCommands() {
    createCommand()
    yesNowCommand()
}
