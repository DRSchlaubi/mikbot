# Game development guide

# Index

- [Features](#features)
- [Dependencies](#dependencies)
- [Concepts](#concepts)
- [Multiple choice game](#multiple-choice-game)
    - [Modify the game mechanics](#modify-the-game-mechanics)
        - [Show other users answers](#show-other-users-answers)
        - [Manipulate points distribution](#manipulate-points-distribution)
- [Hangman (example game)](#hangman-example-game)
- [Auto-joinable game](#auto-joinable-games)
- [Making your game re-matchable](#making-your-game-re-matchable)
- [Making a game with controls](#making-a-game-with-controls)
    - [Making a game with controls auto joinable](#making-a-game-with-controls--auto-joinable)
    - [Making a game with controls re-matchable](#making-a-game-with-controls-auto-rematchable)

# Features

- Lobby handling
- Statistics
- Rematches
- Controls in ephemeral Discord messages

# Dependencies

There are 2 game libraries

- `game-api`: Core APIs for all games: `plugin("dev.schlaubi", "mikbot-game-api", "<version>")`
- `multiple-choice-game`: Skeleton for a multiple choice game based on game-api:
  `plugin("dev.schlaubi", "mikbot-multiple-choice-game", "<version>")`

# Concepts

A game has two core concepts a Player and a Game itself

The `GameModule` class is a KordEx extensions, housing all of your commands, it provides an existing implementation for

- start
- stop
- leaderboard
- stats

Please read the [hangman](#hangman-example-game) section to learn how to implement it

The `AbstractGame` class and therefore your implementation of it houses the game logic, meaning rules, flow and so on.
It requires you to have the following requirements

```kotlin
// This class doesn't fully implement AbstractGame, it only shows the requirements your game needs
// For a full implementation read the Hangman section
class DummyGame : AbstractGame() {
    override val playerRange = 1..Int.MAX_VALUE // this means the game can have unlimited players

    // this gets called at the end of the game to handle stats
    // list order = rank
    // e.g. wonPlayers[0] = firstPlayers
    // if a player finishes the game add them to this list, if a player d.n.f. don't
    override lateinit var wonPlayers: List<DummyPlayer>

    // Method used to add new players after clicking the join game button
    override suspend fun obtainNewPlayer(
        user: User, // the user who clicked the button
        ack: EphemeralMessageInteractionResponseBehavior, // the deferred response of that click
        loading: FollowupMessage, // A followUp with a loading message
        userLocale: Locale? // the locale of the user if Discord specifies it
    ): T = MyPlayer()

    // This functions suspends until your game ends, if this functions finishes, the game ends
    abstract override suspend fun runGame() {
        wonPlayers = listOf(players.random()) // very very very fun game, it's just random chance
    }
}
```

A game is also a `CoroutineScope` you should use for all child tasks

The `Player` interface houses all player logic, which is mostly controls logic, if you game doesn't have player-specific
controls (like an UNO card deck) you can implement it like this

```kotlin
class MyPlayer(override val user: UserBehavior) : Player
```

# Multiple choice game

Multiple choice games are probably the easiest games as they just require a question container, which is essentially a
list

The most basic implementation would be

```kotlin
data class Question(
    override val title: String,
    override val correctAnswer: String,
    override val incorrectAnswers: List<String>
) : Question


class ListContainer<Q : Question>(private val container: List<Q>) : QuestionContainer<Q>, List<Q> by container

// Order of answers will be shuffled at game time automatically
val questionContainer = ListContainer(
    listOf(
        Question("Is Apple Trash?", "Yes", listOf("No", "Maybe")),
        Question("Is Google Trash?", "No", listOf("Yes", "Maybe")),
    )
)

class Game(
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    override val thread: ThreadChannelBehavior, host: UserBehavior,
    module: GameModule<MultipleChoicePlayer, AbstractGame<MultipleChoicePlayer>>,
) : MultipleChoiceGame<MultipleChoicePlayer, Question, QuestionContainer<Question>>(
    host, module, questionContainer.size, questionContainer,
) {
    override suspend fun EmbedBuilder.addQuestion(question: Question, hideCorrectAnswer: Boolean) {
        description = question.title

        if (!hideCorrectAnswer) {
            field {
                name = "Correct answer"
                value = "SIS IS REIT"
            }
        }
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?,
    ): MultipleChoicePlayer = MultipleChoicePlayer(user)
}
```

Now read the [game module section](#game-module)

## Modify the game mechanics

Per default users get awarded 1 point per correct answer and are ranked per average response time.
You can find that
implementation [here](https://github.com/DRSchlaubi/mikbot/blob/main/game/multiple-choice-game/src/main/kotlin/dev/schlaubi/mikbot/game/multiple_choice/mechanics/DefaultGameMechanics.kt)

You can modify these mechanics by implementing
the [GameMechanics interface](https://github.com/DRSchlaubi/mikbot/blob/main/game/multiple-choice-game/src/main/kotlin/dev/schlaubi/mikbot/game/multiple_choice/mechanics/GameMechanics.kt)

### Show other users answers

If you want to show other users answers (but not their correctness), you can overwrite the `showAnswersAfter` property
and set it to a `Duration` like 30.minutes

### Manipulate points distribution

If you want to distribute points differently, you can implement
the [PointsDistributor interface](https://github.com/DRSchlaubi/mikbot/blob/main/game/multiple-choice-game/src/main/kotlin/dev/schlaubi/mikbot/game/multiple_choice/mechanics/GameMechanics.kt#L44-L63)
or use
the [StreakBasedGameMechanics](https://github.com/DRSchlaubi/mikbot/blob/main/game/multiple-choice-game/src/main/kotlin/dev/schlaubi/mikbot/game/multiple_choice/mechanics/StreakBasedGameMechanics.kt)

# Hangman (example game)

First we need to define a player class like this

```kotlin
// We don't need any specific controls, so we just use the default implementation
class HangmanPlayer(override val user: UserBehavior) : Player
```

Then we need to implement game

```kotlin
class HangmanGame(
    host: UserBehavior,
    module: HangmanModule,
    override val welcomeMessage: Message,
    override val thread: ThreadChannelBehavior,
    override val translationsProvider: TranslationsProvider,
) : SingleWinnerGame<HangmanPlayer>(host, module.asType) {
    private val wordOwner = lastWinner ?: host
    override val playerRange: IntRange = 2..Int.MAX_VALUE // You need at least a wordOwner and a guesser

    // This object is used to suspend the runGame() function until ther game is over
    private val gameCompleter by lazy { CompletableDeferred<Unit>() }
    private var state: GameState = GameState.WaitingForWord

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: dev.kord.common.Locale?,
    ): HangmanPlayer = HangmanPlayer(user)

    // Some functions have been omitted, you can find them here
    // https://github.com/DRSchlaubi/mikbot/blob/main/game/googologo/src/main/kotlin/dev/schlaubi/mikbot/game/hangman/game/HangmanGame.kt
    override suspend fun runGame() = coroutineScope {
        welcomeMessage.edit { components = mutableListOf() } // Remove all existing components
        val word = retrieveWord() ?: return@coroutineScope // wait for wordOwner to DM the bot
        startGame(this@HangmanGame, word)

        if (state is GameState.Guessing) {
            // wait for game to finish
            gameCompleter.await()
            cancel() // kill orphans
        }
    }

    // This is just pseudo-code for simplicity
    private fun startGame(scope: CorutineScope, word: String) {
        onEachMessage { message ->
            if (message.isChar()) {
                if (char in word) {
                    updateGameState() // reveal letters or win if word is done
                } else {
                    updateGameState() // Draw hangman
                }
            } else {
                if (guess == word) {
                    updateGameState() // win
                } else {
                    updateGameState() // Draw hangman
                }
            }
        }
    }
}
```

# Game module

After writing your game logic you can a database to store your stats

```kotlin
object HangmanDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("hangman_stats")
}
```

Then you can create the module

```kotlin
class HangmanModule : GameModule<HangmanPlayer, HangmanGame>() {
    override val name: String = "googologo"
    override val bundle: String = "hangman" // bundle used for translate calls
    override val gameStats: CoroutineCollection<UserGameStats> = HangmanDatabase.stats // reference the db from above

    @OptIn(PrivilegedIntent::class)
    override suspend fun gameSetup() {
        // We receive the guesses as guild messages, so we need to add these intents
        intents.add(Intent.GuildMessages)
        intents.add(Intent.MessageContent)

        // these 3 commands are very simple
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
    }
}
```

The startGameCommand is a bit more complicated, the simplest version is this one, which we can use for hangman

```kotlin
startGameCommand(
    "hangman.game.title", // title of the game embed
    "googologo", // name of the game thread
    { message, thread ->
        // lambda converting taking the welcome message and the game thread to create a game instance
        HangmanGame(null, user, this@HangmanModule, message, thread, translationsProvider)
    }
)
```

We can also require some [checks](https://kordex.kotlindiscord.com/en/concepts/checks)

```kotlin
startGameCommand(
    sameAsAbove,
    { hasPermission(Permission.Administrator) }
)
```

Or add additional [arguments](https://kordex.kotlindiscord.com/en/concepts/converters)

```kotlin
class HangmanStartArguments : Arguments() {
    val maxGuesses by int {
        name = "max_guesses"
        description = "The maximum amount of guesses before the word owner wins"
    }
}

startGameCommand(
    sameAsAbove,
    ::Arguments
    { message, thread ->
        // lambda converting taking the welcome message and the game thread to create a game instance
        HangmanGame(null, user, this@HangmanModule, message, thread, translationsProvider, arguments.maxGuesses)
    }
)
```

The full call would be, but the first one would work as well

```kotlin
startGameCommand(
    "hangman.game.title", // title of the game embed
    "googologo", // name of the game thread
    ::Arguments
    { message, thread ->
        // lambda converting taking the welcome message and the game thread to create a game instance
        HangmanGame(null, user, this@HangmanModule, message, thread, translationsProvider)
    },
    { hasPermission(Permission.Administrator) }
)
```

# Auto joinable games

Auto-joinable means that the creator of the game doesn't need to join the game manually by clicking the join button

In order to support that in our game, we would need to change our Game class like this

```kotlin
class HangmanGame(...) : ..., AutoJoinableGame<HangmanPlayer> {
    // This function can create players without a button click/ack
    override fun obtainNewPlayer(user: User): HangmanPlayer = HangmanPlayer(user)
}
```

# Making your game re-matchable

The api can add a "rematch" button at the end of each game, in order for that to work, we need to implement the
`Rematchable` interface, which provides a way to create a new dry (meaning with no game state) copy of a game

```kotlin
class HangmanGame(...) : ..., Rematchable<HangmanPlayer, HangmanGame> {
    override val rematchThreadName: String = "googologo-rematch"

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): HangmanGame {
        // Create a new game in the new thread
        val game = HangmanGame(
            winner!!.user, host, module as HangmanModule, welcomeMessage, thread, translationsProvider
        )
        // Add all current players
        val actualPlayers = players + HangmanPlayer(wordOwner)
        game.players.addAll(actualPlayers)

        return game
    }
}
```

# Making a game with controls

Some games (like [uno](../uno-game)) require controls (like a card deck) that only the user can see, the ControlledGame
interface can help you to streamline this experience

First we need to make a controlled player

```kotlin
interface Player(
    override val ack: MessageInteractionResponseBehavior,
    override var controls: FollowupMessage,
    override val discordLocale: Locale?,
    override val game: AbstractGame<*>
) : ControlledPlayer {

    /**
     * Requests new controls for this placer.
     */
    suspend fun resendControls(ack: EphemeralMessageInteractionResponseBehavior) {
        controls = ack.followUp {
            // controls
        }
    }
}
```

Then simply implement ControlledGame<Player> and the plugin will do the rest

## Making a game with controls  auto joinable

the AutoJoinable interface requires you to provie a function creating players without an ack, since that doesn't work
with controlled games, the ControlledGame interface provides its own auto-join, using the click of the start button
as the interaction, to use it simply not disable it by setting `supportsAutoJoin` to false

## Making a game with controls auto rematchable

The ControlledGame interface provides an askForRematch function which you can use to gather new button interactions for
the new game

```kotlin
    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): DiscordUnoGame {
    val game = DiscordUnoGame(...)
    if (!askForRematch(thread, game)) {
        discordError("Game could not restart")
    }

    return game
}
```
