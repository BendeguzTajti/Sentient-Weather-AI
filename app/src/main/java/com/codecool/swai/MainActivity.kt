package com.codecool.swai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheet: BottomSheetBehavior<NestedScrollView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        bottomSheet = BottomSheetBehavior.from(detailsPage)
        addBottomSheetListener()
    }

    private fun addBottomSheetListener() {
        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (mainPage.isAnimating && slideOffset < 1.0f && slideOffset > 0.0f) {
                    mainPage.pauseAnimation()
                }
                if (slideOffset < 1.0f && slideOffset > 0.0f) {
                    swipeIndicator.visibility = View.INVISIBLE
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (mainPage.progress > 0.0f) {
                    mainPage.resumeAnimation()
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    swipeIndicator.setAnimation(R.raw.swipe_down)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    swipeIndicator.setAnimation(R.raw.swipe_up)
                }
                swipeIndicator.playAnimation()
                swipeIndicator.visibility = View.VISIBLE
            }

        })
    }
}