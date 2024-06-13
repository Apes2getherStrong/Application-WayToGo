package loch.golden.waytogo.routes.utils

class Constants {
    companion object{
        const val BASE_URL = "http://192.168.0.192:8090/api/v1/"
        const val AUDIO_EXTENSION = ".3gp"
        const val IMAGE_EXTENSION = ".jpg"
        const val IMAGE_DIR = "images"
        const val AUDIO_DIR = "audios"
        const val LOGIN_URL = "auth/login"
        const val REGISTER_URL = "auth/register"

        const val GOOGLE_API_URL = "https://maps.googleapis.com/"
        const val GOOGLE_NAVIGATION_ENDPOINT = "maps/api/directions/json"


        const val MAX_DESCRIPTION_LENGTH = 50
    }
}