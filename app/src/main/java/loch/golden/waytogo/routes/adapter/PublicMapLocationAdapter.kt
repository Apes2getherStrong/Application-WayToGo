package loch.golden.waytogo.routes.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest

class PublicMapLocationAdapter(private val mapPointList: MutableList<MapPoint>) :
    RecyclerView.Adapter<PublicMapLocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_maplocations_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mapLocation = mapPointList[position]
        holder.bind(mapLocation)
    }


    override fun getItemCount(): Int {
        return mapPointList.size
    }

    fun updateMapPoint(updatedMapPoint: MapPoint) {
        val position = updatedMapPoint.sequenceNr - 1
        if (position != -1) {
            mapPointList[position] = updatedMapPoint
            notifyItemChanged(position)
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mapLocationTextView: TextView =
            itemView.findViewById(R.id.name_mapLocation_text_view)
        private val mapLocationDescription: TextView =
            itemView.findViewById(R.id.mapLocation_description_text_view)
        private val mapLocationImage: ImageView = itemView.findViewById(R.id.image_view_mapLocation)
        private val mapLocationNumber: TextView = itemView.findViewById(R.id.number)

        fun bind(mapPoint: MapPoint) {
            mapLocationTextView.text = mapPoint.name
            mapLocationDescription.text = mapPoint.description
            mapLocationNumber.text = mapPoint.sequenceNr.toString()
            Log.d("Warmbier", "In binding")
            if (mapPoint.photoPath != null) {
                Log.d("Warmbier", "In binding: photo not null")
                val bitmap = BitmapFactory.decodeFile(mapPoint.photoPath)
                mapLocationImage.setImageBitmap(bitmap)
            }
        }
    }
}
