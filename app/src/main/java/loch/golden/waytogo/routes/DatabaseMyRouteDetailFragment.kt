package loch.golden.waytogo.routes

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import loch.golden.waytogo.R
import loch.golden.waytogo.audio.Audio
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentDatabaseMyRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnChangeFragmentListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.model.maplocation.Coordinates
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.routes.utils.Constants
import loch.golden.waytogo.routes.utils.Constants.Companion.IMAGE_DIR
import loch.golden.waytogo.routes.utils.Constants.Companion.IMAGE_EXTENSION
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.tokenmanager.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import java.util.UUID


class DatabaseMyRouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentDatabaseMyRouteDetailBinding
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private var changeFragmentListener: OnChangeFragmentListener? = null
    private lateinit var route: MapRoute
    private lateinit var routeEntity: Route
    private lateinit var mapLocationsOfRouteEntity: List<MapLocation>
    private val mapLocationIdMap = mutableMapOf<String, String>()
    private val audioIdMap = mutableMapOf<String, String>()
    private val routeMapLocationIdMap = mutableMapOf<String, String>()
    private var bottomNav: BottomNavigationView? = null

    private val getContent =
        this.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            //TODO add cropping mechanism from this lib https://github.com/CanHub/Android-Image-Cropper
            uri?.let {
                binding.addRouteImage.setImageURI(uri)
                saveImage(uri)
                val id = route.id
                val bundle = Bundle().apply {
                    putString("id", id)
                }
                changeFragmentListener?.changeFragment(2, bundle)
            }
        }

    private fun saveImage(imageUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            Log.d("Warmbier", "$IMAGE_DIR/${route.id}$IMAGE_EXTENSION")
            val outputFile =
                File(requireContext().filesDir, "$IMAGE_DIR/${route.id}$IMAGE_EXTENSION")
            val outputFilePath = outputFile.absolutePath
            val outputStream = FileOutputStream(outputFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                    route.photoPath = outputFilePath
                    Log.d("Warmbier", route.toString())
                }
            }
            Log.d("Warmbier", "spoko zapisalem se image")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Warmbier", e.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatabaseMyRouteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChangeFragmentListener) {
            changeFragmentListener = context
        } else {
            throw RuntimeException("$context must implement OnNavigateToMapListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addRouteImage.setOnClickListener {
            getContent.launch("image/*")
        }

        //handle back press
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    changeBackFragment()
                }
            })

        val routeId = arguments?.getString("id") ?: run {
            val route = Route(UUID.randomUUID().toString(), "My Route", "",null)
            routeViewModel.insert(route)
            route.routeUid
        }



        routeViewModel.getRouteWithMapLocations(routeId)
        routeViewModel.routeWithLocationsFromDb.observe(viewLifecycleOwner) { routeWithLocationsFromDb ->
            if (routeWithLocationsFromDb != null) {
                routeEntity = routeWithLocationsFromDb.route

                if(routeEntity.externalId != null) {
                    binding.publishRouteButton.text = "Update Published Route"
                    binding.deletePublishedRoute.visibility = View.VISIBLE

                }else {
                    binding.publishRouteButton.text = "Publish Route"
                    binding.deletePublishedRoute.visibility = View.GONE

                }

                val outputFile = File(
                    requireContext().filesDir,
                    "$IMAGE_DIR/${routeWithLocationsFromDb.route.routeUid}$IMAGE_EXTENSION"
                )
                var photoPath: String? = null
                if (outputFile.exists())
                    photoPath = outputFile.absolutePath

                route = MapRoute(
                    routeWithLocationsFromDb.route.routeUid,
                    routeWithLocationsFromDb.route.name,
                    routeWithLocationsFromDb.route.description,
                    mutableMapOf(),
                    photoPath = photoPath
                )
                Log.d("Warmbier", route.toString())
                binding.routeTitle.setText(routeWithLocationsFromDb.route.name)
                binding.routeDescription.setText(routeWithLocationsFromDb.route.description)
                if (route.photoPath != null) {
                    val bitmap = BitmapFactory.decodeFile(route.photoPath)
                    binding.addRouteImage.setImageBitmap(bitmap)
                }

                mapLocationsOfRouteEntity = routeWithLocationsFromDb.mapLocations
                routeWithLocationsFromDb.mapLocations.let {
                    for (mapLocation in it) {
                        val sequenceNr =
                            runBlocking { routeViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
                        Log.d("Warmbier", "Sequence Nr $sequenceNr name: ${mapLocation.name}")
                        route.pointList[mapLocation.id] =
                            (MapPoint(mapLocation, sequenceNr, requireContext()))
                    }
                }
                val mapLocationAdapter =
                    MapLocationAdapter(route.pointList.values.toList().sortedBy { it.sequenceNr })

                binding.recyclerViewPoints.layoutManager = LinearLayoutManager(requireContext())

                binding.recyclerViewPoints.adapter = mapLocationAdapter
                val itemTouchHelper = ItemTouchHelper(simpleCallBack)
                itemTouchHelper.attachToRecyclerView(binding.recyclerViewPoints)
            } else {
                Snackbar.make(binding.root, "Route not found", Snackbar.LENGTH_LONG)
                    .setAnchorView(bottomNav)
                    .show()
            }

        }



        binding.backButton.setOnClickListener {
            changeBackFragment()
        }

        binding.chooseRoute.setOnClickListener {
            chooseRoute()
        }

        binding.publishRouteButton.setOnClickListener {
            publishRoute()
        }

        binding.deletePublishedRoute.setOnClickListener {
            deletePublishedRoute()
        }

        binding.routeTitle.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.routeTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.routeTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateRoute()
                binding.routeTitle.clearFocus()
                this.requireContext().hideKeyboard(binding.routeTitle)
                true
            } else false
        }
        binding.routeDescription.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.routeDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.routeDescription.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateRoute()
                binding.routeDescription.clearFocus()
                this.requireContext().hideKeyboard(binding.routeDescription)
                true
            } else false
        }


    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateRoute() {
        val newRouteName = binding.routeTitle.text.toString()
        val newRouteDescription = binding.routeDescription.text.toString()

        if (newRouteName != routeEntity.name || newRouteDescription != routeEntity.description) {
            routeEntity.name = newRouteName
            routeEntity.description = newRouteDescription
            routeViewModel.updateRoute(routeEntity)
        }
    }

    private fun deletePublishedRoute() {

        lifecycleScope.launch {
            try {
                mapLocationsOfRouteEntity.forEach { mapLocation ->

                    routeViewModel.deleteMapLocationById(mapLocation.externalId!!,
                        onSuccess = { Log.d("DeleteMapLocation", "MapLocation deleted: ${mapLocation.externalId}") },
                        onFailure = { error -> Log.e("DeleteMapLocation", error) }
                    )
                    val routeMapLocation = routeViewModel.getRouteMapLocationByMapLocationId(mapLocation.id)
                    routeViewModel.deleteRouteMapLocationByIdApi(routeMapLocation.externalId ?: "",
                        onSuccess = { Log.d("DeleteRouteMapLocation", "RouteMapLocation deleted: ${routeMapLocation.externalId}") },
                        onFailure = { error -> Log.e("DeleteRouteMapLocation", error) }
                    )
                }

                routeViewModel.deleteRouteById(routeEntity.externalId!!,
                    onSuccess = {
                        Snackbar.make(binding.root, "Route deleted successfully!", Snackbar.LENGTH_LONG)
                            .setAnchorView(bottomNav)
                            .show()
                        Log.d("DeleteRoute", "Route deleted: ${routeEntity.externalId!!}")
                    },
                    onFailure = { error -> Log.e("DeleteRoute", error) }
                )

            } catch (e: Exception) {
                Log.e("DeleteCompleteRoute", "Error while deleting route: ${e.message}")
            }
        }

    }

    private fun publishRoute() {
        bottomNav =  requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        if (!isUserLoggedIn()) {
            Snackbar.make(binding.root, "You need to log in to publish a route", Snackbar.LENGTH_LONG)
                .setAnchorView(bottomNav)
                .show()
        }
        if (mapLocationsOfRouteEntity.isEmpty()) {
            Snackbar.make(binding.root, "Route must have at least one point to be published", Snackbar.LENGTH_LONG)
                .setAnchorView(bottomNav)
                .show()
            return
        }
        if(routeEntity.externalId == null){
            routeViewModel.postRoute(routeEntity) { newRoute ->
                routeEntity.externalId = newRoute.routeUid
                binding.publishRouteButton.text = "Update Published Route"
                binding.deletePublishedRoute.visibility = View.VISIBLE
                Log.d("GogoRoute", routeEntity.toString())
                routeViewModel.updateRoute(routeEntity)
                Log.d("GogoRoute", routeEntity.toString())
                Log.d("Warmbier", newRoute.toString())
                val routeImageFile = File(
                    requireContext().filesDir,
                    "${IMAGE_DIR}/${route.id}${IMAGE_EXTENSION}"
                )
                if (routeImageFile.exists()) {
                    Log.d("Warmbier", "oooo wysylam image")
                    val imageRequest =
                        RequestBody.create("image/jpg".toMediaTypeOrNull(), routeImageFile)
                    val imageMultiPartBody =
                        MultipartBody.Part.createFormData(
                            "file",
                            routeImageFile.name,
                            imageRequest
                        )
                    routeViewModel.putImageToRoute(
                        newRoute.routeUid,
                        imageMultiPartBody
                    )
                } else {
                    Log.d("Warmbier", "cos nie znalazlem imagy")
                }

                mapLocationsOfRouteEntity.forEach { mapLocation ->
                    val mapLocationRequest = MapLocationRequest(
                        mapLocation.id,
                        mapLocation.name,
                        mapLocation.description,
                        Coordinates("Point", arrayOf(mapLocation.latitude, mapLocation.longitude))
                    )
                    val sequenceNr =
                        runBlocking { routeViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
                    Log.d("GogoMapLocations", mapLocationIdMap[mapLocation.id].toString())
                    Log.d("GogoMapLocations1", mapLocation.id)

                    routeViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->
                        mapLocationIdMap[mapLocation.id] = newMapLocation.id
                        mapLocation.externalId = newMapLocation.id
                        routeViewModel.updateMapLocation(mapLocation)
                        Log.d("GogoMapLocations", mapLocationIdMap[mapLocation.id].toString())
                        val routeMapLocationRequest =
                            RouteMapLocationRequest(
                                UUID.randomUUID().toString(),
                                newMapLocation,
                                newRoute,
                                sequenceNr
                            )

                        val routeMapLocation =
                            RouteMapLocation(
                                routeEntity.routeUid,
                                mapLocation.id,
                                sequenceNr,
                                null
                            )


                        routeViewModel.postRouteMapLocation(routeMapLocationRequest) { newRouteMapLocation ->


                            //routeMapLocationIdMap[newMapLocation.id] = newRouteMapLocation.id
                            Log.d("GogoNewRouteMapLocation",newRouteMapLocation.id)
                            routeMapLocation.externalId = newRouteMapLocation.id
                            Log.d("GogoNewRouteMapLocation",routeMapLocation.externalId!!)
                            //routeViewModel.updateRouteMapLocation(routeMapLocation)

                            routeViewModel.updateExternalId(routeEntity.routeUid,mapLocation.id,newRouteMapLocation.id)
                            val audio = Audio(
                                UUID.randomUUID().toString(),
                                newMapLocation.name + "audio",
                                null,
                                null,
                                newMapLocation
                            )
                            routeViewModel.postAudio(audio) { newAudio ->
                                audioIdMap[newMapLocation.id] = newAudio.id
                                val audioFile = File(
                                    requireContext().filesDir,
                                    "${Constants.AUDIO_DIR}/${mapLocation.id}${Constants.AUDIO_EXTENSION}"
                                )

                                Log.d("Dzicz", audioFile.absolutePath);
                                if (audioFile.exists()) {
                                    Log.d("AudioRequest", "Exists")
                                    val audioRequest =
                                        RequestBody.create("audio/3gp".toMediaTypeOrNull(), audioFile)
                                    val audioMultiPartBody =
                                        MultipartBody.Part.createFormData(
                                            "file",
                                            audioFile.name,
                                            audioFile.asRequestBody()
                                        )
                                    Log.d("AUDIO ID", newAudio.id)
                                    routeViewModel.postAudioFile(newAudio.id, audioMultiPartBody)
                                } else {
                                    Log.d("AudioRequest", "Nie mo")
                                }

                                val imageFile = File(
                                    requireContext().filesDir,
                                    "${IMAGE_DIR}/${mapLocation.id}${IMAGE_EXTENSION}"
                                )

                                if (imageFile.exists()) {
                                    Log.d("Image", "Exists")
                                    val imageRequest =
                                        RequestBody.create("image/jpg".toMediaTypeOrNull(), imageFile)
                                    val imageMultiPartBody =
                                        MultipartBody.Part.createFormData(
                                            "file",
                                            imageFile.name,
                                            imageRequest
                                        )
                                    routeViewModel.putImageToMapLocation(
                                        newMapLocation.id,
                                        imageMultiPartBody
                                    )
                                } else {
                                    Log.d("Image", "Nie mo")
                                }

                            }

                        }
                    }
                }

                Snackbar.make(binding.root, "Route published successfully!", Snackbar.LENGTH_LONG)
                    .setAnchorView(bottomNav)
                    .show()
            }
        }else {
            lifecycleScope.launch{
                updatePublish()
            }
        }

    }

    private suspend fun updatePublish() {
        routeViewModel.putRouteById(routeEntity.externalId!!, routeEntity)
        Log.d("Gogo", routeEntity.toString())

        val routeImageFile = File(
            requireContext().filesDir,
            "${IMAGE_DIR}/${route.id}${IMAGE_EXTENSION}"
        )
        if (routeImageFile.exists()) {
            Log.d("Warmbier", "Updating route image")
            val imageRequest = RequestBody.create("image/jpg".toMediaTypeOrNull(), routeImageFile)
            val imageMultiPartBody = MultipartBody.Part.createFormData(
                "file",
                routeImageFile.name,
                imageRequest
            )
            routeViewModel.putImageToRoute(routeEntity.externalId!!, imageMultiPartBody)
        } else {
            Log.d("Warmbier", "Route image not found")
        }
        Log.d("GogoMapLocations", mapLocationsOfRouteEntity.toString())
        mapLocationsOfRouteEntity.forEach { mapLocation ->

            val sequenceNr = runBlocking { routeViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
            var routeTest = Route(routeEntity.externalId!!,routeEntity.name,routeEntity.description,null)
            Log.d("GogoMapLocations", mapLocationIdMap[mapLocation.id].toString())
            val mapLocationId = mapLocation.externalId

            if (mapLocationId != null) {
                //zmienione 21:39
                val mapLocationRequest = MapLocationRequest(
                    mapLocationId,
                    mapLocation.name,
                    mapLocation.description,
                    Coordinates("Point", arrayOf(mapLocation.latitude, mapLocation.longitude))
                )
                //TODO tutaj tez chyba mapLocationRequest stworyc ale z externalId jako id
                routeViewModel.putMapLocationById(mapLocationId, mapLocationRequest)

                val routeMapLocationId = routeViewModel.getRouteMapLocationByMapLocationId(mapLocation.id)


                    //TODO tutaj sprawdzic czy napewno tak bo te mapLocationRequest i routeEntity id moga sie nei zgadzac
                    val routeMapLocation = RouteMapLocationRequest(
                        routeMapLocationId.externalId!!,
                        mapLocationRequest,
                        routeTest,
                        sequenceNr
                    )
                    //tutaj zmiana na external zamiast .id chyba
                    routeViewModel.putRouteMapLocationById(routeMapLocationId.externalId!!, routeMapLocation)
                    Log.d("RouteMapLocation", "Updated RouteMapLocation ID: ${routeMapLocationId.id}")


                    routeViewModel.getAudioByMapLocationId(mapLocationId)

                    routeViewModel.audioResponse.observe(viewLifecycleOwner) { audioResponse ->
                        audioResponse?.body()?.content?.let { audios ->
                            for (audio in audios) {
                                Log.d("GogoAudio", audio.toString())
                                val audioFile = File(
                                    requireContext().filesDir,
                                    "${Constants.AUDIO_DIR}/${mapLocation.id}${Constants.AUDIO_EXTENSION}"
                                )

                                Log.d("Dzicz", audioFile.absolutePath);
                                if (audioFile.exists()) {
                                    Log.d("AudioRequest", "Exists")
                                    val audioRequest =
                                        RequestBody.create("audio/3gp".toMediaTypeOrNull(), audioFile)
                                    val audioMultiPartBody =
                                        MultipartBody.Part.createFormData(
                                            "file",
                                            audioFile.name,
                                            audioFile.asRequestBody()
                                        )
                                    routeViewModel.postAudioFile(audio.id, audioMultiPartBody)
                                    routeViewModel.putAudioById(audio.id,audio)
                                } else {
                                    Log.d("AudioRequest", "Nie mo")
                                }
                            }
                        }
                    }

//                    routeViewModel.audioFile.observe(viewLifecycleOwner) { response ->
//                        if (response.bytes.isSuccessful) {
//                            val audioBytes = response.bytes.body()
//                            if (audioBytes != null) {
//                                val tempAudioFile = File.createTempFile("temp_audio", ".3gp", requireContext().cacheDir)
//                                val fos = FileOutputStream(tempAudioFile)
//                                fos.write(audioBytes)
//                                route.pointList[response.mapLocationId]?.audioPath = tempAudioFile.absolutePath
//                                fos.close()
//                            }
//                        }
//                    }





            }else {
                //dodane 21:39
                val mapLocationRequest = MapLocationRequest(
                    mapLocation.id,
                    mapLocation.name,
                    mapLocation.description,
                    Coordinates("Point", arrayOf(mapLocation.latitude, mapLocation.longitude))
                )
                routeViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->
                    mapLocationIdMap[mapLocation.id] = newMapLocation.id
                    mapLocation.externalId = newMapLocation.id
                    routeViewModel.updateMapLocation(mapLocation)
                    //tutaj zmienic newRoute czyli routeTest // 21:45 zmiana z routeEntity na routeTest
                    val newRouteMapLocation = RouteMapLocationRequest(
                        UUID.randomUUID().toString(),
                        newMapLocation,
                        routeTest,
                        sequenceNr
                    )
                    Log.d("GogoExternalId",routeEntity.externalId!!)
                    //tutaj zmienic
                    val routeMapLocation =
                        RouteMapLocation(
                            routeEntity.routeUid,
                            mapLocation.id,
                            sequenceNr,
                            null
                        )
                    routeViewModel.postRouteMapLocation(newRouteMapLocation) { createdRouteMapLocation ->
                        routeMapLocationIdMap[mapLocation.id] = createdRouteMapLocation.id
                        routeMapLocation.externalId = createdRouteMapLocation.id
                        routeViewModel.updateExternalId(routeEntity.routeUid, mapLocation.id, createdRouteMapLocation.id)
                        Log.d("RouteMapLocation", "Created new RouteMapLocation ID: ${createdRouteMapLocation.id}")

                        val audio = Audio(
                            UUID.randomUUID().toString(),
                            newMapLocation.name + "audio",
                            null,
                            null,
                            newMapLocation
                        )
                        routeViewModel.postAudio(audio) { newAudio ->
                            audioIdMap[newMapLocation.id] = newAudio.id
                            val audioFile = File(
                                requireContext().filesDir,
                                "${Constants.AUDIO_DIR}/${mapLocation.id}${Constants.AUDIO_EXTENSION}"
                            )

                            Log.d("Dzicz", audioFile.absolutePath);
                            if (audioFile.exists()) {
                                Log.d("AudioRequest", "Exists")
                                val audioRequest =
                                    RequestBody.create("audio/3gp".toMediaTypeOrNull(), audioFile)
                                val audioMultiPartBody =
                                    MultipartBody.Part.createFormData(
                                        "file",
                                        audioFile.name,
                                        audioFile.asRequestBody()
                                    )
                                Log.d("AUDIO ID", newAudio.id)
                                routeViewModel.postAudioFile(newAudio.id, audioMultiPartBody)
                            } else {
                                Log.d("AudioRequest", "Nie mo")
                            }
                            //To chyba nie potrzebne
//                            val imageFile = File(
//                                requireContext().filesDir,
//                                "${IMAGE_DIR}/${mapLocation.id}${IMAGE_EXTENSION}"
//                            )
//
//                            if (imageFile.exists()) {
//                                Log.d("Image", "Exists")
//                                val imageRequest =
//                                    RequestBody.create("image/jpg".toMediaTypeOrNull(), imageFile)
//                                val imageMultiPartBody =
//                                    MultipartBody.Part.createFormData(
//                                        "file",
//                                        imageFile.name,
//                                        imageRequest
//                                    )
//                                routeViewModel.putImageToMapLocation(
//                                    newMapLocation.id,
//                                    imageMultiPartBody
//                                )
//                            } else {
//                                Log.d("Image", "Nie mo")
//                            }

                        }
                    }
                }
            }

            val audioFile = File(
                requireContext().filesDir,
                "${Constants.AUDIO_DIR}/${mapLocation.id}${Constants.AUDIO_EXTENSION}"
            )
            if (audioFile.exists()) {
                val audioRequest = RequestBody.create("audio/3gp".toMediaTypeOrNull(), audioFile)
                val audioMultiPartBody = MultipartBody.Part.createFormData(
                    "file",
                    audioFile.name,
                    audioRequest
                )
                routeViewModel.postAudioFile(UUID.randomUUID().toString(), audioMultiPartBody)

            }

            val imageFile = File(
                requireContext().filesDir,
                "${IMAGE_DIR}/${mapLocation.id}${IMAGE_EXTENSION}"
            )
            if (imageFile.exists()) {
                val imageRequest = RequestBody.create("image/jpg".toMediaTypeOrNull(), imageFile)
                val imageMultiPartBody = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.name,
                    imageRequest
                )
                routeViewModel.putImageToMapLocation(mapLocationId!!, imageMultiPartBody)
            }
        }

        Snackbar.make(binding.root, "Route updated successfully!", Snackbar.LENGTH_LONG).setAnchorView(bottomNav).show()



    }




    private var simpleCallBack =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sortedPointList = route.pointList.values.toList().sortedBy { it.sequenceNr }
                val startPosition = viewHolder.bindingAdapterPosition
                val stopPosition = target.bindingAdapterPosition
                sortedPointList[startPosition].sequenceNr = stopPosition + 1
                sortedPointList[stopPosition].sequenceNr = startPosition + 1

                routeViewModel.updateRouteMapLocationSequenceNrById(
                    sortedPointList[startPosition].id,
                    stopPosition + 1
                )
                routeViewModel.updateRouteMapLocationSequenceNrById(
                    sortedPointList[stopPosition].id,
                    startPosition + 1
                )
                (recyclerView.adapter as? MapLocationAdapter)?.swapCollection(startPosition, stopPosition)

                Collections.swap(mapLocationsOfRouteEntity, startPosition, stopPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, stopPosition)
                return true;
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                recyclerView.adapter?.notifyDataSetChanged()
                viewHolder.itemView.alpha = 1.0f
            }

        }

    private fun isUserLoggedIn(): Boolean {
        val tokenManager = TokenManager(requireContext())
        val token = tokenManager.getToken()
        return !token.isNullOrEmpty() && !tokenManager.isTokenExpired(token)

    }


    private fun chooseRoute() {
        val mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        Log.d("Warmbier", route.toString())
        mapViewModel.route = route
        mapViewModel.inCreationMode = true
        val bundle = Bundle().apply {
            putBoolean("reset", true)
        }
        changeFragmentListener?.changeFragment(1, bundle)
    }

    private fun changeBackFragment() {
        (parentFragment as? RoutesFragment)?.replaceFragment(1, MyRoutesFragment())
    }


}