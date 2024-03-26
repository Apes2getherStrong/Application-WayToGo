package loch.golden.waytogo.map.creation

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import loch.golden.waytogo.Permissions
import loch.golden.waytogo.classes.Route
import loch.golden.waytogo.classes.RoutePoint
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.PointMapFragment
import java.io.File
import java.io.IOException
import java.util.UUID

class RouteCreationManager(
    private val binding: FragmentMapBinding,
    private val infoWindowManager: InfoWindowManager,
    private val fragment: PointMapFragment,
    private val Route: Route? = null //TODO make it work if route is already chosen (something with id no folders etc)
) : OnMarkerDragListener {
    companion object {
        private const val AUDIO_DIRECTORY = "recordings"
        private const val AUDIO_EXTENSION = ".3gp"
        private const val IMAGE_DIRECTORY = "photos"
        private const val IMAGE_EXTENSION = ".jpg"
        private const val CREATION_DIRECTORY = "my_routes"
    }

    enum class MediaType {
        IMAGE,
        AUDIO
    }

    private val infoWindowMap: MutableMap<String, InfoWindow> = mutableMapOf()
    private val creationMarkerMap: MutableMap<String, Marker?> = mutableMapOf()

    private var routeTitle: String = "My Route"

    private var currentMarkerId: String? = null

    private val mediaRecorderDelegate = lazy {
        //this is fine
        MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
    }
    private val mediaRecorder by mediaRecorderDelegate

    private var isRecording = false

    private val routeId: String = RouteIdManager.getId()

    private val getContent =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri
            uri?.let {
                binding.expandedPanel.creationAddImage.setImageURI(uri)
            }
        }

    fun generateMarkerId() = UUID.randomUUID().toString()


    init {
        //TODO check folders
        //TODO check if deleting is correct
        //TODO clear the map on switching back to creation
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

    fun start(routeTitle: String) {
        Log.d("Warmbier", "In start: $routeTitle")
        setRouteTitle(routeTitle)
        initFolders()
        RouteIdManager.putTitle(fragment.requireContext(), routeId, routeTitle)
    }

    private fun initFolders() {
        val mainFolder = File(fragment.requireContext().filesDir, CREATION_DIRECTORY)
        if (!mainFolder.exists()) {
            val created = mainFolder.mkdirs()
            Log.d("Warmbier", "Main folder: $created")
        }
        val folder = File(fragment.requireContext().filesDir, "$CREATION_DIRECTORY/${routeId}")
        Log.d("Warmbier", "Route id: $routeId")
        Log.d("Warmbier", "Id folder exists: ${folder.exists()}")
        if (!folder.exists()) {
            val created = folder.mkdirs()
            Log.d("Warmbier", "ID folder: $created")
            File(
                fragment.requireContext().filesDir,
                "$CREATION_DIRECTORY/${routeId}/$AUDIO_DIRECTORY"
            ).mkdirs()
            File(
                fragment.requireContext().filesDir,
                "$CREATION_DIRECTORY/${routeId}/$IMAGE_DIRECTORY"
            ).mkdirs()
        }

    }

    fun addMarker(marker: Marker?, infoWindow: InfoWindow) {
        val id = marker?.snippet!!
        creationMarkerMap[id] = marker
        infoWindowMap[id] = infoWindow
    }

    fun removeMarker(marker: Marker?) {
        val id = marker?.snippet!!
        creationMarkerMap.remove(id)
        hideInfoWindow(id)
        infoWindowMap.remove(id)
        marker.remove()
    }

    fun getInfoWindow(id: String) = infoWindowMap[id]!!
    fun hideInfoWindow(id: String) {
        if (infoWindowMap[id]!!.windowState in setOf(
                InfoWindow.State.SHOWN,
                InfoWindow.State.SHOWING
            )
        )
            infoWindowManager.toggle(infoWindowMap[id]!!)
    }


    private fun setRouteTitle(title: String) {
        Log.d("Warmbier", "In set Route Title: $title")
        this.routeTitle = title
        RouteIdManager.putTitle(fragment.requireContext(), routeId, title)
    }

    private fun startRecording() {
        mediaRecorder.setOutputFile(
            getOutputFile(currentMarkerId!!, MediaType.AUDIO).absolutePath
        )
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            Log.d("Warmbier","SHOULD BE RECORDING")
            isRecording = true
            binding.expandedPanel.recordButton.setBackgroundColor(Color.RED)
        } catch (e: IOException) {
            Log.d("Warmbier", e.toString())
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder.stop()
            binding.expandedPanel.recordButton.setBackgroundColor(Color.WHITE)
            isRecording = false
        }
    }

    private fun getOutputFile(fileName: String, mediaType: MediaType): File {
        val extension = if (mediaType == MediaType.IMAGE) IMAGE_EXTENSION else AUDIO_EXTENSION
        val directory = if (mediaType == MediaType.IMAGE) IMAGE_DIRECTORY else AUDIO_DIRECTORY
        return File(fragment.requireContext().filesDir,"$CREATION_DIRECTORY/${routeId}/$directory/$fileName$extension")
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
        if (mediaRecorderDelegate.isInitialized()) {
            stopRecording()
            mediaRecorder.release()
        }
    }

    //TODO PROBABLY ADD MARKER CREATION LISTENER TO THIS CLASS
}