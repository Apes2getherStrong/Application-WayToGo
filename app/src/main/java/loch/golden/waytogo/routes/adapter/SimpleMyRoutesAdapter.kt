package loch.golden.waytogo.routes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.route.Route

class SimpleMyRoutesAdapter(private var routes: List<Route> ) : RecyclerView.Adapter<SimpleMyRoutesAdapter.SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_routes_item, parent, false)
        return SimpleViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        val route = routes[position]
        holder.bind(route)
    }

    fun setRoutes(routes: List<Route>?) {
        this.routes = routes!!
        notifyDataSetChanged()
    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        fun bind(route: Route) {
            itemView.apply {
                titleTextView.text = route.name
                descriptionTextView.text = route.description
            }
        }

    }


}
