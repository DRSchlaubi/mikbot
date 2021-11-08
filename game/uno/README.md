# uno
This module houses an implementation of the [UNO](https://en.wikipedia.org/wiki/Uno_(card_game)) rules in Kotlin.

There are also partial implementations for the game derivatives UNO Extreme and UNO Flash plus a drop-in ability.

# Example usage
```kotlin
    val player1 = Player()
    val player2 = Player()

    val game = Game(listOf(player1, player2))

    player1.playCard(game, player1.deck.first { it.canBePlayedOn(game.topCard) })
    player2.playCard(game, player2.deck.first { it.canBePlayedOn(game.topCard) })
    // fast forward
    player1.uno()
    player1.playCard(game, player1.deck.first { it.canBePlayedOn(game.topCard) })
    player2.playCard(game, player2.deck.first { it.canBePlayedOn(game.topCard) })
    player1.playCard(game, player1.deck.first { it.canBePlayedOn(game.topCard) })

    println(game.wonPlayers)
```

# Concept
A game is considered to be running, until all but one player have 0 cards.
All players with 0 cards will be in `Game.wonPlayers`

For more information please refer to the [documentation](https://mikbot.schlau.bi/game/uno) or take a look at the [uno plugin](../uno-game)
