package de.pixelgerecht.riekespaarefinden.memory

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import java.lang.IllegalArgumentException

class GameTest
{
    @Test
    fun currentPlayerScoresWhenPairIsFound() {
        // Prepare
        val game = createGameWithFourMotives()
        val prevPlayer = game.getCurrentPlayer()
        val prevScore = game.getScoreOf(prevPlayer)

        // Act
        val cards = game.turnCards(0, 5)

        // Check
        assertThat(cards.isPair).isEqualTo(true)
        assertThat(game.getScoreOf(prevPlayer)).isEqualTo(prevScore + 1)
    }

    @Test
    fun currentPlayerKeepsPlayingWhenPairIsFound() {
        // Prepare
        val game = createGameWithFourMotives()
        val prevPlayer = game.getCurrentPlayer()

        // Act
        val cards = game.turnCards(0, 5)

        // Check
        assertThat(cards.isPair).isEqualTo(true)
        assertThat(game.getCurrentPlayer()).isEqualTo(prevPlayer)
    }

    @Test
    fun currentPlayerScoresNotWhenPairIsNotFound() {
        // Prepare
        val game = createGameWithFourMotives()
        val prevPlayer = game.getCurrentPlayer()
        val prevScore = game.getScoreOf(prevPlayer)

        // Act
        val cards = game.turnCards(1, 2)

        // Check
        assertThat(cards.isPair).isEqualTo(false)
        assertThat(game.getScoreOf(prevPlayer)).isEqualTo(prevScore)
    }

    @Test
    fun nextPlayerScoresWhenPairIsFound() {
        // Prepare
        val game = createGameWithFourMotives()
        val prevPlayer = game.getCurrentPlayer()
        val prevScoreOfPrevPlayer = game.getScoreOf(prevPlayer)
        val prevScoreOfNextPlayer = game.getScoreOf(prevPlayer.next())

        // Act
        val firstCards = game.turnCards(1, 2)
        val secondCards = game.turnCards(0, 5)

        // Check
        assertThat(firstCards.isPair).isEqualTo(false)
        assertThat(secondCards.isPair).isEqualTo(true)

        assertThat(game.getCurrentPlayer()).isEqualTo(prevPlayer.next())

        assertThat(game.getScoreOf(prevPlayer)).isEqualTo(prevScoreOfPrevPlayer)
        assertThat(game.getScoreOf(game.getCurrentPlayer())).isEqualTo(prevScoreOfNextPlayer + 1)
    }

    @Test
    fun currentPlayerChangesWhenPairIsNotFound() {
        // Prepare
        val game = createGameWithFourMotives()
        val prevPlayer = game.getCurrentPlayer()

        // Act
        val cards = game.turnCards(1, 2)

        // Check
        assertThat(cards.isPair).isEqualTo(false)
        assertThat(game.getCurrentPlayer()).isEqualTo(prevPlayer.next())
    }

    @Test
    fun gameBeginsWithAllCardsHidden() {
        // Prepare
        val motivesToUse = 4
        val expectedCards = motivesToUse * 2
        val game = createGameWithFourMotives(motivesToUse)

        // Check
        assertThat(game.getVisibilityByIndex().size).isEqualTo(expectedCards)

        for (i in 0 until expectedCards)
        {
            assertThat(game.getVisibilityByIndex()[i]).isEqualTo(false)
        }
    }

    @Test
    fun cannotTurnVisibleCards() {
        // Prepare
        val game = createGameWithFourMotives()

        val cards = game.turnCards(0, 5)

        assertThat(cards.isPair).isEqualTo(true)

        // Act / Check
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(0, 5) }
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(0, 2) }
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(5, 2) }
    }

    @Test
    fun cannotUseMoreMotivesThanDeckOffers() {
        val deck = Deck(setOf(Motive("1")))

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { Game(deck, 2, 1) }
    }

    @Test
    fun usedMotivesAppearAsPair() {
        // Prepare / Act
        val motivesToUse = 3
        val expectedCards = motivesToUse * 2
        val game = createGameWithFourMotives(3)

        // Check
        assertThat(game.getVisibilityByIndex().size).isEqualTo(expectedCards)

        // These indices are guaranteed to be pairs, with used seed and deck
        val firstCards = game.turnCards(0, 2)
        val secondCards = game.turnCards(1, 4)
        val thirdCards = game.turnCards(3, 5)

        assertThat(firstCards.isPair).isEqualTo(true)
        assertThat(secondCards.isPair).isEqualTo(true)
        assertThat(thirdCards.isPair).isEqualTo(true)
    }

    @Test
    fun cannotTurnCardsOutOfIndexRange() {
        // Prepare
        val game = createGameWithFourMotives()

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(-1, 0) }
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(0, -1) }
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(7, 8) }
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { game.turnCards(8, 7) }
    }

    @Test
    fun gameIsOverWhenAllPairsFound() {
        // Prepare
        val game = createGameWithFourMotives()

        // Act / Check
        val firstCards = game.turnCards(0, 5)
        assertThat(game.isGameOver()).isEqualTo(false)

        val secondCards = game.turnCards(1, 7)
        assertThat(game.isGameOver()).isEqualTo(false)

        val thirdCards = game.turnCards(2, 4)
        assertThat(game.isGameOver()).isEqualTo(false)

        val fourthCards = game.turnCards(3, 6)
        assertThat(game.isGameOver()).isEqualTo(true)

        // Check
        assertThat(firstCards.isPair).isEqualTo(true)
        assertThat(secondCards.isPair).isEqualTo(true)
        assertThat(thirdCards.isPair).isEqualTo(true)
        assertThat(fourthCards.isPair).isEqualTo(true)
    }

    @Test
    fun motivesAccessibleWhenCardsTurned() {
        val game = createGameWithFourMotives()

        val turnedCards1 = game.turnCards(0, 1)

        assertThat(turnedCards1.isPair).isEqualTo(false)
        assertThat(turnedCards1.card1).isEqualTo(Motive("4"))
        assertThat(turnedCards1.card2).isEqualTo(Motive("3"))


        val turnedCards2 = game.turnCards(2, 4)

        assertThat(turnedCards2.isPair).isEqualTo(true)
        assertThat(turnedCards2.card1).isEqualTo(Motive("1"))
        assertThat(turnedCards2.card2).isEqualTo(Motive("1"))
    }

    @Test
    fun accessMotivesByIndex() {
        val game = createGameWithFourMotives()

        assertThat(game.cards[0]).isEqualTo(Motive("4"))
        assertThat(game.cards[1]).isEqualTo(Motive("3"))
        assertThat(game.cards[2]).isEqualTo(Motive("1"))
        assertThat(game.cards[3]).isEqualTo(Motive("2"))
        assertThat(game.cards[4]).isEqualTo(Motive("1"))
        assertThat(game.cards[5]).isEqualTo(Motive("4"))
        assertThat(game.cards[6]).isEqualTo(Motive("2"))
        assertThat(game.cards[7]).isEqualTo(Motive("3"))
    }

    /**
     * With given seed, the pairs are
     * 0 = {Motive@864} "Motive(pathToImage=4)"
     * 1 = {Motive@865} "Motive(pathToImage=3)"
     * 2 = {Motive@866} "Motive(pathToImage=1)"
     * 3 = {Motive@867} "Motive(pathToImage=2)"
     * 4 = {Motive@866} "Motive(pathToImage=1)"
     * 5 = {Motive@864} "Motive(pathToImage=4)"
     * 6 = {Motive@867} "Motive(pathToImage=2)"
     * 7 = {Motive@865} "Motive(pathToImage=3)"
     */
    private fun createGameWithFourMotives(motivesToUse: Int = 4): Game {
        val deck = Deck(setOf(Motive("1"), Motive("2"), Motive("3"), Motive("4")))
        return Game(deck, motivesToUse, 1)
    }
}