package de.pixelgerecht.riekespaarefinden.memory

data class TurnedCards(val card1: Motive, val card2: Motive) {
    val isPair: Boolean = card1 == card2
}