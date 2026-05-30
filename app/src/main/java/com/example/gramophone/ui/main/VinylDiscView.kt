package com.example.gramophone.ui.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class VinylDiscView(context: Context, attrs: AttributeSet?) : androidx.constraintlayout.widget.ConstraintLayout(context, attrs) {
    var onSeekListener: ((Boolean) -> Unit)? = null
    private var rotationAngle = 0f
    private var isRotating = false

    private lateinit var discImage: ImageView
    private lateinit var centerText: TextView

    private var initialTouchAngle = 0f
    private var currentTouchAngle = 0f

    private val rotationRunnable = object : Runnable {
        override fun run() {
            if (isRotating) {
                rotationAngle = (rotationAngle + 2f) % 360f
                rotateDisc()
                handler.postDelayed(this, 16)
            }
        }
    }
    private val handler = Handler(Looper.getMainLooper())

    init {
        setupViews()
    }

    private fun setupViews() {
        discImage = ImageView(context).apply {
            id = View.generateViewId()
            setImageResource(android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTERCROP
        }

        centerText = TextView(context).apply {
            id = View.generateViewId()
            text = "Artist - Song"
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            textSize = 12f
        }

        addView(discImage)
        addView(centerText)

        // Center the views
        discImage.layoutParams = LayoutParams(300, 300) // Simplified
        centerText.layoutParams = LayoutParams(wrapContent, wrapContent)
    }

    fun setRotation(rotating: Boolean) {
        isRotating = rotating
        if (rotating) {
            handler.post(rotationRunnable)
        } else {
            handler.removeCallbacks(rotationRunnable)
        }
    }

    fun setSongInfo(title: String, artist: String, artUri: Uri?) {
        centerText.text = "$artist - $title"
        artUri?.let {
            discImage.setImageURI(it)
        } ?: run {
            discImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun rotateDisc() {
        rotation = rotationAngle
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - width / 2f
        val y = event.y - height / 2f
        val angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchAngle = angle
            }
            MotionEvent.ACTION_MOVE -> {
                currentTouchAngle = angle
                val delta = currentTouchAngle - initialTouchAngle
                if (delta > 30f) {
                    onSeekListener?.invoke(true) // Clockwise
                    initialTouchAngle = currentTouchAngle
                } else if (delta < -30f) {
                    onSeekListener?.invoke(false) // Counter-clockwise
                    initialTouchAngle = currentTouchAngle
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private val wrapContent = -2
}
