package de.pixelgerecht.riekespaarefinden.android

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.pixelgerecht.riekespaarefinden.memory.Deck
import de.pixelgerecht.riekespaarefinden.memory.Game
import de.pixelgerecht.riekespaarefinden.memory.Motive
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameFragmentTest {

    @Test fun clickOnHiddenCardShouldTurnItsMotive() {
        val fragmentArgs = Bundle().apply {
            putSerializable(STATE_CARD_ARRANGEMENT, RectCardArrangement(3, 2, RectCardArrangement.Orientation.PORTRAIT))
            putSerializable(STATE_GAME, createGameWithFourMotives())
            putSerializable(STATE_SELECTION, SelectionState())
        }

        launchFragmentInContainer<GameFragment>(fragmentArgs)

        val card0 = withTagKey(R.integer.TAG_CARD_INDEX, Matchers.equalTo(0))
        onView(card0)
            .check(matches(withDrawable("card-back-purple.png")))

        onView(card0).perform(click())
        onView(card0)
            .check(matches(withDrawable("deck-animals-mixed/chameleon.jpg")))

        onView(card0).perform(click())
        onView(card0).perform(waitFor(501))
        onView(card0)
            .check(matches(withDrawable("deck-animals-mixed/chameleon.jpg")))
    }

    private fun createGameWithFourMotives(motivesToUse: Int = 3): Game {
        val deck = Deck(setOf(
            Motive("deck-animals-mixed/butterfly.jpg"),
            Motive("deck-animals-mixed/chameleon.jpg"),
            Motive("deck-animals-mixed/chimpanzee.jpg"),
            Motive("deck-animals-mixed/cow.jpg")
        ))
        return Game(deck, motivesToUse, 1)
    }

    private fun waitFor(delay: Long) = object : ViewAction {
        override fun getConstraints(): org.hamcrest.Matcher<View> {
            return isDisplayed();
        }

        override fun getDescription(): String {
            return "wait for " + delay + "milliseconds";
        }

        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadForAtLeast(delay);
        }
    }

    private fun withDrawable(assetPath: String) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with drawable same as drawable asset $assetPath")
        }

        override fun matchesSafely(item: View): Boolean {
            val expected = Drawable.createFromStream(item.context.assets.open(assetPath), null).toBitmap()

            return item is ImageView && item.drawable.toBitmap().sameAs(expected)
        }
    }
}