package dev.schlaubi.mikbot.game.trivia

import com.kotlindiscord.kord.extensions.checks.hasRole
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.trivia.game.TriviaGame
import dev.schlaubi.mikbot.game.trivia.open_trivia.Category
import dev.schlaubi.mikbot.game.trivia.open_trivia.Difficulty
import dev.schlaubi.mikbot.game.trivia.open_trivia.Type
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.litote.kmongo.coroutine.CoroutineCollection

class StartTriviaArguments : Arguments() {
    val amount by defaultingInt {
        name = "amount"
        description = "Amount of questions"

        defaultValue = 10
    }

    val category by optionalEnumChoice<Category> {
        name = "category"
        description = "Only play questions from a specific category"
        typeName = "Category"
    }

    val difficulty by optionalEnumChoice<Difficulty> {
        name = "difficulty"
        description = "Only play questions of a specific difficulty"
        typeName = "Difficulty"
    }

    val type by optionalEnumChoice<Type> {
        name = "type"
        description = "Only play questions of a specific type"
        typeName = "Type"
    }
}

class TriviaModule : GameModule<MultipleChoicePlayer, TriviaGame>() {
    override val name: String = "trivia"
    override val bundle: String = "trivia"
    override val gameStats: CoroutineCollection<UserGameStats> = TriviaDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand(
            "trivia.game.title", "trivia", ::StartTriviaArguments, {

                val locale = event.interaction.guildLocale?.convertToISO()?.asJavaLocale()
                    ?: bot.settings.i18nBuilder.defaultLocale

                val questions = try {
                    QuestionContainer(
                        arguments.amount,
                        arguments.difficulty,
                        arguments.category,
                        arguments.type,
                        locale,
                        translationsProvider,
                        this@TriviaModule
                    )
                } catch (e: IllegalArgumentException) {
                    discordError(translate("commands.trivia.start.no_questions", bundle))
                }

                questions to locale
            },
            { (questionContainer, _), message, thread ->
                TriviaGame(
                    thread,
                    message,
                    translationsProvider,
                    user,
                    this@TriviaModule,
                    arguments.amount,
                    questionContainer
                )
            }
        )

        stopGameCommand()
        profileCommand()
        leaderboardCommand("trivia.stats.title")
    }
}
