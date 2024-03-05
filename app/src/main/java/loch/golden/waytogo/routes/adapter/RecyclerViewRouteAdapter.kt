package loch.golden.waytogo.routes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.Route

class RecyclerViewRouteAdapter constructor(private var routeList: List<Route>) :
    RecyclerView.Adapter<RecyclerViewRouteAdapter.RouteViewHolder>() {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_routes_item, parent, false)
        return RouteViewHolder(itemView)
    }

    override fun getItemCount(): Int = routeList.size

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routeList[position]
        holder.bind(route)
//        holder.itemView.setOnClickListener {
//            if(onClickListener != null) {
//                onClickListener!!.onClick()
//            }
//        }
    }

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.description_text_view)

        fun bind(route: Route) {
            titleTextView.text = route.name
            descriptionTextView.text = route.description?.take(50)
            //imageView.setImageResource(route.image)

        }
    }

    fun updateRoutes(newRoutes: List<Route>) {
        routeList = newRoutes
        notifyDataSetChanged()
    }
}


