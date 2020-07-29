package de.pixelgerecht.riekespaarefinden.android

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.pixelgerecht.riekespaarefinden.memory.Game

class GameModel : ViewModel() {
    val gameLiveData = MutableLiveData<Game>()
    val selectionLiveData = MutableLiveData<SelectionState>()
}