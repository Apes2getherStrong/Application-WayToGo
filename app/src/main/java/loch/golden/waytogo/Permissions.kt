package loch.golden.waytogo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissions { //Maybe add this : ActivityCompat.OnRequestPermissionsResultCallback
    const val RECORD_AUDIO_REQUEST_CODE = 3001
    const val LOCATION_PERMISSION_REQUEST_CODE = 3002

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestCode
        )
    }

    fun requestPermission(activity: Activity, permissionArray: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            permissionArray,
            requestCode
        )
    }


}