package loch.golden.waytogo.fragments.map.components.creation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import loch.golden.waytogo.R

//Used for changing the info window on marker press
class PointInfoWindowAdapter(
    private val context: Context
) : InfoWindowAdapter {

    //change the whole window
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    //Change the contents of the info window
    override fun getInfoContents(p0: Marker): View {
        var view: View = LayoutInflater.from(context).inflate(R.layout.layout_marker_window, null)
        val markerImage = view.findViewById<ImageView>(R.id.image_view_marker_image)
        view.setOnClickListener {
            Toast.makeText(context, "Siema", Toast.LENGTH_SHORT).show()
        }
        return view
    }


}