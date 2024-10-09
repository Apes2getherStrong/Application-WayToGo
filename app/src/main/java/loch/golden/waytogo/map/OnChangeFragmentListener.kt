package loch.golden.waytogo.map

import android.os.Bundle

interface OnChangeFragmentListener {
    fun changeFragment(fragmentNo: Int, bundle: Bundle? = null)
}