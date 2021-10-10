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
import kotlin.random.Random

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
     * Function called when the player forgot to say uno in [game].
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
 * @property extreme enable extreme mode (60% of the times you draw no cards, but if you do you can draw up to 5)
 * @property flash enable flash mode (Player sequence is completely random)
 */
public class Game<T : Player>(initialPlayers: List<T>, public val extreme: Boolean = false, flash: Boolean = false) {
    private val playerSequence: PlayerSequence<T> = if (flash) FlashPlayerSequence() else NormalPlayerSequence()
    private val deck = LinkedList(with(playerSequence) {
        // e.g no skip/reverse in flash mode
        defaultUnoDeck.filterIncompatbile()
    })
    private val playedDeck: MutableList<PlayedCard> = mutableListOf()
    private val _players = ArrayList(initialPlayers)
    // last player cannot win
    private val _wonPlayers = ArrayList<T>(initialPlayers.size - 1)

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
            handOutCards(it, 7)
            UnoColor.values().forEach { color ->
                it.deck.add(SimpleCard(1, color))
            }
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
     * Reverses [direction].
     */
    public fun reverse() {
        direction = !direction
        if (players.size == 2) {
            skipPlayer() // If there are only 2 players, reverse essentially means skip
        }
    }

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

    public fun dropIn(player: Player, card: PlayedCard) {
        if (topCard != card) throw CardDoesntMatchException(topCard, card)
        playerSequence.lastIndex = _players.indexOf(player)
        playCard(player, card)
    }

    internal fun playCard(player: Player, card: PlayedCard) {
        // Check card matches
        if (!card.canBePlayedOn(topCard)) throw CardDoesntMatchException(topCard, card)

        // Check uno rule
        if (player.deck.size == 1 && !player.saidUno) {
            drawCards(player, 2)
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
            player.onWin(wonPlayers.size)
        }
    }

    private fun win(player: Player) {
        _players.remove(player)
        @Suppress("UNCHECKED_CAST")
        _wonPlayers.add(player as T)
    }

    private fun drawSummedCards(player: Player) {
        drawCards(player, drawCardSum)
        drawCardSum = 0
    }

    internal fun drawCards(player: Player, cards: Int) {
        if (extreme) {
            repeat(drawCardSum.coerceAtLeast(1)) {
                extremeDrawCards(player)
            }
        } else if (drawCardSum >= 1 && cards != drawCardSum) {
            return drawSummedCards(player)
        } else {
            handOutCards(player, cards)
        }
    }

    private fun extremeDrawCards(player: Player) {
        val random = Random.nextInt(1, 100)
        if (random < 65) return // 65% chance, I don't actually know if this is 65% chance because I suck at math, but let's just hope it is
        handOutCards(player, Random.nextInt(1, 6))
    }

    private fun handOutCards(player: Player, cards: Int) {

        // Grab cards from played deck if this deck is empty
        if (cards > deck.size) {
            deck.addAll(playedDeck.shuffled())
        }

        val drawedCards = deck.poll(cards)
        player.deck.addAll(drawedCards)
    }

    private inner class NormalPlayerSequence : PlayerSequence<T> {
        override var lastIndex = -1

        // Winning players get removed => only one player left means game ended
        override fun hasNext(): Boolean = _players.size > 1

        override fun next(): T {
            if (direction == Direction.CLOCKWISE) {
                if (++lastIndex >= players.size) {
                    lastIndex = 0
                }
            } else {
                if (--lastIndex < 0) {
                    lastIndex = players.lastIndex
                }
            }
            return players[lastIndex] // if index is broken re-coerce it
        }

        override fun nextWithoutProgress(): T {
            val indexNow = lastIndex
            val player = nextPlayer()
            lastIndex = indexNow

            return player
        }
    }

    /**
     * Sequence selecting players completely random, simmilar to the UNO variant UNO flash
     */
    private inner class FlashPlayerSequence : PlayerSequence<T> {
        // Winning players get removed => only one player left means game ended
        override fun hasNext(): Boolean = _players.size > 1

        override var lastIndex = -1

        override fun next(): T = _players.random()

        override fun nextWithoutProgress() =
            throw UnsupportedOperationException("Next player isn't known in flash mode!")

        override fun List<Card>.filterIncompatbile() = filterNot { it is SkipCard }

    }
}

private interface PlayerSequence<T : Player> : Iterator<T> {
    var lastIndex: Int

    fun nextWithoutProgress(): T

    fun List<Card>.filterIncompatbile() = this

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
