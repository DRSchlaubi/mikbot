package dev.schlaubi.uno

import dev.schlaubi.uno.cards.ActionCard
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.DrawTwoCard
import dev.schlaubi.uno.cards.DrawingCard
import dev.schlaubi.uno.cards.PlayedCard
import dev.schlaubi.uno.cards.ReverseCard
import dev.schlaubi.uno.cards.SimpleCard
import dev.schlaubi.uno.cards.SkipCard
import dev.schlaubi.uno.cards.WildCard
import dev.schlaubi.uno.cards.WildCardDraw4
import dev.schlaubi.uno.exceptions.CardDoesntMatchException
import dev.schlaubi.uno.exceptions.PlayerDoesNotHaveCardException
import java.util.LinkedList

/**
 * The direction in which the game is going.
 */
public enum class Direction(private val oppositeName: String) {
    /**
     * Clockwise (index + 1).
     */
    CLOCKWISE("COUNTER_CLOCKWISE"),

    /**
     * Counter-clockwise (index - 1)
     */
    @Suppress("unused") // used via not()
    COUNTER_CLOCKWISE("CLOCKWISE");

    /**
     * Reverses this [Direction].
     */
    public operator fun not(): Direction = valueOf(oppositeName)
}

/**
 * Representation of a player (inheritable).
 */
public open class Player {
    public lateinit var deck: MutableList<Card>
    public var saidUno: Boolean = false
        internal set

    /**
     * Function called if this player was skipped.
     */
    public open fun onSkip(): Unit = Unit

    /**
     * FUnction called when the player forgot to say uno in [game].
     */
    public open fun forgotUno(game: Game<*>): Unit = Unit

    /**
     * Function called if the player won [place].
     */
    public open fun onWin(place: Int): Unit = Unit

    /**
     * Makes the player say uno.
     */
    public fun uno() {
        saidUno = true
    }

    /**
     * Plays [card] in [game] choosing [color] if possible.
     *
     * @throws PlayerDoesNotHaveCardException if the player doesn't have an instance of [card] in his deck
     * @throws CardDoesntMatchException if the card doesn't match the top card
     */
    public fun playCard(game: Game<*>, card: Card, color: UnoColor = UnoColor.BLUE) {
        if (!deck.remove(card)) {
            throw PlayerDoesNotHaveCardException(card)
        }

        game.playCard(this, card.play(color))
    }

    /**
     * Makes the player draw 1 card in [game].
     */
    public fun draw(game: Game<*>): Unit = game.drawCards(this, 1)
}

/**
 * Representation of a game of UNO.
 *
 * @param initialPlayers the initial amount of players
 *
 * @property players the [Players][Player] which are currently still in the game
 * @property wonPlayers [Players][Player] which already finished the game
 * @property direction the [Direction] in which the game is going
 */
public class Game<T : Player>(initialPlayers: List<T>) {
    private val deck = LinkedList(defaultUnoDeck)
    private val playerSequence = PlayerSequence()
    private val playedDeck: MutableList<PlayedCard> = mutableListOf()
    private val _players = ArrayList(initialPlayers)
    private val _wonPlayers = ArrayList<T>(initialPlayers.size - 1) // last player cannot win

    public var direction: Direction = Direction.CLOCKWISE
    public val players: List<T> get() = _players.toList()
    public val wonPlayers: List<T> get() = _wonPlayers.toList() // last player cannot win
    public val cardsPlayed: Int get() = playedDeck.size
    public var drawCardSum: Int = 0
        private set

    init {
        check(initialPlayers.size in 2..10) { "You need to be 2..10 players" }

        deck.shuffle() // Mix the cards

        // Hand out each player's initial cards
        players.forEach {
            it.deck = mutableListOf()
            drawCards(it, 7)
            it.deck.add(DrawTwoCard(UnoColor.YELLOW))
            it.deck.add(WildCardDraw4())
        }

        // Poll first card
        playedDeck.add(deck.poll().play(UnoColor.BLUE))
        // Play first card as first player
        playCard(players.first(), playedDeck.first())
    }

    /**
     * Whether this game is still running or not.
     */
    public val gameRunning: Boolean
        get() = playerSequence.hasNext()

    /**
     * The [PlayedCard] which is on top of the deck.
     */
    public val topCard: PlayedCard
        get() = playedDeck.last()

    /**
     * Skips the next player.
     */
    public fun skipPlayer(): Unit = nextPlayer().onSkip()

    /**
     * Obtains the next [Player].
     */
    public fun nextPlayer(): T = playerSequence.next()

    /**
     * Obtains the next player without progressing the player sequence.
     */
    public fun getNextPlayer(): T = playerSequence.nextWithoutProgress()

    /**
     * Forces [player] to be added to [wonPlayers].
     */
    public fun forceWin(player: T) {
        win(player)
    }

    public fun removePlayer(player: Player): Boolean =
        _players.remove(player)

    internal fun playCard(player: Player, card: PlayedCard) {
        // Check card matches
        if (!card.canBePlayedOn(topCard)) throw CardDoesntMatchException(topCard, card)

        // Check uno rule
        if (player.deck.size == 1 && !player.saidUno) {
            drawCards(player, 3)
            player.forgotUno(this)
        }

        // Play card

        if (card is ActionCard) {
            card.applyToGame(this)
        }
        if (card is DrawingCard) {
            if (card.canStackWith(topCard)) {
                drawCardSum += card.cards
            } else {
                drawSummedCards(player)
                drawCardSum = card.cards
            }
        } else if (drawCardSum >= 1) {
            drawSummedCards(player)
        }
        playedDeck.add(card)

        // Reset uno state
        player.saidUno = false

        // Check win condition
        if (player.deck.isEmpty()) {
            win(player)
        }
    }

    private fun win(player: Player) {
        _players.remove(player)
        @Suppress("UNCHECKED_CAST")
        _wonPlayers.add(player as T)
        player.onWin(wonPlayers.size)
    }

    private fun drawSummedCards(player: Player) {
        drawCards(player, drawCardSum)
        drawCardSum = 0
    }

    internal fun drawCards(player: Player, cards: Int) {
        if (drawCardSum >= 1 && cards != drawCardSum) {
            return drawSummedCards(player)
        }
        // Grab cards from played deck if this deck is empty
        if (cards > deck.size) {
            deck.addAll(playedDeck.shuffled())
        }

        val drawedCards = deck.poll(cards)
        player.deck.addAll(drawedCards)
    }

    private inner class PlayerSequence : Iterator<T> {
        var currentIndex = 0

        // Winning players get removed => only one player left means game ended
        override fun hasNext(): Boolean = _players.size > 1

        override fun next(): T {
            val player = players.getOrNull(currentIndex)
            if (direction == Direction.CLOCKWISE) {
                if (++currentIndex >= players.size) {
                    currentIndex = 0
                }
            } else {
                if (--currentIndex < 0) {
                    currentIndex = players.lastIndex
                }
            }
            return player ?: players[currentIndex] // if index is broken re-coerce it
        }

        fun nextWithoutProgress(): T {
            val indexNow = currentIndex
            val player = nextPlayer()
            currentIndex = indexNow

            return player
        }
    }
}

/**
 * The default deck of an uno game
 *
 * Reference: https://www.unorules.org/how-many-cards-in-uno/
 */
@OptIn(ExperimentalStdlibApi::class)
public val defaultUnoDeck: List<Card> = buildList(108) {
    UnoColor.values().forEach { color ->
        // each color has one 0 card
        add(SimpleCard(0, color))

        // each color has 2 cards for each number from 1 to 9
        for (number in 1..9) {
            repeat(2) {
                add(SimpleCard(number, color))
            }
        }

        // Each color has two of each colored action cards
        repeat(2) {
            add(DrawTwoCard(color))
            add(ReverseCard(color))
            add(SkipCard(color))
        }
    }

    // There is are 4 wild cards of each type
    repeat(4) {
        add(WildCard())
        add(WildCardDraw4())
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun <T> LinkedList<T>.poll(amount: Int) = buildList(amount) {
    repeat(amount) {
        add(poll())
    }
}
