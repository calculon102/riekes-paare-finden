package de.pixelgerecht.riekespaarefinden.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceManager
import de.pixelgerecht.riekespaarefinden.memory.Player
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.scores_fragment.*

// TODO Change first player after game
// TODO Offer Alternative deck
// TODO Add border to card-backs
// TODO Implement computer-partner
// TODO Fade turned cards away
// TODO App-Logo
// TODO Sound-effects
// TODO Rework game-over animations
class MainActivity : AppCompatActivity(), GameListener {

    private var gameFragment: GameFragment? = null
    private var gameoverFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        gameFragment = supportFragmentManager.findFragmentById(R.id.gameFragment) as GameFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if (fragment is GameFragment)
        {
            fragment.gameListener = this
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_newgame -> {
                gameFragment?.restartGame()
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onWin(winner: Player) {
        val avatar = if (winner == Player.ONE) imagePlayer1.drawable else imagePlayer2.drawable

        val fragment = WinnerFragment.newInstance(avatar)

        onGameOver(fragment)
    }

    override fun onDraw() {
        val fragment = DrawFragment.newInstance(imagePlayer1.drawable, imagePlayer2.drawable)

        onGameOver(fragment)
    }

    private fun onGameOver(gameoverFragment: Fragment) {
        gameoverFragment.apply {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.MainContainer, this)
            transaction.commit()
        }

        val scoresFragment = supportFragmentManager
            .findFragmentById(R.id.scoresFragment) as ScoresFragment

        scoresFragment.stopAnimations()

        this.gameoverFragment = gameoverFragment
    }

    override fun onNewGame() {
        // Clear winner-screen and correct aspect ratio of layout according to card-count in
        // preferences.
        gameoverFragment?.apply {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.remove(this)
            transaction.commit()
        }
        gameoverFragment = null

        val cardArrangement = RectCardArrangement.fromPrefs(
            PreferenceManager.getDefaultSharedPreferences(this),
            resources
        )

        updateCardLayoutDimensionRatio(cardArrangement)
    }

    override fun onResumedGame(cardArrangement: RectCardArrangement) {
        updateCardLayoutDimensionRatio(cardArrangement)
    }

    private fun updateCardLayoutDimensionRatio(cardArrangement: RectCardArrangement) {
        val gameFragmentView = findViewById<FragmentContainerView>(R.id.gameFragment)
        val gameFragmentLayout = gameFragmentView.layoutParams as ConstraintLayout.LayoutParams

        gameFragment?.apply {
            gameFragmentLayout.dimensionRatio = cardArrangement.asRatio()
            gameFragmentView.layoutParams = gameFragmentLayout
        }
    }

    override fun onScoreUpdate(playerOne: Int, playerTwo: Int) {
        val scorePlayer1 = scoresFragment.findViewById(R.id.scorePlayer1) as TextView
        val scorePlayer2 = scoresFragment.findViewById(R.id.scorePlayer2) as TextView

        scorePlayer1.text = playerOne.toString()
        scorePlayer2.text = playerTwo.toString()
    }

    override fun onPlayerChange(newCurrentPlayer: Player) {
        val scoresFragment = supportFragmentManager
            .findFragmentById(R.id.scoresFragment) as ScoresFragment

        scoresFragment.animateCurrentPlayer(newCurrentPlayer)
    }
}