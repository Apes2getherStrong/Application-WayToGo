package loch.golden.waytogo.routes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.Route

class RecyclerViewRouteAdapter :
    PagingDataAdapter<Route, RecyclerViewRouteAdapter.RouteViewHolder>(ROUTE_DIFF_CALLBACK) {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_routes_item, parent, false)
        return RouteViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = getItem(position)
        route?.let { routeItem ->
            holder.bind(routeItem)
            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onItemClick(position,routeItem)
                }
            }
        }
    }

    companion object {
        private val ROUTE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Route>() {
            override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
                return oldItem.routeUid == newItem.routeUid
            }

            override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
                return oldItem == newItem
            }

        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.description_text_view)

        fun bind(route: Route) {
            titleTextView.text = route.name
            descriptionTextView.text = route.description
            //imageView.setImageResource(route.image)

        }
    }

    interface OnClickListener {
        fun onItemClick(position: Int, route: Route)
    }

}


