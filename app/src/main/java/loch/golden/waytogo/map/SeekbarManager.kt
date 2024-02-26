package loch.golden.waytogo.map

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import loch.golden.waytogo.R


class SeekbarManager(
    context: Context,
    private val mapViewModel: MapViewModel,
    private val seekbar: SeekBar,
    private val customSeekbar: View,
    private val playPauseButtons: List<ImageButton>
) {
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val screenWidth by lazy {
        val a=context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density;
        Log.d("Display Metrics",a.toString())
        a
    }
    private val seekbarRunnable by lazy {
        object : Runnable {
            val percentList = mutableListOf<Float>() // this is for debugging
            override fun run() {
                try {
                    val currentPosition = mapViewModel.mp!!.currentPosition.toFloat()
                    val duration = mapViewModel.mp!!.duration.toFloat()

                    // Update marker details seekbar
                    seekbar.progress = currentPosition.toInt()

                    // Update bottom custom seekbar
                    val trackPercentage = if (duration != 0.0f) currentPosition / duration else 0.0f
                    val dp = (screenWidth * trackPercentage).toInt()
                    val pixels = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp.toFloat(),
                        context.resources.displayMetrics
                    ).toInt()
                    percentList.add(trackPercentage)
                    customSeekbar.layoutParams.width = pixels


                    //how often to refresh the seekbar
                    handler.postDelayed(this, 50)
                } catch (e: Exception) {
                    Log.d("Warmbier", e.toString())
                }
            }

        }
    }

    init {
        togglePlayPauseIcons()
        if (mapViewModel.mp!!.isPlaying) // maybe change to if null
            initSeekbar()

    }

    private fun initSeekbar() {
        seekbar.max = mapViewModel.mp!!.duration
        handler.postDelayed(seekbarRunnable, 0)

        seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mapViewModel.mp?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                // when start tracking
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                //when end tracking
            }
        })

        mapViewModel.mp!!.setOnCompletionListener {
            mapViewModel.mp?.seekTo(0)
            togglePlayPauseIcons()
        }
    }

    private fun togglePlayPauseIcons() {
        val playPauseDrawable = if (mapViewModel.mp!!.isPlaying) {
            R.drawable.ic_pause_24
        } else {
            R.drawable.ic_play_arrow_24
        }

        for (button in playPauseButtons) {
            button.setImageResource(playPauseDrawable)
        }
    }

    fun startTracking(mp: MediaPlayer) {

    }


    fun removeCallback() {
        handler.removeCallbacks(seekbarRunnable)
    }
}