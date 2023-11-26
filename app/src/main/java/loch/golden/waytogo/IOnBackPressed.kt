package loch.golden.waytogo

interface IOnBackPressed {

    // if you return true the back press will be overritten otherwise it will not be
    fun onBackPressed(): Boolean
}