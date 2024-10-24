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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
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
    private var isPublished = false
    private val mapLocationIdMap = mutableMapOf<String, String>()
    private val audioIdMap = mutableMapOf<String, String>()
    private val routeMapLocationIdMap = mutableMapOf<String, String>()

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
            val outputFile = File(requireContext().filesDir, "$IMAGE_DIR/${route.id}$IMAGE_EXTENSION")
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
            val route = Route(UUID.randomUUID().toString(), "My Route", "")
            routeViewModel.insert(route)
            route.routeUid
        }



        routeViewModel.getRouteWithMapLocations(routeId)
        routeViewModel.routeWithLocationsFromDb.observe(viewLifecycleOwner) { routeWithLocationsFromDb ->
            if (routeWithLocationsFromDb != null) {
                routeEntity = routeWithLocationsFromDb.route

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
                Toast.makeText(requireContext(), "Route not found", Toast.LENGTH_SHORT).show()
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
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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

    private fun publishRoute() {
        if (!isUserLoggedIn()) {
            Toast.makeText(
                requireContext(),
                "You need to log in to publish a route",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (mapLocationsOfRouteEntity.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Route must have at least one point to be published",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        routeViewModel.postRoute(routeEntity) { newRoute ->
            isPublished = true
            binding.publishRouteButton.text = "Update Route"
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

                routeViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->
                    mapLocationIdMap[mapLocation.id] = newMapLocation.id
                    val routeMapLocation =
                        RouteMapLocationRequest(
                            UUID.randomUUID().toString(),
                            newMapLocation,
                            newRoute,
                            sequenceNr
                        )
                    routeViewModel.postRouteMapLocation(routeMapLocation) { newRouteMapLocation ->
                        routeMapLocationIdMap[newMapLocation.id] = newRouteMapLocation.id

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
                .show()
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

                routeViewModel.updateRouteMapLocationSequenceNrById(
                    sortedPointList[startPosition].id,
                    stopPosition + 1
                )
                routeViewModel.updateRouteMapLocationSequenceNrById(
                    sortedPointList[stopPosition].id,
                    startPosition + 1
                )

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