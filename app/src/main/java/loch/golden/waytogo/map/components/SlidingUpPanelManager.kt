package loch.golden.waytogo.map.components

import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import loch.golden.waytogo.databinding.FragmentMapBinding

class SlidingUpPanelManager(
    private val binding: FragmentMapBinding
) : PanelSlideListener {
    private var inCreationMode: Boolean = false

    //TODO make something nice with bottomSlidingPanel on creation :-)
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
        }
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        val maxVisibilitySlideOffset = 0.2f
        binding.bottomPanel.container.alpha =
            1.0f - (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
        binding.expandedPanel.container.alpha =
            (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
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
        binding.expandedPanel.normal.visibility = View.GONE
        binding.expandedPanel.creation.visibility = View.VISIBLE
        binding.bottomPanel.normal.visibility = View.GONE
        binding.bottomPanel.creation.visibility = View.VISIBLE
        inCreationMode = !inCreationMode
    }

    fun openCreationPanel(markerId: String) {
        //TODO create this fun
    }

    fun openNormalPanel() {
        //TODO create this fun probably add params
    }


    //This was not tested thoroughly
    private fun bottomPanelListener() {
        if (inCreationMode)
            return
        else {
            binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

    }

}