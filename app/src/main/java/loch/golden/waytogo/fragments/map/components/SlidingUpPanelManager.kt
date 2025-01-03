package loch.golden.waytogo.fragments.map.components

import android.graphics.BitmapFactory
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import loch.golden.waytogo.R
import loch.golden.waytogo.viewmodels.classes.MapPoint
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.viewmodels.MapViewModel

class SlidingUpPanelManager(
    private val binding: FragmentMapBinding,
    private val mapViewModel: MapViewModel
) : PanelSlideListener {
    private var inCreationMode: Boolean = false

    init {
        setUpSlidingUpPanel()
        binding.bottomPanel.container.setOnClickListener {
            bottomPanelListener()
        }

    }

    private fun setUpSlidingUpPanel() {
        val slideUpPanel = binding.slideUpPanel
        slideUpPanel.addPanelSlideListener(this)
        slideUpPanel.setFadeOnClickListener {
            slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            if (inCreationMode) binding.slideUpPanel.isTouchEnabled = false

        }
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        val maxVisibilitySlideOffset = 0.2f
        binding.bottomPanel.container.alpha =
            1.0f - (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
        binding.expandedPanel.container.alpha =
            (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f) //TODO do better function
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {
        if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            binding.bottomPanel.container.visibility = View.VISIBLE
            binding.expandedPanel.container.visibility = View.VISIBLE
        }

        if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                binding.bottomPanel.container.visibility = View.GONE
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                binding.expandedPanel.container.visibility = View.GONE
        }
    }

    fun toggleCreation() {
        inCreationMode = !inCreationMode
        binding.slideUpPanel.isTouchEnabled = !inCreationMode
        binding.expandedPanel.container.visibility = View.GONE
        binding.apply {
            expandedPanel.normal.visibility = if (inCreationMode) View.GONE else View.VISIBLE
            expandedPanel.creation.visibility = if (inCreationMode) View.VISIBLE else View.GONE
            bottomPanel.normal.visibility = if (inCreationMode) View.GONE else View.VISIBLE
            bottomPanel.creation.visibility = if (inCreationMode) View.VISIBLE else View.GONE
        }

    }


    fun openNormalPanel(mapPoint: MapPoint?) {
        binding.expandedPanel.buttonSelectMarker.visibility = View.GONE
        binding.expandedPanel.seekbar.visibility = View.VISIBLE
        binding.expandedPanel.normalPlayPause.visibility = View.VISIBLE
        binding.expandedPanel.title.text = mapPoint?.name
        binding.expandedPanel.description.text = mapPoint?.description
//        updateBottomPanel(mapPoint)
        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        val bitmap = BitmapFactory.decodeFile(mapViewModel.route!!.pointList[mapPoint?.id]?.photoPath)
        if (bitmap != null) binding.expandedPanel.image.setImageBitmap(bitmap) else
            binding.expandedPanel.image.setImageResource(R.drawable.ic_no_photo_24)
    }

    fun updateBottomPanel(mapPoint: MapPoint?) {
        binding.bottomPanel.title.text = mapPoint?.name
    }

    fun openDifferentPanel(mapPoint: MapPoint?) {
        binding.expandedPanel.seekbar.visibility = View.GONE
        binding.expandedPanel.normalPlayPause.visibility = View.GONE
        binding.expandedPanel.buttonSelectMarker.visibility = View.VISIBLE
        binding.expandedPanel.title.text = mapPoint?.name
        binding.expandedPanel.description.text = mapPoint?.description
        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        val bitmap = BitmapFactory.decodeFile(mapViewModel.route!!.pointList[mapPoint!!.id]?.photoPath)
        if (bitmap != null) binding.expandedPanel.image.setImageBitmap(bitmap)
        else binding.expandedPanel.image.setImageResource(R.drawable.ic_no_photo_24)
    }


    //This was not tested thoroughly
    private fun bottomPanelListener() {
        //still makes sound tho
        if (inCreationMode)
            return
        else {
            if (mapViewModel.route != null)
                openNormalPanel(mapViewModel.currentPoint)
        }

    }

}