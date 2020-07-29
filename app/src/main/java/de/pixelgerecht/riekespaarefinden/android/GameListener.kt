package de.pixelgerecht.riekespaarefinden.android

import de.pixelgerecht.riekespaarefinden.memory.Player

interface GameListener {
    fun onNewGame()

    fun onResumedGame(cardArrangement: RectCardArrangement)

    fun onScoreUpdate(playerOne: Int, playerTwo: Int)

    fun onWin(winner: Player)

    fun onDraw()

    fun onPlayerChange(newCurrentPlayer: Player)
}