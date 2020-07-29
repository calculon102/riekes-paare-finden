package de.pixelgerecht.riekespaarefinden.android

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.animation.doOnCancel
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.pixelgerecht.riekespaarefinden.memory.Player
import kotlinx.android.synthetic.main.scores_fragment.*

const val STATE_ANIMATION_PLAYER_ONE = "STATE_ANIMATION_PLAYER_ONE"
const val STATE_ANIMATION_PLAYER_TWO = "STATE_ANIMATION_PLAYER_TWO"

class ScoresFragment: Fragment() {
    private val animators: MutableMap<Player, Animator> = mutableMapOf()
    private val animationStates: MutableMap<Player, Boolean> = mutableMapOf(Pair(Player.ONE, false), Pair(Player.TWO, false))

    private val onAvatarChange : SharedPreferences.OnSharedPreferenceChangeListener = createOnAvatarChangeListener()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.scores_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initAvatars()
        initAnimations(savedInstanceState)
    }

    private fun initAvatars() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        prefs.registerOnSharedPreferenceChangeListener(onAvatarChange)

        onAvatarChange.onSharedPreferenceChanged(
            prefs,
            resources.getString(R.string.prefs_style_avatar_player_one)
        )
        onAvatarChange.onSharedPreferenceChanged(
            prefs,
            resources.getString(R.string.prefs_style_avatar_player_two)
        )
    }

    private fun initAnimations(savedInstanceState: Bundle?) {
        animators[Player.ONE] = createPlayerAnimator(imagePlayer1)
        animators[Player.TWO] = createPlayerAnimator(imagePlayer2)

        animationStates[Player.ONE] = savedInstanceState?.getBoolean(STATE_ANIMATION_PLAYER_ONE)
            ?: false
        animationStates[Player.TWO] = savedInstanceState?.getBoolean(STATE_ANIMATION_PLAYER_TWO)
            ?: false

        if (animationStates[Player.ONE] == true)
            startAnimation(Player.ONE)
        else if (animationStates[Player.TWO] == true)
            startAnimation(Player.TWO)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(STATE_ANIMATION_PLAYER_ONE, animationStates[Player.ONE] ?: false)
        outState.putBoolean(STATE_ANIMATION_PLAYER_TWO, animationStates[Player.TWO] ?: false)
    }

    private fun createPlayerAnimator(playerImage: ImageView): Animator {
        val animator = ObjectAnimator.ofFloat(playerImage, "rotation", -20f, 40f)
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.duration = resources.getInteger(R.integer.avatarAnimationDuration).toLong()
        animator.doOnCancel { playerImage.rotation = 0f }

        return animator
    }

    fun animateCurrentPlayer(currentPlayer: Player) {
        stopAnimations()
        startAnimation(currentPlayer)
    }

    fun stopAnimations() {
        stopAnimation(Player.ONE)
        stopAnimation(Player.TWO)
    }

    private fun startAnimation(ofPlayer: Player) {
        animators[ofPlayer]?.start()
        animationStates[ofPlayer] = true
    }

    private fun stopAnimation(ofPlayer: Player) {
        animators[ofPlayer]?.cancel()
        animationStates[ofPlayer] = false
    }


    private fun createOnAvatarChangeListener(): SharedPreferences.OnSharedPreferenceChangeListener {
        return SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            val res = context?.resources

            val avatarView: ImageView? = when (key) {
                res?.getString(R.string.prefs_style_avatar_player_one) -> {
                    imagePlayer1
                }
                res?.getString(R.string.prefs_style_avatar_player_two) -> {
                    imagePlayer2
                }
                else -> {
                    null
                }
            }

            val avatarDefault = when (key) {
                res?.getString(R.string.prefs_style_avatar_player_one) -> {
                    resources.getString(R.string.prefs_style_avatar_player_one_default)
                }
                res?.getString(R.string.prefs_style_avatar_player_two) -> {
                    resources.getString(R.string.prefs_style_avatar_player_two_default)
                }
                else -> {
                    null
                }
            }

            avatarView?.apply {
                val newValue = prefs.getString(key, avatarDefault)
                val id = R.drawable::class.java.getField("ic_$newValue").getInt(null)
                setImageDrawable(resources.getDrawable(id, null))
            }
        }
    }
}