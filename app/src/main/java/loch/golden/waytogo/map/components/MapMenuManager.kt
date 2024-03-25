package loch.golden.waytogo.map.components

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import loch.golden.waytogo.R

class MapMenuManager(
    context: Context,
    private val menuButton: FloatingActionButton,
    private val menuItems: List<View>
) {
    //TODO make the animation go from the top not bottom without affecting the space between them in animation layouts
    private val openAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.map_menu_open_anim)
    }
    private val closeAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.map_menu_close_anim)
    }

    private var isMenuOpen: Boolean = true

    init {
        menuButton.setOnClickListener {

            isMenuOpen = !isMenuOpen

            for (menuItem in menuItems) {
                setVisibility(menuItem)
                setAnimation(menuItem)
                setClickable(menuItem)
            }
        }
    }

    private fun setClickable(menuItem: View) {
        menuItem.isClickable = !isMenuOpen
    }

    private fun setAnimation(menuItem: View) {
        if (!isMenuOpen) {
            menuItem.startAnimation(openAnim)
        } else {
            menuItem.startAnimation(closeAnim)
        }
    }

    private fun setVisibility(menuItem: View) {
        if (!isMenuOpen) {
            menuItem.visibility = View.VISIBLE

        } else {
            menuItem.visibility = View.INVISIBLE
        }
    }
}