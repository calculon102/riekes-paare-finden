package de.pixelgerecht.riekespaarefinden.memory

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Game(
    deckToUse: Deck,
    motivesToUse: Int,
    shuffleSeed: Long = System.currentTimeMillis()
) : Serializable {
    val cards: List<Motive> = shuffleCards(deckToUse, motivesToUse, shuffleSeed)

    private val scores: MutableMap<Player, Int> = initScores()

    private val visibilityByIndex: MutableMap<Int, Boolean> = createVisibilityMap(cards)

    private var currentPlayer: Player = Player.ONE

    fun getScoreOf(player: Player): Int = scores.getOrElse(
        player,
        { throw IllegalArgumentException("Unsupported player $player for score") })


    /**
     * Current player chooses two cards to turn.
     *
     * If these are a pair, the players score will increase and he may continue turning cards with
     * this method.
     *
     * If not, the next player will turn cards on a successive call.
     *
     * @param firstIndex First index of a hidden card to turn
     * @param secondIndex Second index of hidden card to turn
     * @return Boolean true if turned cards are a pair.
     */
    fun turnCards(firstIndex: Int, secondIndex: Int): TurnedCards {
        if (firstIndex < 0 || secondIndex < 0) {
            throw IllegalArgumentException(
                "Index lower than 0 given: firstindex=" + firstIndex + " , secondIndex=" + secondIndex
            )
        }

        if (firstIndex >= visibilityByIndex.size || secondIndex  >= visibilityByIndex.size) {
            throw IllegalArgumentException(
                "Index greater than card-count of " + visibilityByIndex.size + " given: " +
                        "firstindex=" + firstIndex + " , secondIndex=" + secondIndex
            )
        }

        if (visibilityByIndex[firstIndex] == true || visibilityByIndex[secondIndex] == true) {
            throw IllegalArgumentException(
                "At least one of the given indices is already for a visible card: "
                        + firstIndex + "=" + visibilityByIndex[firstIndex] + ", "
                        + secondIndex + "=" + visibilityByIndex[secondIndex]
            )
        }

        val turnedCards = TurnedCards(cards[firstIndex], cards[secondIndex])

        if (!turnedCards.isPair) {
            currentPlayer = currentPlayer.next()
            return turnedCards
        }

        val now = scores.getOrElse(currentPlayer, { 0 })
        scores[currentPlayer] = now + 1

        visibilityByIndex[firstIndex] = true
        visibilityByIndex[secondIndex] = true

        return turnedCards
    }


    fun getCurrentPlayer(): Player = currentPlayer


    fun getVisibilityByIndex(): Map<Int, Boolean> = visibilityByIndex


    fun isGameOver(): Boolean = !visibilityByIndex.values.contains(false)


    private fun initScores() = mapOf(Player.ONE to 0, Player.TWO to 0).toMutableMap()


    private fun shuffleCards(deck: Deck, motiveCount: Int, seed: Long): List<Motive> {
        if (motiveCount > deck.motiveCount()) {
            throw IllegalArgumentException(
                "Given deck has only "
                        + deck.motiveCount() + " motives, but "
                        + motiveCount + " given to use as parameter."
            )
        }

        var cards = deck.motiveSet().toMutableList()

        val random = Random(seed)

        cards.shuffle(random)

        val shuffledAndReducedCards = cards.subList(0, motiveCount)

        val doubledCards = ArrayList(shuffledAndReducedCards)
        doubledCards.addAll(shuffledAndReducedCards)

        doubledCards.shuffle(random)

        return doubledCards
    }


    private fun createVisibilityMap(cards: List<Motive>): MutableMap<Int, Boolean> {
        val indices: MutableMap<Int, Boolean> = mutableMapOf()

        for (i in cards.indices) {
            indices[i] = false
        }

        return indices
    }
}