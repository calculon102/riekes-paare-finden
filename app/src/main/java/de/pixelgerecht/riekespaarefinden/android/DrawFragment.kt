package de.pixelgerecht.riekespaarefinden.android

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.draw_fragment.*


class DrawFragment : Fragment() {

    var avatar1: Drawable? = null
    var avatar2: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.draw_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        setImageByArgument()

        startAnimation(draw_image_1)
        startAnimation(draw_image_2)
    }

    private fun setImageByArgument() {
        draw_image_1.setImageDrawable(avatar1)
        draw_image_2.setImageDrawable(avatar2)
    }

    private fun startAnimation(imageView: ImageView) {
        val animatorX = ObjectAnimator.ofFloat(imageView, "translationX", -30f, 30f)
        animatorX.repeatMode = ValueAnimator.REVERSE
        animatorX.repeatCount = ValueAnimator.INFINITE
        animatorX.start()
    }

    companion object {
        /**
         * @param avatar1
         * @param avatar2
         * @return A new instance of fragment DrawFragment.
         */
        @JvmStatic
        fun newInstance(avatar1: Drawable, avatar2: Drawable) =
            DrawFragment().apply {
                this.avatar1 = avatar1
                this.avatar2 = avatar2
            }
    }
}
