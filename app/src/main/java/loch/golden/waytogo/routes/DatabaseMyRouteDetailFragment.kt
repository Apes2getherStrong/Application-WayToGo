package loch.golden.waytogo.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import loch.golden.waytogo.audio.Audio
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentDatabaseMyRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnNavigateToMapListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.model.maplocation.Coordinates
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.routes.utils.Constants
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


class DatabaseMyRouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentDatabaseMyRouteDetailBinding
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private var navigateToMapListener: OnNavigateToMapListener? = null
    private lateinit var route: MapRoute
    private lateinit var routeEntity: Route
    private lateinit var mapLocationsOfRouteEntity: List<MapLocation>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatabaseMyRouteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigateToMapListener) {
            navigateToMapListener = context
        } else {
            throw RuntimeException("$context must implement OnNavigateToMapListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                route = MapRoute(
                    routeWithLocationsFromDb.route.routeUid,
                    routeWithLocationsFromDb.route.name,
                    routeWithLocationsFromDb.route.description,
                    mutableMapOf()
                )
                binding.routeTitle.setText(routeWithLocationsFromDb.route.name)
                binding.routeDescription.setText(routeWithLocationsFromDb.route.description)
                val mapLocationAdapter = MapLocationAdapter(routeWithLocationsFromDb.mapLocations)
                Log.d("Map Location", routeWithLocationsFromDb.mapLocations.toString())
                mapLocationsOfRouteEntity = routeWithLocationsFromDb.mapLocations
                routeWithLocationsFromDb.mapLocations.let {
                    Log.d("Warmbier", it.toString())
                    for (mapLocation in it) {
                        route.pointList[mapLocation.id] = (MapPoint(mapLocation, requireContext()))
                    }
                }
                binding.recyclerViewPoints.layoutManager = LinearLayoutManager(requireContext())

                binding.recyclerViewPoints.adapter = mapLocationAdapter
            } else {
                Toast.makeText(requireContext(), "Route not found", Toast.LENGTH_SHORT).show()
            }

        }

        binding.routeTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateRoute()
        }
        binding.routeDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateRoute()
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
    }

    private fun updateRoute() {
        val newRouteName = binding.routeTitle.text.toString()
        val newRouteDescription = binding.routeDescription.text.toString()

        if (newRouteName != routeEntity.name || newRouteDescription != routeEntity.description) {
            routeEntity.name = newRouteName
            routeEntity.description = newRouteDescription
            lifecycleScope.launch {
                routeViewModel.updateRoute(routeEntity)
            }
            Toast.makeText(requireContext(), "Udalo sie zupdatowac routa", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //TODO nie dziala publishowania mapLocation jeszcze , fix
    private fun publishRoute() {
        var sequenceNumber = 1;
        routeViewModel.postRoute(routeEntity) { newRoute ->

            mapLocationsOfRouteEntity.forEach { mapLocation ->
                val mapLocationRequest = MapLocationRequest(
                    mapLocation.id,
                    mapLocation.name,
                    mapLocation.description,
                    Coordinates("Point", arrayOf(mapLocation.latitude, mapLocation.longitude))
                )

                routeViewModel.postMapLocation(mapLocationRequest) { newMapLocation ->

                    Log.d("Dzicz","Siema jestem se tu");
                    val routeMapLocation =
                        RouteMapLocationRequest(newMapLocation, newRoute, sequenceNumber)
                    routeViewModel.postRouteMapLocation(routeMapLocation)

                    sequenceNumber++

                    val audio = Audio(
                        UUID.randomUUID().toString(),
                        newMapLocation.name + "audio",
                        null,
                        null,
                        newMapLocation
                    )
                    routeViewModel.postAudio(audio) { newAudio ->
                        val audioFile = File(
                            requireContext().filesDir,
                            "${Constants.AUDIO_DIR}/${mapLocation.id}${Constants.AUDIO_EXTENSION}")

                        Log.d("Dzicz",audioFile.absolutePath);
                        if(audioFile.exists()){
                            Log.d("Dzicz","Exists")
                            val audioRequest = RequestBody.create("audio/3gp".toMediaTypeOrNull(),audioFile )
                            val audioMultiPartBody = MultipartBody.Part.createFormData("file", audioFile.name, audioFile.asRequestBody())
                            Log.d("AUDIO ID", newAudio.id)
                            routeViewModel.postAudioFile(newAudio.id, audioMultiPartBody)
                        }else {
                            Log.d("Dzicz","Nie mo")
                        }

                        val imageFile = File(
                            requireContext().filesDir,
                            "${Constants.IMAGE_DIR}/${mapLocation.id}${Constants.IMAGE_EXTENSION}")

                        if(imageFile.exists()){
                            Log.d("Image","Exists")
                            val imageRequest = RequestBody.create("image/jpg".toMediaTypeOrNull(),imageFile)
                            val imageMultiPartBody = MultipartBody.Part.createFormData("file", imageFile.name, imageRequest)
                            routeViewModel.putImageToMapLocation(newMapLocation.id,imageMultiPartBody)
                        }else {
                            Log.d("Image","Nie mo")
                        }

                    }
                }
            }
        }
    }

    private fun chooseRoute() {
        val mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        Log.d("Warmbier", route.toString())
        mapViewModel.route = route
        mapViewModel.inCreationMode = true
        navigateToMapListener?.navigateToMap()
    }

    private fun changeBackFragment() {

        (parentFragment as? RoutesFragment)?.replaceFragment(1, MyRoutesFragment())

    }


}