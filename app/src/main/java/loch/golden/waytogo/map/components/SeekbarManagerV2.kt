package loch.golden.waytogo.map.components

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import loch.golden.waytogo.map.MapViewModel

class SeekbarManagerV2(
    private val mapViewModel: MapViewModel,
    private val seekbar: SeekBar,
    private val buttonList: List<Button>
) {
    private val handler = Handler(Looper.getMainLooper())
    private var seekbarRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                seekbar.progress = mapViewModel.mp!!.currentPosition
                handler.postDelayed(this, 100)
            } catch (e: Exception) {
                Log.d("Warmbier", e.toString())
            }
        }

    }


    init {
        for (button in buttonList) {
            button.setOnClickListener {
                try {
                    if (mapViewModel.mp!!.isPlaying)
                        pauseAudio()
                    else
                        resumeAudio()
                } catch (e: Exception) {
                    Log.d("Warmbier", "pause/play button: $e")
                }
            }
        }
    }

    fun prepareAudio(audioPath: String) {
        mapViewModel.mp = MediaPlayer()
        mapViewModel.mp!!.apply {
            setDataSource(audioPath)
            prepare()
            setOnPreparedListener {
                toggleButtons(false)
                initSeekbar() // Initialize SeekBar here
                seekbar.isEnabled = true
            }
            setOnCompletionListener {
                Log.d("AudioWarmbier", "stop/release playing")
                toggleButtons(false)

            }
        }
    }

    private fun initSeekbar() {
        seekbar.max = mapViewModel.mp!!.duration
        handler.postDelayed(seekbarRunnable, 0)
        seekbar.isEnabled = true
        seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) mapViewModel.mp?.seekTo(progress)
                }

                override fun onStartTrackingTouch(seekbar: SeekBar?) {
                    pauseAudio()
                }

                override fun onStopTrackingTouch(seekbar: SeekBar?) {
                    resumeAudio()
                }
            })
    }

    fun setCustomSeekbar(customSeekbar: View, context: Context) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        seekbarRunnable = object : Runnable {
            override fun run() {
                try {
                    val currentPosition = mapViewModel.mp!!.currentPosition
                    val duration = mapViewModel.mp!!.duration

                    seekbar.progress = currentPosition.toInt()

                    // Update bottom custom seekbar
                    val trackPercentage = (if (duration != 0) currentPosition / duration else 0.0f) as Float
                    val pixels = (screenWidth * trackPercentage).toInt()
                    customSeekbar.layoutParams.width = pixels
                    customSeekbar.requestLayout()

                    handler.postDelayed(this, 100)
                } catch (e: Exception) {
                    Log.e("Warmbier", e.toString())
                }
            }

        }
    }


    private fun pauseAudio() {
        Log.d("AudioWarmbier", "pausing playing")
        mapViewModel.mp!!.pause()
        toggleButtons(false)
    }

    private fun resumeAudio() {
        Log.d("AudioWarmbier", "resume playing")
        mapViewModel.mp!!.start()
        toggleButtons(true)
    }

    private fun toggleButtons(setActive: Boolean) {
        for (button in buttonList) {
            button.isActivated = setActive
        }
    }

}