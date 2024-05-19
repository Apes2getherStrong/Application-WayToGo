package loch.golden.waytogo.routes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest

class PublicMapLocationAdapter(private val mapLocationsRequest: List<MapLocationRequest>) :
RecyclerView.Adapter<PublicMapLocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_maplocations_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mapLocation = mapLocationsRequest[position]
        holder.bind(mapLocation)

    }


    override fun getItemCount(): Int {
        return mapLocationsRequest.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mapLocationTextView: TextView = itemView.findViewById(R.id.name_mapLocation_text_view)
        private val mapLocationDescription: TextView = itemView.findViewById(R.id.mapLocation_description_text_view)

        fun bind(mapLocation: MapLocationRequest) {
            mapLocationTextView.text = mapLocation.name
            mapLocationDescription.text = mapLocation.description

        }
    }
}
