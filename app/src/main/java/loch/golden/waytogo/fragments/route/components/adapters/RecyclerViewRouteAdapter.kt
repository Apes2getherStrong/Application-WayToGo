package loch.golden.waytogo.fragments.route.components.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import loch.golden.waytogo.R
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.viewmodels.BackendViewModel
import retrofit2.Response

class RecyclerViewRouteAdapter(
    val viewModel: BackendViewModel,
    val lifecycleScope: CoroutineScope
) :
    PagingDataAdapter<Route, RecyclerViewRouteAdapter.RouteViewHolder>(ROUTE_DIFF_CALLBACK) {

    private var onClickListener: OnClickListener? = null
    private val routeImagesMap = mutableMapOf<String, Bitmap?>()  // Map to store route images


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
                if (onClickListener != null) {
                    onClickListener!!.onItemClick(position, routeItem)
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

    fun updateRouteImage(routeId: String, bitmap: Bitmap) {
        routeImagesMap[routeId] = bitmap  // Store the image in the map
        val position = snapshot().items.indexOfFirst { it.routeUid == routeId }
        if (position != -1) {
            notifyItemChanged(position)  // Notify adapter to refresh the item
        }
    }

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.description_text_view)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view_route)
        var delete: ImageView = itemView.findViewById(R.id.remove_image_view)


        fun bind(route: Route) {
            titleTextView.text = route.name
            descriptionTextView.text = route.description
            delete.visibility = View.INVISIBLE
            val cachedImage = routeImagesMap[route.routeUid]
            if (cachedImage != null)
                imageView.setImageBitmap(cachedImage)  // If cached, set it directly
            else {
                imageView.setImageResource(R.drawable.ic_route_24)  // Optional: Set a placeholder
                lifecycleScope.launch {
                    val response = safeApiCall { viewModel.getBlockingRouteImage(route.routeUid) }
                    response?.let {
                        val imageBytes = response.body()
                        if (imageBytes != null) {
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            updateRouteImage(route.routeUid, bitmap)
                            imageView.setImageBitmap(bitmap)
                            routeImagesMap[route.routeUid] = bitmap  // Store the image in the map
                        }
                    }
                }
            }
        }
    }

    suspend fun safeApiCall(apiCall: suspend () -> Response<ByteArray>): Response<ByteArray>? {
        return try {
            apiCall()
        } catch (e: Exception) {
            Log.e("API Error", "An error occurred: ${e.message}")
            null
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onItemClick(position: Int, route: Route)
    }

}


