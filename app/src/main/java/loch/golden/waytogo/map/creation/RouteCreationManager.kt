package loch.golden.waytogo.map.creation

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import loch.golden.waytogo.Permissions
import loch.golden.waytogo.classes.RoutePoint
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.PointMapFragment
import java.io.File
import java.io.IOException

class RouteCreationManager(
    private val binding: FragmentMapBinding,
    private val infoWindowManager: InfoWindowManager,
    private val fragment: PointMapFragment
) : OnMarkerDragListener {
    companion object {
        private const val AUDIO_DIRECTORY = "Recordings"
        private const val AUDIO_EXTENSION = ".3gp"
        private const val PHOTO_DIRECTORY = "Photos"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val CREATION_DIRECTORY = "my_routes"
    }

    private val infoWindowMap: MutableMap<String, InfoWindow> = mutableMapOf()
    private val creationMarkerMap: MutableMap<String, Marker?> = mutableMapOf()
    private val routePointMap: MutableMap<String, RoutePoint> = mutableMapOf()
    private var markerId = 1

    private var routeTitle: String = "My Route"

    private var currentMarkerId: String? = null

    private lateinit var mediaRecorder: MediaRecorder

    private var isRecording = false

    private var routeId: Int? = null

    private val getContent =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri
            uri?.let {
                binding.expandedPanel.creationAddImage.setImageURI(uri)
            }
        }

    init {
        //TODO FIX creating folder and shared prefs this is voodoo
        //TODO CHECK IF the folder/name already exists and all that boring ahh stuff
        //TODO FIX LEAVING ROUTE CREATION
        initFolders()


        binding.expandedPanel.creationAddImage.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.expandedPanel.recordButton.setOnClickListener {
            if (Permissions.isPermissionGranted(
                    fragment.requireContext(),
                    Manifest.permission.RECORD_AUDIO
                )
            )
                if (!isRecording) startRecording()
                else stopRecording()
            else
                Permissions.requestPermission(
                    fragment.requireActivity(),
                    Manifest.permission.RECORD_AUDIO,
                    Permissions.RECORD_AUDIO_REQUEST_CODE
                )
        }

    }

    private fun initFolders() {
        val mainFolder = File(fragment.requireContext().filesDir, CREATION_DIRECTORY)
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }
        routeId = RouteIdManager.getCounterAndInc(fragment.requireContext())
        val folder = File(fragment.requireContext().filesDir, "$CREATION_DIRECTORY/${routeId}")
        if (!folder.exists()) {
            mainFolder.mkdirs()
            File(
                fragment.requireContext().filesDir,
                "$CREATION_DIRECTORY/${routeId}/$AUDIO_DIRECTORY"
            ).mkdirs()
            File(
                fragment.requireContext().filesDir,
                "$CREATION_DIRECTORY/${routeId}/$PHOTO_DIRECTORY"
            ).mkdirs()
            RouteIdManager.putTitle(fragment.requireContext(), routeId!!, routeTitle)
        }

    }

    fun addMarker(marker: Marker?, infoWindow: InfoWindow) {
        val id = marker?.snippet!!
        creationMarkerMap[id] = marker
        infoWindowMap[id] = infoWindow
        routePointMap[id] = RoutePoint(
            marker.title!!, marker.position,
            getAudioPath(id),
            getImagePath(id)
        )
    }

    private fun getAudioPath(id: String) =
        "${routeTitle}/${AUDIO_DIRECTORY}/point_${id}$AUDIO_EXTENSION"

    private fun getImagePath(id: String) =
        "${routeTitle}/${PHOTO_DIRECTORY}/point_${id}$PHOTO_EXTENSION"

    fun removeMarker(marker: Marker?) {
        val id = marker?.snippet!!
        creationMarkerMap.remove(id)
        hideInfoWindow(id)
        infoWindowMap.remove(id)
        routePointMap.remove(id)
        marker.remove()
    }

    fun getCurrentId() = markerId++
    fun getInfoWindow(id: String) = infoWindowMap[id]!!
    fun hideInfoWindow(id: String) {
        if (infoWindowMap[id]!!.windowState in setOf(
                InfoWindow.State.SHOWN,
                InfoWindow.State.SHOWING
            )
        )
            infoWindowManager.toggle(infoWindowMap[id]!!)
    }


    fun setRouteTitle(title: String) {
        this.routeTitle = title
        RouteIdManager.putTitle(fragment.requireContext(), routeId!!, title)
    }

    private fun startRecording() {
        //this would harm backward compatibility
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(
                getOutputFile(
                    fragment.requireContext(),
                    AUDIO_DIRECTORY,
                    creationMarkerMap[currentMarkerId]!!.snippet!!

                ).absolutePath
            )

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.expandedPanel.recordButton.setBackgroundColor(Color.RED)
            start()
        }
        isRecording = true
    }

    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            release()
        }
        binding.expandedPanel.recordButton.setBackgroundColor(Color.WHITE)
        isRecording = false
    }

    private fun getOutputFile(context: Context, directoryName: String, fileName: String): File {
        val folder = File(context.filesDir, "MyRecordings")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val extension = if (directoryName == PHOTO_DIRECTORY) PHOTO_EXTENSION else AUDIO_EXTENSION

        return File(folder, "${fileName}_${System.currentTimeMillis()}$extension")
    }

    fun setCurrentMarkerId(id: String) {
        this.currentMarkerId = id
    }

    override fun onMarkerDrag(marker: Marker) {
        val id = marker.snippet!!
        hideInfoWindow(id)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val id = marker.snippet!!
        infoWindowMap[id]!!.position = marker.position
    }

    override fun onMarkerDragStart(marker: Marker) {
        val id = marker.snippet!!
        hideInfoWindow(id)
    }

    fun onDestroy() {
        stopRecording()
    }

    //TODO PROBABLY ADD MARKER CREATION LISTENER TO THIS CLASS
}