package loch.golden.waytogo.fragments.route.views.local

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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentDatabaseMyRouteDetailBinding
import loch.golden.waytogo.fragments.route.components.adapters.MapLocationAdapter
import loch.golden.waytogo.fragments.route.views.RoutesFragment
import loch.golden.waytogo.fragments.user.components.TokenManager
import loch.golden.waytogo.room.entity.maplocation.MapLocation
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.room.entity.routemaplocation.RouteMapLocation
import loch.golden.waytogo.services.dto.audio.AudioDTO
import loch.golden.waytogo.services.dto.maplocation.Coordinates
import loch.golden.waytogo.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.services.dto.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.utils.Constants
import loch.golden.waytogo.utils.Constants.Companion.IMAGE_DIR
import loch.golden.waytogo.utils.Constants.Companion.IMAGE_EXTENSION
import loch.golden.waytogo.utils.OnChangeFragmentListener
import loch.golden.waytogo.viewmodels.MapViewModel
import loch.golden.waytogo.viewmodels.BackendViewModel
import loch.golden.waytogo.viewmodels.LocalViewModel
import loch.golden.waytogo.viewmodels.classes.MapPoint
import loch.golden.waytogo.viewmodels.classes.MapRoute
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import java.util.UUID

@AndroidEntryPoint
class DatabaseMyRouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentDatabaseMyRouteDetailBinding
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val backendViewModel: BackendViewModel by viewModels()
    private val localViewModel: LocalViewModel by viewModels()
    private val mapViewModel: MapViewModel by activityViewModels()
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
            val route = Route(UUID.randomUUID().toString(), "My Route", "", null)
            localViewModel.insert(route)
            route.routeUid
        }



        localViewModel.getRouteWithMapLocations(routeId)
        localViewModel.routeWithLocationsFromDb.observe(viewLifecycleOwner) { routeWithLocationsFromDb ->
            if (routeWithLocationsFromDb != null) {
                routeEntity = routeWithLocationsFromDb.route

                updateButtons()

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
                            runBlocking { localViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
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

    private fun updateButtons() {
        if (routeEntity.externalId != null) {
            binding.publishRouteButton.text = "Update Published Route"
            binding.deletePublishedRoute.visibility = View.VISIBLE

        } else {
            binding.publishRouteButton.text = "Publish Route"
            binding.deletePublishedRoute.visibility = View.GONE

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
            localViewModel.updateRoute(routeEntity)
        }
    }

    private fun deletePublishedRoute() {
        lifecycleScope.launch {
            try {
                mapLocationsOfRouteEntity.forEach { mapLocation ->
                    backendViewModel.deleteMapLocationById(
                        mapLocation.externalId ?: "",
                        onSuccess = {
                            Log.d("DeleteMapLocation", "MapLocation deleted: ${mapLocation.externalId}")
                        },
                        onFailure = { error -> Log.e("DeleteMapLocation", error) }
                    )

                    val routeMapLocation = localViewModel.getRouteMapLocationByMapLocationId(mapLocation.id)
                    backendViewModel.deleteRouteMapLocationByIdApi(
                        routeMapLocation.externalId ?: "",
                        onSuccess = {
                            Log.d("DeleteRouteMapLocation", "RouteMapLocation deleted: ${routeMapLocation.externalId}")
                        },
                        onFailure = { error -> Log.e("DeleteRouteMapLocation", error) }
                    )
                }

                val result = backendViewModel.deleteRouteById(routeEntity.externalId!!)
                if (result.isSuccess) {
                    Log.d("DeleteRoute", "Route deleted: ${routeEntity.externalId}")

                    routeEntity.externalId = null
                    localViewModel.updateRouteExternalId(routeEntity.routeUid, routeEntity.externalId)

                    updateButtons()

                    Snackbar.make(binding.root, "Route deleted successfully!", Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .show()
                } else {
                    Log.e("DeleteRoute", "Failed to delete route: ${result.exceptionOrNull()?.message}")
                    Snackbar.make(binding.root, "Failed to delete route!", Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("DeleteCompleteRoute", "Error while deleting route: ${e.message}")
            }
        }


    }


    private fun publishRoute() {
        bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
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
        if (routeEntity.externalId == null) {
            backendViewModel.postRoute(routeEntity) { newRoute ->
                routeEntity.externalId = newRoute.routeUid
                binding.publishRouteButton.text = "Update Published Route"
                binding.deletePublishedRoute.visibility = View.VISIBLE
                Log.d("GogoRoute", routeEntity.toString())
                localViewModel.updateRoute(routeEntity)
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
                    backendViewModel.putImageToRoute(
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
                        runBlocking { localViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
                    Log.d("GogoMapLocations", mapLocationIdMap[mapLocation.id].toString())
                    Log.d("GogoMapLocations1", mapLocation.id)

                    backendViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->
                        mapLocationIdMap[mapLocation.id] = newMapLocation.id
                        mapLocation.externalId = newMapLocation.id
                        localViewModel.updateMapLocation(mapLocation)
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


                        backendViewModel.postRouteMapLocation(routeMapLocationRequest) { newRouteMapLocation ->


                            //routeMapLocationIdMap[newMapLocation.id] = newRouteMapLocation.id
                            Log.d("GogoNewRouteMapLocation", newRouteMapLocation.id)
                            routeMapLocation.externalId = newRouteMapLocation.id
                            Log.d("GogoNewRouteMapLocation", routeMapLocation.externalId!!)
                            //routeViewModel.updateRouteMapLocation(routeMapLocation)

                            localViewModel.updateExternalId(
                                routeEntity.routeUid,
                                mapLocation.id,
                                newRouteMapLocation.id
                            )
                            val audioDTO = AudioDTO(
                                UUID.randomUUID().toString(),
                                newMapLocation.name + "audio",
                                null,
                                null,
                                newMapLocation
                            )
                            backendViewModel.postAudio(audioDTO) { newAudio ->
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
                                    backendViewModel.postAudioFile(newAudio.id, audioMultiPartBody)
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
                                    backendViewModel.putImageToMapLocation(
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
        } else {
            lifecycleScope.launch {
                updatePublish()
            }
        }

    }

    private suspend fun updatePublish() {
        backendViewModel.putRouteById(routeEntity.externalId!!, routeEntity)
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
            backendViewModel.putImageToRoute(routeEntity.externalId!!, imageMultiPartBody)
        } else {
            Log.d("Warmbier", "Route image not found")
        }
        Log.d("GogoMapLocations", mapLocationsOfRouteEntity.toString())
        mapLocationsOfRouteEntity.forEach { mapLocation ->

            val sequenceNr = runBlocking { localViewModel.getSequenceNrByMapLocationId(mapLocation.id) }
            var routeTest = Route(routeEntity.externalId!!, routeEntity.name, routeEntity.description, null)
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
                backendViewModel.putMapLocationById(mapLocationId, mapLocationRequest)

                val routeMapLocationId = localViewModel.getRouteMapLocationByMapLocationId(mapLocation.id)


                //TODO tutaj sprawdzic czy napewno tak bo te mapLocationRequest i routeEntity id moga sie nei zgadzac
                val routeMapLocation = RouteMapLocationRequest(
                    routeMapLocationId.externalId!!,
                    mapLocationRequest,
                    routeTest,
                    sequenceNr
                )
                //tutaj zmiana na external zamiast .id chyba
                backendViewModel.putRouteMapLocationById(routeMapLocationId.externalId!!, routeMapLocation)
                Log.d("RouteMapLocation", "Updated RouteMapLocation ID: ${routeMapLocationId.id}")


                backendViewModel.getAudioByMapLocationId(mapLocationId)
                postNewAudio(mapLocationRequest, mapLocation)
            } else {
                //dodane 21:39
                val mapLocationRequest = MapLocationRequest(
                    mapLocation.id,
                    mapLocation.name,
                    mapLocation.description,
                    Coordinates("Point", arrayOf(mapLocation.latitude, mapLocation.longitude))
                )
                backendViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->
                    mapLocationIdMap[mapLocation.id] = newMapLocation.id
                    mapLocation.externalId = newMapLocation.id
                    localViewModel.updateMapLocation(mapLocation)
                    //tutaj zmienic newRoute czyli routeTest // 21:45 zmiana z routeEntity na routeTest
                    val newRouteMapLocation = RouteMapLocationRequest(
                        UUID.randomUUID().toString(),
                        newMapLocation,
                        routeTest,
                        sequenceNr
                    )
                    Log.d("GogoExternalId", routeEntity.externalId!!)
                    //tutaj zmienic
                    val routeMapLocation =
                        RouteMapLocation(
                            routeEntity.routeUid,
                            mapLocation.id,
                            sequenceNr,
                            null
                        )
                    backendViewModel.postRouteMapLocation(newRouteMapLocation) { createdRouteMapLocation ->
                        routeMapLocationIdMap[mapLocation.id] = createdRouteMapLocation.id
                        routeMapLocation.externalId = createdRouteMapLocation.id
                        localViewModel.updateExternalId(
                            routeEntity.routeUid,
                            mapLocation.id,
                            createdRouteMapLocation.id
                        )
                        Log.d("RouteMapLocation", "Created new RouteMapLocation ID: ${createdRouteMapLocation.id}")

                        postNewAudio(newMapLocation, mapLocation)
                    }
                }
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
                backendViewModel.putImageToMapLocation(mapLocationId!!, imageMultiPartBody)
            }
        }
        backendViewModel.audioResponse.observe(viewLifecycleOwner) { audioResponse ->
            audioResponse?.body()?.content?.let { audios ->
                for (audio in audios) {
                    Log.d("GogiUsuwa", audio.toString())
                    backendViewModel.deleteAudioById(
                        audio.id,
                        onSuccess = { Log.d("GogiUsuwa", "gogi usunal") },
                        onFailure = { Log.d("GogiUsuwa", "gogi nie usunal") },
                    )
                }

            }
        }
        Snackbar.make(binding.root, "Route updated successfully!", Snackbar.LENGTH_LONG).setAnchorView(bottomNav).show()


    }

    private fun postNewAudio(
        newMapLocation: MapLocationRequest,
        mapLocation: MapLocation
    ) {
        val audioDTO = AudioDTO(
            UUID.randomUUID().toString(),
            newMapLocation.name + "audio",
            null,
            null,
            newMapLocation
        )
        backendViewModel.postAudio(audioDTO) { newAudio ->
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
                backendViewModel.postAudioFile(newAudio.id, audioMultiPartBody)
            } else {
                Log.d("AudioRequest", "Nie mo")
            }
        }
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

                localViewModel.updateRouteMapLocationSequenceNrById(
                    sortedPointList[startPosition].id,
                    stopPosition + 1
                )
                localViewModel.updateRouteMapLocationSequenceNrById(
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
        Log.d("Warmbier", route.toString())
        mapViewModel.route = route
        mapViewModel.inCreationMode = true
        changeFragmentListener?.changeFragment(1)
    }

    private fun changeBackFragment() {
        (parentFragment as? RoutesFragment)?.replaceFragment(1, MyRoutesFragment())
    }


}