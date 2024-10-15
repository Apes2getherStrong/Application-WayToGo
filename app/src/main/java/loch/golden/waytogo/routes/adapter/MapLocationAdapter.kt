package loch.golden.waytogo.routes.adapter

import android.graphics.BitmapFactory
import android.media.Image
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.routes.model.maplocation.MapLocation

class MapLocationAdapter(private val mapLocations: List<MapPoint>) :
    RecyclerView.Adapter<MapLocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_maplocations_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mapLocation = mapLocations[position]
        holder.bind(mapLocation)

    }


    override fun getItemCount(): Int {
        return mapLocations.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mapLocationTextView: TextView = itemView.findViewById(R.id.name_mapLocation_text_view)
        private val mapLocationDescription: TextView = itemView.findViewById(R.id.mapLocation_description_text_view)
        private val mapLocationImage: ImageView = itemView.findViewById(R.id.image_view_mapLocation)
        private val mapLocationNumber: TextView = itemView.findViewById(R.id.number)

        fun bind(mapLocation: MapPoint) {
            mapLocationTextView.text = mapLocation.name
            mapLocationDescription.text = mapLocation.description
            mapLocationNumber.text = mapLocation.sequenceNr.toString()
            if (mapLocation.photoPath != null) {
                val bitmap = BitmapFactory.decodeFile(mapLocation.photoPath)
                mapLocationImage.setImageBitmap(bitmap)
            }
        }


    }
}
