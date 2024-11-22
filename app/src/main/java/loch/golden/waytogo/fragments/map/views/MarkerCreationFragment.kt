package loch.golden.waytogo.fragments.map.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.Marker
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.hilt.android.AndroidEntryPoint
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.databinding.FragmentMarkerCreationBinding
import loch.golden.waytogo.fragments.map.components.creation.RouteCreationManager
import loch.golden.waytogo.viewmodels.MapViewModel

class MarkerCreationFragment(
    private val marker: Marker?,
    private val routeCreationManager: RouteCreationManager,
    private val mapBinding: FragmentMapBinding
) : Fragment() {

    private lateinit var binding: FragmentMarkerCreationBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarkerCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checlkbox.setOnClickListener {
            marker?.isDraggable = !binding.checlkbox.isChecked
        }
        binding.buttonDelete.setOnClickListener {
            routeCreationManager.removeMarker(marker)
        }
        binding.buttonEdit.setOnClickListener {
            mapBinding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            mapBinding.expandedPanel.creationTitle.setText(marker?.title)
            routeCreationManager.onEditMarker(marker?.snippet!!)
            routeCreationManager.hideInfoWindow(marker.snippet!!)
            mapBinding.slideUpPanel.isTouchEnabled = true
        }
    }


}