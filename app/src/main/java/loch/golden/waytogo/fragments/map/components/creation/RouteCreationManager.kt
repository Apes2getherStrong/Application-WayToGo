package loch.golden.waytogo.fragments.map.components.creation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import loch.golden.waytogo.utils.Permissions
import loch.golden.waytogo.R
import loch.golden.waytogo.viewmodels.classes.MapPoint
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.fragments.map.components.SeekbarManagerV2
import loch.golden.waytogo.viewmodels.MapViewModel
import loch.golden.waytogo.fragments.map.views.PointMapFragment
import loch.golden.waytogo.fragments.map.views.MarkerCreationFragment
import loch.golden.waytogo.room.entity.maplocation.MapLocation
import loch.golden.waytogo.utils.Constants
import loch.golden.waytogo.viewmodels.RouteViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class RouteCreationManager(
    private val binding: FragmentMapBinding,
    private val infoWindowManager: InfoWindowManager,
    private val fragment: PointMapFragment,
    private val routeViewModel: RouteViewModel,
    private val mapViewModel: MapViewModel
) : OnMarkerDragListener {

    enum class MediaType {
        IMAGE,
        AUDIO
    }

    private val infoWindowMap: MutableMap<String, InfoWindow> = mutableMapOf()
    private val creationMarkerMap: MutableMap<String, Marker?> = mutableMapOf()
    private var currentMarkerId: String? = null
    private lateinit var routeId: String


    private val mediaRecorderDelegate = lazy {
        MediaRecorder()
    }
    private val mediaRecorder by mediaRecorderDelegate
    private var isRecording = false

    private lateinit var seekbarManager: SeekbarManagerV2


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
            val outputFile = getOutputFile(currentMarkerId!!, MediaType.IMAGE)
            val outputFilePath = outputFile.absolutePath
            val outputStream = FileOutputStream(outputFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                    mapViewModel.route!!.pointList[currentMarkerId]?.photoPath = outputFilePath
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Warmbier", e.toString())
        }
    }

    fun generateMarkerId() = UUID.randomUUID().toString()


    init {
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
        binding.expandedPanel.creationTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val mapPoint = mapViewModel.route!!.pointList[currentMarkerId]!!
                mapPoint.name = binding.expandedPanel.creationTitle.text.toString()
                routeViewModel.updateMapLocation(MapLocation(mapPoint))
                binding.expandedPanel.creationTitle.clearFocus()
                fragment.requireContext().hideKeyboard(binding.expandedPanel.creationTitle)
                true
            } else false
        }
        binding.expandedPanel.creationDescription.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.expandedPanel.creationDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.expandedPanel.creationDescription.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val mapPoint = mapViewModel.route!!.pointList[currentMarkerId]!!
                mapPoint.description = binding.expandedPanel.creationDescription.text.toString()
                routeViewModel.updateMapLocation(MapLocation(mapPoint))
                binding.expandedPanel.creationDescription.clearFocus()
                fragment.requireContext().hideKeyboard(binding.expandedPanel.creationDescription)
                true
            } else false
        }


        seekbarManager = SeekbarManagerV2(
            mapViewModel,
            binding.expandedPanel.creationSeekbar,
            listOf(binding.expandedPanel.creationPlayPause)
        )


    }


    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    fun startExisting(routeId: String, markerList: MutableList<Marker?>) {
        this.routeId = routeId
        for (marker in markerList) {
            marker?.isDraggable = true
            creationMarkerMap[marker?.snippet!!] = marker
            val infoWindow = InfoWindow(
                marker,
                InfoWindow.MarkerSpecification(0, 100),
                MarkerCreationFragment(marker, this, binding)
            )
            infoWindowMap[marker.snippet!!] = infoWindow
        }
        Log.d("Warmbier", infoWindowMap.toString())
        Log.d("Warmbier", creationMarkerMap.toString())
    }

    fun addMarker(marker: Marker?, infoWindow: InfoWindow) {
        val markerId = marker?.snippet!!
        val mapLocation = MapLocation(
            markerId,
            marker.title!!,
            "",
            marker.position.latitude,
            marker.position.longitude,
            null
        )
        val sequenceNr = mapViewModel.route!!.pointList.size + 1
        routeViewModel.insertMapLocation(mapLocation, routeId, sequenceNr)
        mapViewModel.route!!.pointList[markerId] = MapPoint(mapLocation, sequenceNr)
        creationMarkerMap[markerId] = marker
        infoWindowMap[markerId] = infoWindow
    }


    fun removeMarker(marker: Marker?) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Delete Marker?")
            .setMessage(" You can't undo this action")
            .setPositiveButton("Delete") { dialog, _ ->
                val markerId = marker?.snippet!!
                creationMarkerMap.remove(markerId)
                hideInfoWindow(markerId)
                routeViewModel.deleteMapLocation( //TODO do it only by id
                    MapLocation(
                        markerId,
                        marker.title!!,
                        "",
                        marker.position.latitude,
                        marker.position.longitude,
                        null
                    )
                )
                mapViewModel.route?.pointList?.remove(markerId)
                infoWindowMap.remove(markerId)
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

    // TODO change point name on add

    fun getInfoWindow(id: String) = infoWindowMap[id]!!
    fun hideInfoWindow(id: String) {
        if (infoWindowMap[id]!!.windowState in setOf(
                InfoWindow.State.SHOWN,
                InfoWindow.State.SHOWING
            )
        )
            infoWindowManager.toggle(infoWindowMap[id]!!)
    }


    private fun startRecording() {
        Log.d("Warmbier", "Start recording: $isRecording")
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(getOutputFile(currentMarkerId!!, MediaType.AUDIO).absolutePath)
        }
        binding.expandedPanel.recordButton.setIconTintResource(R.color.red)
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
        } catch (e: IOException) {
            Log.d("Warmbier", e.toString())
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Log.d("Warmbier", "Stop recording: $isRecording")
        binding.expandedPanel.recordButton.setIconTintResource(R.color.color3)
        if (isRecording) {
            mediaRecorder.stop()
            isRecording = false
            val outputFilePath = getOutputFile(currentMarkerId!!, MediaType.AUDIO).absolutePath
            mapViewModel.route!!.pointList[currentMarkerId]?.audioPath = outputFilePath
            seekbarManager.prepareAudio(outputFilePath)
            binding.expandedPanel.creationPlayPause.isEnabled= true
        }
    }


    fun onEditMarker(id: String) {
        this.currentMarkerId = id
        binding.expandedPanel.creationTitle.setText(mapViewModel.route!!.pointList[id]?.name)
        binding.expandedPanel.creationDescription.setText(mapViewModel.route!!.pointList[id]?.description)
        if (mapViewModel.route!!.pointList[id]?.photoPath != null) {
            val bitmap = BitmapFactory.decodeFile(mapViewModel.route!!.pointList[id]?.photoPath)
            binding.expandedPanel.creationAddImage.setImageBitmap(bitmap)
        } else {
            binding.expandedPanel.creationAddImage.setImageResource(R.drawable.ic_add_photo_24)
        }
        if (mapViewModel.route!!.pointList[id]?.audioPath != null) {
            seekbarManager.prepareAudio(mapViewModel.route!!.pointList[id]?.audioPath!!)
            binding.expandedPanel.creationPlayPause.isEnabled = true

        } else {
            mapViewModel.mp = null
            binding.expandedPanel.creationSeekbar.isEnabled = false
            binding.expandedPanel.creationPlayPause.isEnabled = false
        }
    }


    private fun getOutputFile(fileName: String, mediaType: MediaType): File {
        val extension = if (mediaType == MediaType.IMAGE) Constants.IMAGE_EXTENSION else Constants.AUDIO_EXTENSION
        val directory = if (mediaType == MediaType.IMAGE) Constants.IMAGE_DIR else Constants.AUDIO_DIR
        return File(fragment.requireContext().filesDir, "$directory/$fileName$extension")
    }


    override fun onMarkerDrag(marker: Marker) {
        val id = marker.snippet!!
        hideInfoWindow(id)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val id = marker.snippet!!
        infoWindowMap[id]!!.position = marker.position
        val mapPoint = mapViewModel.route!!.pointList[id]!!
        mapPoint.position = marker.position
        routeViewModel.updateMapLocation(MapLocation(mapPoint))
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