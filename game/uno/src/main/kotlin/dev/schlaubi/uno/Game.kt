package dev.schlaubi.uno

import dev.schlaubi.uno.cards.*
import dev.schlaubi.uno.exceptions.CardDoesNotMatchException
import dev.schlaubi.uno.exceptions.PlayerDoesNotHaveCardException
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.random.Random

/** The direction in which the game is going. */
public enum class Direction(private val oppositeName: String) {
    /** Clockwise (index + 1). */
    CLOCKWISE("COUNTER_CLOCKWISE"),

    /** Counter-clockwise (index - 1) */
    @Suppress("unused") // used via not()
    COUNTER_CLOCKWISE("CLOCKWISE");

    /** Reverses this [Direction]. */
    public operator fun not(): Direction = valueOf(oppositeName)
}

/** Representation of a player (inheritable). */
public open class Player {
    public lateinit var deck: MutableList<Card>
    public var saidUno: Boolean = false
        internal set

    /** Function called if this player was skipped. */
    public open fun onSkip(): Unit = Unit

    /** Function called when the player forgot to say uno in [game]. */
    public open fun forgotUno(game: Game<*>): Unit = Unit

    /** Function called if the player won [place]. */
    public open fun onWin(place: Int): Unit = Unit

    /**
     * Callback when player is allowed to see [otherPlayer]'s cards.
     */
    public open fun onVisibleCards(otherPlayer: Player): Unit = Unit

    /**
     * Callback to indicate, that the players cards changed.
     */
    public open fun refreshCards(): Unit = Unit

    /** Makes the player say uno. */
    public fun uno() {
        saidUno = true
    }

    /**
     * Plays [card] in [game] choosing [color] if possible.
     *
     * @throws PlayerDoesNotHaveCardException if the player doesn't have an instance of [card] in
     * his deck
     * @throws CardDoesNotMatchException if the card doesn't match the top card
     */
    public suspend fun playCard(game: Game<*>, card: Card, color: UnoColor = UnoColor.BLUE) {
        if (!deck.remove(card)) {
            throw PlayerDoesNotHaveCardException(card)
        }

        game.playCard(this, card.play(color))
    }

    /**
     * Plays a [CardSwitching7] for [player].
     *
     * @see playCard
     */
    public suspend fun playCard(game: Game<*>, card: SimpleCard, player: Player) {
        require(card.number == 7) { "Card needs to be a 7 for switching" }
        if (!deck.remove(card)) {
            throw PlayerDoesNotHaveCardException(card)
        }

        game.playCard(this, CardSwitching7(player, card.color))
    }

    /** Makes the player draw 1 card in [game]. */
    public fun draw(game: Game<*>): Unit = game.drawCards(this, 1)

    /**
     * Callback if a [SlapCard] is played.
     *
     * This expects to call [SlapContext.slap]
     */
    public open fun onSlap(context: SlapContext): Unit = Unit

    /**
     * Callback if a [SlapCard] is played and you were to slow.
     */
    public open fun onSlapEnd(): Unit = Unit
}

/**
 * Representation of a game of UNO.
 *
 * @param initialPlayers the initial amount of players
 *
 * @property players the [Players][Player] which are currently still in the game
 * @property wonPlayers [players][Player] which already finished the game
 * @property direction the [Direction] in which the game is going
 * @property allowDrawCardStacking whether to allow the "stacking" of [DrawingCards][DrawingCard] or not
 * @property allowBluffing allows other players to challenge a [WildCardDraw4].
 *                  Meaning the challenged player needs to show their cards, to proof they actually have a card in the
 *                  selected color, if yes, the challenger needs to draw 6 cards, if not the challenged player needs
 *                  to draw 4 cards
 * @param extreme enable extreme mode (60% of the times you draw no cards, but if you do you can
 * draw up to 5)
 * @param flash enable flash mode (Player sequence is completely random)
 * @param drawUntilPlayable forces players to draw until they have at least one playable card
 * @param useSpecial7And0 See [CardSwitching7] and [CardRotating0]
 */
public class Game<T : Player>(
    initialPlayers: List<T>,
    private val extreme: Boolean = false,
    flash: Boolean = false,
    private val drawUntilPlayable: Boolean = false,
    private val allowDrawCardStacking: Boolean = true,
    private val allowBluffing: Boolean = false,
    private val useSpecial7And0: Boolean = false
) {

    /**
     * Whether the current top card can be challenged.
     */
    public val canBeChallenged: Boolean
        get() = allowBluffing && challengePossible && topCard is WildCardDraw4
    private var challengePossible = true
    private val playerSequence: PlayerSequence<T> =
        if (flash) FlashPlayerSequence() else NormalPlayerSequence()
    private val deck = getDefaultUnoDeck(extreme, flash, useSpecial7And0)
    internal val playedDeck: MutableList<PlayedCard> = mutableListOf()

    internal val orderedPlayers: List<T>
        get() = (playerSequence as? OrderedPlayerSequence<T>)?.playersInOrder
            ?: error("orderedPlayers is only available when using OrderedPlayerSequence implementations")
    private val _players = ArrayList(initialPlayers)

    // last player cannot win
    private val _wonPlayers = ArrayList<T>(initialPlayers.size - 1)

    public var direction: Direction = Direction.CLOCKWISE
    public val players: List<T>
        get() = _players.toList()
    public val wonPlayers: List<T>
        get() = _wonPlayers.toList() // last player cannot win
    public val cardsPlayed: Int
        get() = playedDeck.size
    public var drawCardSum: Int = 0

    private var challengeablePlayer: Player? = null

    init {
        check(initialPlayers.size in 2..10) { "You need to be 2..10 players" }

        deck.shuffle() // Mix the cards

        // Hand out each player's initial cards
        players.forEach {
            it.deck = ArrayList(7)
            handOutCards(it, 7)
        }

        // Play first card as first player
        runBlocking {
            playedDeck.add(deck.pollNonSlapCard().play(UnoColor.BLUE))
            playCard(players.first(), playedDeck.first())
            playedDeck.removeAt(0)
        }
    }

    /** The index of the last player, having a turn. */
    public val lastPlayerIndex: Int
        get() = playerSequence.lastIndex

    /** Whether this game is still running or not. */
    public val gameRunning: Boolean
        get() = playerSequence.hasNext()

    /** The [PlayedCard] which is on top of the deck. */
    public val topCard: PlayedCard
        get() = playedDeck.last()

    /** Skips the next player. */
    public fun skipPlayer(): Unit = nextPlayer().onSkip()

    /** Reverses [direction]. */
    public fun reverse() {
        direction = !direction
        if (players.size == 2) {
            // If there are only 2 players, reverse essentially means skip
            nextPlayer()
        }
    }

    /** Obtains the next [Player]. */
    public fun nextPlayer(): T = playerSequence.next()

    /** Obtains the next player without progressing the player sequence. */
    public fun getNextPlayer(): T = playerSequence.nextWithoutProgress()

    /** Forces [player] to be added to [wonPlayers]. */
    public fun forceWin(player: T) {
        win(player)
    }

    /**
     * Removes [player] from the game.
     */
    public fun removePlayer(player: Player): Boolean = _players.remove(player)

    /**
     * Let's [player] drop in a [card].
     */
    public suspend fun dropIn(player: Player, card: PlayedCard) {
        if (topCard != card) throw CardDoesNotMatchException(topCard, card)
        if (player.deck.size == 2) player.uno()
        playerSequence.lastIndex = _players.indexOf(player)
        player.playCard(this, card)
    }

    /**
     * Challenges the current [WildCardDraw4] card.
     *
     * **Note:** Calls this before [nextPlayer]
     * @see allowBluffing
     */
    public fun challenge(player: Player) {
        require(allowBluffing) { "allowBluffing is required" }
        require(topCard is WildCardDraw4) { "You can only challenge WildCardDraw4 cards" }

        val challengedPlayer = challengeablePlayer ?: error("No challengable player in context")
        drawCardSum = 0
        player.onVisibleCards(challengedPlayer)
        if (challengedPlayer.deck.none { (it as? ColoredCard)?.color == topCard.color }) {
            drawCards(challengedPlayer, 4)
        } else {
            drawCards(player, 6)
        }
        challengePossible = false
    }

    internal suspend fun playCard(player: Player, card: PlayedCard) {
        // Check card matches
        if (!card.canBePlayedOn(topCard)) throw CardDoesNotMatchException(topCard, card)

        challengePossible = true
        if (card is WildCardDraw4) {
            challengeablePlayer = player
        }

        // Check uno rule
        if (player.deck.size == 1 && !player.saidUno) {
            drawCards(player, 2)
            player.forgotUno(this)
        }

        // Play card
        if (card is ActionCard) {
            card.applyToGame(this, player)
        }
        if (card is DrawingCard) {
            if (allowDrawCardStacking && card.canStackWith(topCard)) {
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
        @Suppress("UNCHECKED_CAST") _wonPlayers.add(player as T)
    }

    private fun drawSummedCards(player: Player) {
        drawCards(player, drawCardSum)
        drawCardSum = 0
    }

    internal fun drawCards(player: Player, cards: Int) {
        do {
            if (extreme) {
                repeat(drawCardSum.coerceAtLeast(cards)) { extremeDrawCards(player) }
            } else if (drawCardSum >= 1 && cards != drawCardSum) {
                return drawSummedCards(player)
            } else {
                handOutCards(player, cards)
            }
        } while (drawUntilPlayable && player.deck.none { it.canBePlayedOn(topCard) })
    }

    private fun extremeDrawCards(player: Player) {
        val random = Random.nextInt(1, 100)
        if (random < 65)
            return // 65% chance, I don't actually know if this is 65% chance because I suck at
        // math, but let's just hope it is
        handOutCards(player, Random.nextInt(1, 4))
        drawCardSum = 0
    }

    private fun handOutCards(player: Player, cards: Int) {
        // Grab cards from played deck if this deck is empty
        if (cards > deck.size) {
            deck.addAll(playedDeck.shuffled())
        }

        val drawedCards = deck.poll(cards)
        player.deck.addAll(drawedCards)
    }

    private tailrec fun LinkedList<Card>.pollNonSlapCard(): Card {
        val card = poll()
        return if (card is SlapCard) {
            playedDeck.add(card)

            pollNonSlapCard()
        } else {
            card
        }
    }

    private inner class NormalPlayerSequence private constructor(initialLastIndex: Int) : OrderedPlayerSequence<T> {
        constructor() : this(-1)

        override var lastIndex = initialLastIndex

        override val playersInOrder: List<T>
            get() {
                val copy = NormalPlayerSequence(lastIndex)

                return copy.asSequence().take(players.size).toList()
            }

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

    /** Sequence selecting players completely random, simmilar to the UNO variant UNO flash */
    private inner class FlashPlayerSequence : PlayerSequence<T> {
        // Winning players get removed => only one player left means game ended
        override fun hasNext(): Boolean = _players.size > 1

        override var lastIndex = -1

        // If someone uses nextWithoutProgress() we need to ensure to use the predicted player
        private var nextPlayer: T? = null

        override fun next(): T = nextPlayer?.also { nextPlayer = null } ?: _players.random()

        override fun nextWithoutProgress(): T = nextPlayer ?: next().also { nextPlayer = it }
    }
}

private interface PlayerSequence<T : Player> : Iterator<T> {
    var lastIndex: Int

    fun nextWithoutProgress(): T
}

private interface OrderedPlayerSequence<T : Player> : PlayerSequence<T> {
    val playersInOrder: List<T>
}

@OptIn(ExperimentalStdlibApi::class)
private val extremeDeck: List<Card> = buildList(UnoColor.values().size * 2) {
    UnoColor.values().forEach { color ->
        repeat(2) {
            add(DiscardAllCardsCard(color))
        }
    }
}

/**
 * Provides a [LinkedList] containing all cards applicable for the specified rules.
 */
public fun getDefaultUnoDeck(extreme: Boolean, flash: Boolean, useSpecial7And0: Boolean): LinkedList<Card> {
    val deck = LinkedList(defaultUnoDeck)
    if (extreme) {
        deck.addAll(extremeDeck)
    }
    if (flash) {
        deck.removeIf { it is ReverseCard }
        val slapCards = UnoColor.values().flatMap { color ->
            (0 until 2).map {
                SlapCard(color)
            }
        }

        deck.addAll(slapCards)
    }

    if (useSpecial7And0) {
        deck.replaceAll {
            val simpleCard = it as? SimpleCard

            if (simpleCard?.number == 0) {
                CardRotating0(simpleCard.color)
            } else {
                it
            }
        }
    }

    return deck
}

/**
 * The default deck of an uno game
 *
 * Reference: https://www.unorules.org/how-many-cards-in-uno/
 */
@OptIn(ExperimentalStdlibApi::class)
public val defaultUnoDeck: List<Card> =
    buildList(108) {
        UnoColor.values().forEach { color ->
            // each color has one 0 card
            add(SimpleCard(0, color))

            // each color has 2 cards for each number from 1 to 9
            for (number in 1..9) {
                repeat(2) { add(SimpleCard(number, color)) }
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
