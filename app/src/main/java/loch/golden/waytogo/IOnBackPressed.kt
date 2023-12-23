package loch.golden.waytogo

//Use this if u want to intercept back press in your fragment
interface IOnBackPressed {

    // if you return true the back press will be overwritten otherwise it will not be
    fun onBackPressed(): Boolean
}