package loch.golden.waytogo.map

import android.content.Context
import android.opengl.Visibility
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.forEach
import com.google.android.material.floatingactionbutton.FloatingActionButton
import loch.golden.waytogo.R

class MapMenuManager(
    context: Context,
    menuButton: FloatingActionButton,
    menuItems: List<ViewGroup>
) {
    private val openAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.map_menu_open_anim
        )
    }
    private val closeAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.map_menu_close_anim
        )
    }
    private var isMenuOpen: Boolean = false

    init {
        menuButton.setOnClickListener() {

            isMenuOpen = !isMenuOpen
            Toast.makeText(context, isMenuOpen.toString(), Toast.LENGTH_SHORT).show()
            for (menuItem in menuItems) {
                setVisibility(menuItem)
                setAnimation(menuItem)
            }
        }
    }

    private fun setAnimation(menuItem: ViewGroup) {
        if (isMenuOpen)
            menuItem.startAnimation(openAnim)
        else
            menuItem.startAnimation(closeAnim)
    }

    private fun setVisibility(menuItem: ViewGroup) {
        if (isMenuOpen) {
            menuItem.forEach { childView ->
                childView.visibility = View.VISIBLE
            }
        }
        else {
            menuItem.forEach { childView ->
                childView.visibility = View.INVISIBLE
            }
        }
    }
}