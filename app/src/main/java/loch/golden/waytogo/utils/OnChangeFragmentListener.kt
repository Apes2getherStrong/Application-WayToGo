package loch.golden.waytogo.utils

import android.os.Bundle

interface OnChangeFragmentListener {
    fun changeFragment(fragmentNo: Int, bundle: Bundle? = null)
}