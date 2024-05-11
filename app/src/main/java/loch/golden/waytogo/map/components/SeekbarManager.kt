package loch.golden.waytogo.map.components

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import loch.golden.waytogo.R
import loch.golden.waytogo.map.MapViewModel


//class SeekbarManager(
//    private val context: Context,
//    private val mapViewModel: MapViewModel,
//    private val seekbar: SeekBar,
//    private val customSeekbar: View,
//    private val playPauseButtons: List<ImageButton>
//) {
//
//    private val handler: Handler by lazy {
//        Handler(Looper.getMainLooper())
//    }
//
//    private val screenWidth by lazy {
//        Log.d("DisplayMetrics", context.resources.displayMetrics.widthPixels.toString())
//        context.resources.displayMetrics.widthPixels
//    }
//
//    private val seekbarRunnable by lazy {
//        object : Runnable {
//            val percentList = mutableListOf<Float>() // this is for debugging
//            override fun run() {
//                try {
//                    val currentPosition = mapViewModel.mp!!.currentPosition.toFloat()
//                    val duration = mapViewModel.mp!!.duration.toFloat()
//
//                    // Update marker details seekbar
//                    seekbar.progress = currentPosition.toInt()
//
//                    // Update bottom custom seekbar
//                    val trackPercentage = if (duration != 0.0f) currentPosition / duration else 0.0f
//                    val pixels = (screenWidth * trackPercentage).toInt()
//                    Log.d("PercentPixel", pixels.toString())
//                    percentList.add(trackPercentage)
//                    customSeekbar.layoutParams.width = pixels
//                    customSeekbar.requestLayout()
//                    Log.d("PixelWidth", customSeekbar.layoutParams.width.toString())
//
//
//                    //how often to refresh the seekbar
//                    handler.postDelayed(this, 50)
//                } catch (e: Exception) {
//                    Log.d("Warmbier", e.toString())
//                }
//            }
//
//        }
//    }
//
//
//    init {
//        setUpMediaPlayer(R.raw.piosenka)
//        if (mapViewModel.mp != null) {
//            togglePlayPauseIcons()
//            initSeekbar()
//        }
//    }
//
//    private fun setUpMediaPlayer(trackId: Int) {
//        val playButtonClickListener = View.OnClickListener {
//            if (mapViewModel.mp == null) {
//                mapViewModel.mp = MediaPlayer.create(context, trackId)
//                mapViewModel.mp?.start()
//                initSeekbar()
//            } else if (mapViewModel.mp!!.isPlaying)
//                mapViewModel.mp?.pause()
//            else
//                mapViewModel.mp?.start()
//            togglePlayPauseIcons()
//        }
//
//        for (button in playPauseButtons) {
//            button.setOnClickListener(playButtonClickListener)
//        }
//    }
//
//    private fun initSeekbar() {
//        seekbar.max = mapViewModel.mp!!.duration
//        handler.postDelayed(seekbarRunnable, 0)
//
//        seekbar.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) mapViewModel.mp?.seekTo(progress)
//            }
//
//            override fun onStartTrackingTouch(seekbar: SeekBar?) {
//                // when start tracking
//            }
//
//            override fun onStopTrackingTouch(seekbar: SeekBar?) {
//                //when end tracking
//            }
//        })
//
//        mapViewModel.mp!!.setOnCompletionListener {
//            mapViewModel.mp?.seekTo(0)
//            togglePlayPauseIcons()
//        }
//    }
//
//    private fun togglePlayPauseIcons() {
//        val playPauseDrawable = if (mapViewModel.mp!!.isPlaying) {
//            R.drawable.ic_pause_24
//        } else {
//            R.drawable.ic_play_arrow_24
//        }
//
//        for (button in playPauseButtons) {
//            button.setImageResource(playPauseDrawable)
//        }
//    }
//
//    fun startTracking(mp: MediaPlayer) {
//
//    }
//
//
//    fun removeCallback() {
//        handler.removeCallbacks(seekbarRunnable)
//    }
//}