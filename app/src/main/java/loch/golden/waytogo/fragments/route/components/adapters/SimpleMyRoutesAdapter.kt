package loch.golden.waytogo.fragments.route.components.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import loch.golden.waytogo.R
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.utils.Constants.Companion.IMAGE_DIR
import loch.golden.waytogo.utils.Constants.Companion.IMAGE_EXTENSION
import java.io.File

class SimpleMyRoutesAdapter(
    private var routes: List<Route>,
    private val context: Context,
) :
    RecyclerView.Adapter<SimpleMyRoutesAdapter.SimpleViewHolder>() {

    private var onClickListener: OnClickListener? = null
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
        holder.itemView.setOnClickListener { view ->
            when (view.id) {
                R.id.remove_image_view -> onClickListener?.onItemClick(position, route, true)
                else -> onClickListener?.onItemClick(position, route, false)
            }
        }
        holder.delete.setOnClickListener {
            onClickListener?.onItemClick(position,route,true)
        }
    }

    fun setRoutes(routes: List<Route>?) {
        this.routes = routes!!
        notifyDataSetChanged()
    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.description_text_view)
        private val imageView =
            itemView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.image_view_route)
        val delete: ImageView = itemView.findViewById(R.id.remove_image_view)

        fun bind(route: Route) {
            titleTextView.text = route.name
            descriptionTextView.text = route.description
            val imageFile = File(
                context.filesDir,
                "$IMAGE_DIR/${route.routeUid}$IMAGE_EXTENSION"
            )
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ic_route_24)  // Optional: Set a placeholder
            }

        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onItemClick(position: Int, route: Route, isDelete: Boolean)
    }


}

