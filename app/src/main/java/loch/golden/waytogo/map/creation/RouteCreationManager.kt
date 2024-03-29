package loch.golden.waytogo.map.creation

import android.Manifest
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import loch.golden.waytogo.Permissions
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.PointMapFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class RouteCreationManager(
    private val binding: FragmentMapBinding,
    private val infoWindowManager: InfoWindowManager,
    private val fragment: PointMapFragment,
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
        MediaRecorder()
    }
    private val mediaRecorder by mediaRecorderDelegate

    private var isRecording = false

    private lateinit var routeId: String

    private val getContent =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            //TODO add cropping mechanism from this lib https://github.com/CanHub/Android-Image-Cropper
            uri?.let {
                binding.expandedPanel.creationAddImage.setImageURI(uri)
                saveImage(uri)
            }
        }

    private fun saveImage(imageUri: Uri) {
        try {
            val inputStream = fragment.requireContext().contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(getOutputFile(currentMarkerId!!, MediaType.IMAGE))
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("Warmbier", "save successful")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Warmbier", e.toString())
        }
    }

    fun generateMarkerId() = UUID.randomUUID().toString()


    init {
        // TODO Opening the point with id
        // TODO Fix currentMarker id having !! in saveImage and start Recording
        // TODO do something with opening the sliding up panel without edit option
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

    fun startNew(routeTitle: String) {
        this.routeId = UUID.randomUUID().toString()
        setRouteTitle(routeTitle)
        CreationPrefManager
        initFolders()
    }

    fun startExisting(routeId: String) {
        this.routeId = routeId
        getExistingPoints(routeId)
        initFolders()
    }

    private fun getExistingPoints(routeId: String) {
        routeTitle = CreationPrefManager.getRouteTitle(fragment.requireContext(), routeId)!!

    }


    private fun initFolders() {
        Log.d("Warmbier", routeId)
        val mainFolder = File(fragment.requireContext().filesDir, CREATION_DIRECTORY)
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }
        val folder = File(fragment.requireContext().filesDir, "$CREATION_DIRECTORY/${routeId}")
        if (!folder.exists()) {
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
        val markerId = marker?.snippet!!
        creationMarkerMap[markerId] = marker
        infoWindowMap[markerId] = infoWindow
        CreationPrefManager.putMarkerInRoute(
            fragment.requireContext(),
            routeId,
            markerId,
            marker.title!!
        )
    }


    fun removeMarker(marker: Marker?) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Delete Marker?")
            .setMessage(" You can't undo this action")
            .setPositiveButton("Delete") { dialog, _ ->
                val markerId = marker?.snippet!!
                creationMarkerMap.remove(markerId)
                hideInfoWindow(markerId)
                infoWindowMap.remove(markerId)
                CreationPrefManager.removeMarker(fragment.requireContext(), routeId, markerId)
                deleteFiles(markerId)
                marker.remove()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun deleteFiles(markerId: String) {
        val audioFile = getOutputFile(markerId, MediaType.AUDIO)
        val imageFile = getOutputFile(markerId, MediaType.IMAGE)
        if (audioFile.exists())
            audioFile.delete()
        if (imageFile.exists())
            imageFile.delete()
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
        this.routeTitle = title
        CreationPrefManager.putTitle(fragment.requireContext(), routeId, title)
    }

    private fun startRecording() {
        Log.d("Warmbier", "Start recording: $isRecording")
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(getOutputFile(currentMarkerId!!, MediaType.AUDIO).absolutePath)
        }

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            Log.d("Warmbier", "SHOULD BE RECORDING")
            isRecording = true
            binding.expandedPanel.recordButton.setBackgroundColor(Color.RED)
        } catch (e: IOException) {
            Log.d("Warmbier", e.toString())
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Log.d("Warmbier", "Stop recording: $isRecording")
        if (isRecording) {
            mediaRecorder.stop()
            binding.expandedPanel.recordButton.setBackgroundColor(Color.WHITE)
            isRecording = false
        }
    }

    private fun getOutputFile(fileName: String, mediaType: MediaType): File {
        val extension = if (mediaType == MediaType.IMAGE) IMAGE_EXTENSION else AUDIO_EXTENSION
        val directory = if (mediaType == MediaType.IMAGE) IMAGE_DIRECTORY else AUDIO_DIRECTORY
        return File(
            fragment.requireContext().filesDir,
            "$CREATION_DIRECTORY/${routeId}/$directory/$fileName$extension"
        )
    }

    fun clearCreationMarkers() {
        creationMarkerMap.forEach { (_, marker) ->
            marker!!.remove()
        }

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

}