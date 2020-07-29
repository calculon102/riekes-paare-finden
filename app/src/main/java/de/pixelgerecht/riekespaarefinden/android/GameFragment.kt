package de.pixelgerecht.riekespaarefinden.android

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.pixelgerecht.riekespaarefinden.memory.Deck
import de.pixelgerecht.riekespaarefinden.memory.Game
import de.pixelgerecht.riekespaarefinden.memory.Motive
import de.pixelgerecht.riekespaarefinden.memory.Player

const val STATE_GAME = "game"
const val STATE_SELECTION = "selection"
const val STATE_CARD_ARRANGEMENT = "cardArrangement"

class GameFragment : Fragment() {
    var gameListener: GameListener? = null

    private var currentGame: Game? = null
    private var selectionState = SelectionState()

    private var cardBack: Drawable = ColorDrawable(Color.WHITE)
    private var emptyCard: Drawable = ColorDrawable(Color.WHITE)

    private val drawableMotives: MutableMap<Motive, Drawable> = mutableMapOf()
    private val viewsByCardIndex: MutableMap<Int, ImageView> = mutableMapOf()

    private var cardLayout: TableLayout? = null

    private var cardArrangement: RectCardArrangement =
        RectCardArrangement(3, 2, RectCardArrangement.Orientation.PORTRAIT)

    private val onPrefLayoutChange : SharedPreferences.OnSharedPreferenceChangeListener = createPrefLayoutChangeListener()

    private var onPrefCardBackChange : SharedPreferences.OnSharedPreferenceChangeListener? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        cardLayout = inflater.inflate(R.layout.game_fragment, container, false) as TableLayout
        return cardLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = savedInstanceState ?: arguments

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        prefs.registerOnSharedPreferenceChangeListener(onPrefLayoutChange)

        if (bundle != null) {
            initCardsFromPrefs(prefs)
            reloadGame(bundle)
        } else {
            restartGame()
        }
    }

    private fun initCardsFromPrefs(prefs: SharedPreferences) {
        val defaultCardBack = resources.getString(R.string.prefs_style_card_default_value)
        val cardBackKey = resources.getString(R.string.prefs_style_card_back_key)
        initCardBack(prefs, cardBackKey, defaultCardBack)

        // Enable on-the-fly change
        onPrefCardBackChange = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == cardBackKey) {
                initCardBack(prefs, cardBackKey, defaultCardBack)
                fillLayout(cardArrangement.width(), cardLayout)
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(onPrefCardBackChange)

        emptyCard = Drawable.createFromStream(activity?.assets?.open("empty-card.png"), null)
    }

    private fun initCardBack(
        prefs: SharedPreferences,
        cardBackKey: String,
        defaultCardBack: String
    ) {
        val cardBackPref = prefs.getString(cardBackKey, defaultCardBack) ?: defaultCardBack

        cardBack =
            Drawable.createFromStream(activity?.assets?.open("card-back-$cardBackPref.png"), null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(STATE_GAME, currentGame())
        outState.putSerializable(STATE_SELECTION, selectionState)
        outState.putSerializable(STATE_CARD_ARRANGEMENT, cardArrangement)

        super.onSaveInstanceState(outState)
    }

    private fun reloadGame(savedInstanceState: Bundle) {
        cardArrangement =
            savedInstanceState.getSerializable(STATE_CARD_ARRANGEMENT) as RectCardArrangement
        selectionState = savedInstanceState.getSerializable(STATE_SELECTION) as SelectionState
        currentGame = savedInstanceState.getSerializable(STATE_GAME) as Game

        cardArrangement = cardArrangement.alignToConfiguration(resources.configuration.orientation)

        fillLayout(cardArrangement.width(), cardLayout)
        updateScores(currentGame())
        gameListener?.onPlayerChange(currentGame().getCurrentPlayer())

        gameListener?.onResumedGame(cardArrangement)

        currentGame?.apply {
            checkGameOver(this)
        }
    }

    internal fun restartGame() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        initCardsFromPrefs(prefs)

        val newCardArrangement = RectCardArrangement.fromPrefs(prefs, resources)

        cardArrangement = newCardArrangement
        selectionState.reset()
        currentGame = createNewGame()

        fillLayout(cardArrangement.width(), cardLayout)
        updateScores(currentGame())
        gameListener?.onPlayerChange(currentGame().getCurrentPlayer())

        gameListener?.onNewGame()
    }

    private fun createNewGame(): Game =
        Game(createDeck(), (cardArrangement.lengthA * cardArrangement.lengthB) / 2)

    private fun fillLayout(cols: Int, layout: TableLayout?) {
        layout?.removeAllViews()

        viewsByCardIndex.clear()

        var col = 0
        var row = createCardRow(layout)

        val currentGame = currentGame()

        for (i: Int in currentGame.cards.indices) {
            val cardView = createCardView()
            cardView.setTag(R.integer.TAG_CARD_INDEX, i)

            if (currentGame.getVisibilityByIndex()[i] == true) {
                applyEmptyCard(cardView)
            } else if (selectionState.index1 == i || selectionState.index2 == i) {
                applyCardMotive(cardView, getDrawableMotive(currentGame, i), currentGame.cards[i].pathToImage)
                cardView.setOnClickListener { turnCard(i) }
            } else {
                applyCardBack(cardView)
                cardView.setOnClickListener { turnCard(i) }
            }

            cardView.setPadding(resources.getDimensionPixelSize(R.dimen.card_padding))

            row.addView(cardView)

            viewsByCardIndex[i] = cardView

            col += 1

            if (col >= cols) {
                col = 0
                row = createCardRow(layout)
            }
        }
    }

    private fun turnCard(i: Int) {
        // Sanity checks
        if (selectionState.index1 >= 0 && selectionState.index2 >= 0) {
            return // Game is done or pausing for players to view latest cards
        }

        val currentGame = currentGame()

        if (currentGame.getVisibilityByIndex()[i] == true) {
            return
        }

        // First card selected
        if (selectionState.index1 < 0) {
            selectionState.index1 = i
            viewsByCardIndex[selectionState.index1]?.apply {
                setImageDrawable(
                    getDrawableMotive(
                        currentGame,
                        i
                    )
                )
            }
            return
        }

        if (i == selectionState.index1) {
            return
        }

        // Second card selected
        selectionState.index2 = i
        viewsByCardIndex[selectionState.index2]?.apply {
            setImageDrawable(
                getDrawableMotive(
                    currentGame,
                    i
                )
            )
        }

        val result = currentGame.turnCards(selectionState.index1, selectionState.index2)

        val wait = if (result.isPair)
            resources.getInteger(R.integer.waitAfterPair).toLong() else
            resources.getInteger(R.integer.waitAfterTurn).toLong()

        Handler().postDelayed({ activity?.runOnUiThread { onTwoCardsTurned(result.isPair) } }, wait)
    }

    private fun onTwoCardsTurned(isPair: Boolean) {
        // Reset temporary game state
        if (isPair) {
            viewsByCardIndex[selectionState.index1]?.apply { applyEmptyCard(this) }
            viewsByCardIndex[selectionState.index2]?.apply { applyEmptyCard(this) }
        } else {
            viewsByCardIndex[selectionState.index1]?.apply { applyCardBack(this) }
            viewsByCardIndex[selectionState.index2]?.apply { applyCardBack(this) }
        }

        selectionState.reset()

        // Update game state and check for game over
        val currentGame = currentGame()

        if (isPair) {
            updateScores(currentGame)
            checkGameOver(currentGame)
        } else {
            gameListener?.onPlayerChange(currentGame.getCurrentPlayer())
        }
    }

    private fun updateScores(currentGame: Game) {
        gameListener?.onScoreUpdate(
            currentGame.getScoreOf(Player.ONE),
            currentGame.getScoreOf(Player.TWO)
        )
    }

    private fun checkGameOver(game: Game) {
        if (game.isGameOver()) {
            when {
                game.getScoreOf(Player.ONE) > game.getScoreOf(Player.TWO) -> gameListener?.onWin(
                    Player.ONE
                )
                game.getScoreOf(Player.ONE) < game.getScoreOf(Player.TWO) -> gameListener?.onWin(
                    Player.TWO
                )
                else -> gameListener?.onDraw()
            }
        }
    }

    private fun createDeck(): Deck {
        val dir = "deck-animals-mixed" // TODO Create preference-option for different decks

        val motives = activity?.assets?.list(dir)

        val motiveSet = motives?.map { Motive("$dir/$it") }?.toSet()
            ?: throw IllegalArgumentException("No assets found in $dir!")

        return Deck(motiveSet)
    }

    private fun createCardRow(table: TableLayout?): TableRow {
        val row = TableRow(context)

        row.gravity = Gravity.CENTER
        row.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )

        table?.addView(row)

        return row
    }

    private fun createCardView(): ImageView {
        val cardView = ImageView(context)

        cardView.adjustViewBounds = true
        cardView.scaleType = ImageView.ScaleType.FIT_CENTER

        val layout = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1.0f
        )

        cardView.layoutParams = layout

        cardView.elevation = 1.0f
        cardView.outlineProvider = ViewOutlineProvider.BOUNDS

        return cardView
    }

    private fun currentGame() = currentGame
        ?: throw IllegalStateException("No current game initialized!")


    private fun getDrawableMotive(currentGame: Game, index: Int): Drawable {
        return drawableMotives.computeIfAbsent(currentGame.cards[index]) {
            Drawable.createFromStream(activity?.assets?.open(it.pathToImage), null)
        }
    }

    private fun applyEmptyCard(card: ImageView) {
        applyCardMotive(card, emptyCard, resources.getString(R.string.TAG_CARD_STATE_EMPTY))
        card.isClickable = false
    }

    private fun applyCardBack(card: ImageView) {
        applyCardMotive(card, cardBack, resources.getString(R.string.TAG_CARD_STATE_BACK))
    }

    private fun applyCardMotive(card: ImageView, newDrawable: Drawable, motiveTag: String) {
        card.setImageDrawable(newDrawable)
        card.setTag(R.integer.TAG_CARD_STATE, motiveTag)
    }


    private fun createPrefLayoutChangeListener(): SharedPreferences.OnSharedPreferenceChangeListener {
        return SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            val res = context?.resources

            if (key == res?.getString(R.string.prefs_game_card_count_key)) {
                restartGame()
            }
        }
    }
}
