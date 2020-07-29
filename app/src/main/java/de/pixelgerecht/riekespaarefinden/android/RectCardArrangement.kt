package de.pixelgerecht.riekespaarefinden.android

import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

data class RectCardArrangement(
    val lengthA: Int,
    val lengthB: Int,
    val orientation: Orientation
) : Serializable {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    fun width() =
        if (orientation == Orientation.PORTRAIT) smallerSide() else longerSide()

    fun height() =
        if (orientation == Orientation.LANDSCAPE) smallerSide() else longerSide()

    fun smallerSide() = min(lengthA, lengthB)

    fun longerSide() = max(lengthA, lengthB)

    fun asRatio() = width().toString() + ":" + height().toString()

    fun alignToConfiguration(configValue: Int): RectCardArrangement {
        if (configValue == Configuration.ORIENTATION_LANDSCAPE) {
            return if (orientation == Orientation.LANDSCAPE)
                this
            else
                RectCardArrangement(lengthA, lengthB, Orientation.LANDSCAPE)
        }

        if (configValue == Configuration.ORIENTATION_PORTRAIT) {
            return if (orientation == Orientation.PORTRAIT)
                this
            else
                RectCardArrangement(lengthA, lengthB, Orientation.PORTRAIT)
        }

        throw IllegalArgumentException("Invalid configuration-value given. Expecting Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT")
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences, resources: Resources): RectCardArrangement {
            val defaultGameLayout =
                resources.getString(R.string.prefs_game_card_count_default_value)

            val gameLayout = prefs.getString(
                resources.getString(R.string.prefs_game_card_count_key),
                defaultGameLayout
            ) ?: defaultGameLayout

            val gameLayoutFragments = gameLayout.split('x')

            val orientation =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    Orientation.PORTRAIT
                else
                    Orientation.LANDSCAPE

            return RectCardArrangement(
                gameLayoutFragments[0].toInt(),
                gameLayoutFragments[1].toInt(),
                orientation
            )
        }
    }
}