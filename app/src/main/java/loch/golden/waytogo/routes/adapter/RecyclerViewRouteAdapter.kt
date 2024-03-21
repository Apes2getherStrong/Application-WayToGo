package loch.golden.waytogo.routes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import loch.golden.waytogo.routes.viewmodel.RouteViewHolder

class RecyclerViewRouteAdapter(private var routeList: List<Route>) :
        PagingDataAdapter<Route,RouteViewHolder>(ROUTE_DIFF_CALLBACK) {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_routes_item, parent, false)
        return RouteViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = getItem(position)
        route?.let { holder.bind(it) }
//        holder.itemView.setOnClickListener {
//            if(onClickListener != null) {
//                onClickListener!!.onClick()
//            }
//        }
    }

    fun updateRoutes(newRoutes: List<Route>) {
        routeList = newRoutes
        notifyDataSetChanged()
    }
    companion object {
        private val ROUTE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Route>() {
            override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
                return oldItem == newItem
            }

        }
    }
}


