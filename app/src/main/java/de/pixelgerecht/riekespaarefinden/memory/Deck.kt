package de.pixelgerecht.riekespaarefinden.memory

import java.io.Serializable

class Deck(private val motives: Set<Motive>) : Serializable {

    private val unmodifiableMotives : Set<Motive>

    init {
        if (motives.isEmpty())
        {
            throw IllegalArgumentException("Given set of motives must not be empty!")
        }

        unmodifiableMotives = motives.toMutableSet()
    }

    /**
     * @return Count of the motives of this deck.
     */
    fun motiveCount() = motives.size

    /**
     * @return Motives of this deck as an unmodifiable Set.
     */
    fun motiveSet() = unmodifiableMotives
}
