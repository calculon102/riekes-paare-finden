package de.pixelgerecht.riekespaarefinden.memory

enum class Player {
    ONE, TWO;

    fun next(): Player = if (this == ONE) TWO else ONE
}