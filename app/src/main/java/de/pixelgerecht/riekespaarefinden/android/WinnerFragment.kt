package de.pixelgerecht.riekespaarefinden.android

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.winner_fragment.*

class WinnerFragment : Fragment() {

    var winner: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.winner_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        draw_image_1.setImageDrawable(winner)

        startAnimation()
    }

    private fun startAnimation() {
        val animatorX = ObjectAnimator.ofFloat(draw_image_1, "translationY", -50f, 50f)
        animatorX.repeatMode = ValueAnimator.REVERSE
        animatorX.repeatCount = ValueAnimator.INFINITE

        val animatorScaleX = ObjectAnimator.ofFloat(draw_image_1, "scaleX", 0.8f, 1.0f)
        animatorScaleX.repeatMode = ValueAnimator.REVERSE
        animatorScaleX.repeatCount = ValueAnimator.INFINITE

        val animatorScaleY = ObjectAnimator.ofFloat(draw_image_1, "scaleY", 0.8f, 1.0f)
        animatorScaleY.repeatMode = ValueAnimator.REVERSE
        animatorScaleY.repeatCount = ValueAnimator.INFINITE

        AnimatorSet().apply {
            duration = resources.getInteger(R.integer.avatarAnimationDuration).toLong()
            play(animatorX).with(animatorScaleX).with(animatorScaleY)
            start()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param winner Parameter 1.
         * @return A new instance of fragment PlayerOneWinsFragment.
         */
        @JvmStatic
        fun newInstance(winner: Drawable) =
            WinnerFragment().apply {
                this.winner = winner
            }
    }
}
