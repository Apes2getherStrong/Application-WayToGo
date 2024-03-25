package loch.golden.waytogo.map.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.Marker
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.databinding.FragmentMarkerCreationBinding
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

class MarkerCreationFragment(
    private val marker: Marker?,
    private val routeCreationManager: RouteCreationManager,
    private val mapBinding: FragmentMapBinding
) : Fragment() {

    private lateinit var binding: FragmentMarkerCreationBinding
    private val id by lazy {
        marker?.snippet
    }

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
            routeCreationManager.setCurrentMarkerId(marker?.snippet!!)
            routeCreationManager.hideInfoWindow(marker?.snippet!!)
        }
        mapBinding.expandedPanel.creationSeekbar.isEnabled = false
    }


}