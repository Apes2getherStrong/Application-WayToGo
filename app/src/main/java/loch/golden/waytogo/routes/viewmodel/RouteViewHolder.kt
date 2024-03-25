package loch.golden.waytogo.routes.viewmodel

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.Route

class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    private val descriptionTextView: TextView =
        itemView.findViewById(R.id.description_text_view)

    fun bind(route: Route) {
        titleTextView.text = route.name
        descriptionTextView.text = route.description.take(50)
        //imageView.setImageResource(route.image)

    }
}