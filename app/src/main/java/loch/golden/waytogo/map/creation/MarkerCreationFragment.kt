package loch.golden.waytogo.map.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.Marker
import loch.golden.waytogo.databinding.FragmentMarkerCreationBinding


class MarkerCreationFragment(private val marker: Marker?) : Fragment() {

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

    }

    override fun onDestroy() {
        Toast.makeText(requireContext(), "DYing righnt now", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

}