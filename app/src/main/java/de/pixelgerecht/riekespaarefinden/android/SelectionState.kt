package de.pixelgerecht.riekespaarefinden.android

import java.io.Serializable

const val UNSELECTED = -1

data class SelectionState(
    var index1: Int = UNSELECTED,
    var index2: Int = UNSELECTED
) : Serializable {

    fun reset() = setBoth(UNSELECTED, UNSELECTED)

    private fun setBoth(newIndex1: Int, newIndex2: Int) {
        index1 = newIndex1
        index2 = newIndex2
    }
}