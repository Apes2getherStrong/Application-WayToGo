package loch.golden.waytogo.routes

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnChangeFragmentListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.adapter.PublicMapLocationAdapter
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import java.io.File
import java.io.FileOutputStream


class RouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentRouteDetailBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private var changeFragmentListener: OnChangeFragmentListener? = null
    private lateinit var route: MapRoute
    private lateinit var mapViewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteDetailBinding.inflate(inflater, container, false)
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
        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        //handle back press
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    changeBackFragment()
                }
            })


        val routeId = arguments?.getString("id") ?: ""


        // Fetch route by ID
        routeViewModel.getRouteById(routeId)

        // Observe route response
        routeViewModel.myRouteResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                route = MapRoute(
                    response.body()!!.routeUid,
                    response.body()!!.name,
                    response.body()!!.description,
                    mutableMapOf()
                )
                binding.routeTitle.text = response.body()?.name
                binding.routeDescription.text = response.body()?.description


                // Fetch map locations by route ID
                routeViewModel.getMapLocationsByRouteId(routeId)
            }
            routeViewModel.getRouteImage(routeId)
            routeViewModel.currentRouteImage.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    Log.d("Warmbier", "Rut: is Succesful")
                    val imageBytes = response.body()
                    if (imageBytes != null) {
                        Log.d("Warmbier", "Rut: bytes not null")
                        val tempImageFile = File.createTempFile("temp_img", ".jpg", requireContext().cacheDir)
                        tempImageFile.deleteOnExit()
                        val fos = FileOutputStream(tempImageFile)
                        fos.write(imageBytes)
                        fos.close()
                        route.photoPath = tempImageFile.absolutePath
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        // Set the Bitmap to the ImageView
                        binding.routeImage.setImageBitmap(bitmap)
                    } else
                        Log.d("Warmbier", "Rut: bytes are null")
                } else
                    Log.d("Warmbier", "Rut:is not succesful")

            }
        }
        binding.progressBar.visibility = View.VISIBLE
        // Observe map locations response

        var sequenceNr = 0 //TODO this works but maybe not all the time should make seperate fetch for sequence nr
        routeViewModel.myMapLocationsResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.content?.let { mapLocations ->
                    for (mapLocation in mapLocations) {
                        val mapPoint = MapPoint(mapLocation, ++sequenceNr)
                        route.pointList[mapLocation.id] = mapPoint
                        routeViewModel.getAudioByMapLocationId(mapLocation.id)
                        routeViewModel.getMapLocationImage(mapLocation.id)
                    }
                }
                val mapLocationAdapter =
                    PublicMapLocationAdapter(route.pointList.values.sortedBy { it.sequenceNr }.toMutableList())
                binding.progressBar.visibility = View.GONE
                binding.recyclerViewPoints.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerViewPoints.adapter = mapLocationAdapter
            }
        }

        // Observe audio response
        routeViewModel.audioResponse.observe(viewLifecycleOwner) { audioResponse ->
            audioResponse?.body()?.content?.let { audios ->
                for (audio in audios) {
                    routeViewModel.getAudioFile(audio.id, audio.mapLocationRequest.id)
                }
            }
        }
        //TODO move these componenets to seperate functions

        routeViewModel.audioFile.observe(viewLifecycleOwner) { response ->
            if (response.bytes.isSuccessful) {
                val audioBytes = response.bytes.body()
                if (audioBytes != null) {
                    val tempAudioFile = File.createTempFile("temp_audio", ".3gp", requireContext().cacheDir)
                    val fos = FileOutputStream(tempAudioFile)
                    fos.write(audioBytes)
                    route.pointList[response.mapLocationId]?.audioPath = tempAudioFile.absolutePath
                    fos.close()
                }
            }
        }

        routeViewModel.currentMapImage.observe(viewLifecycleOwner) { response ->
            if (response.bytes.isSuccessful) {
                val imageBytes = response.bytes.body()
                if (imageBytes != null) {
                    val tempImageFile = File.createTempFile("temp_img", ".jpg", requireContext().cacheDir)
                    tempImageFile.deleteOnExit()
                    val fos = FileOutputStream(tempImageFile)
                    fos.write(imageBytes)
                    fos.close()
                    route.pointList[response.mapLocationId]?.photoPath = tempImageFile.absolutePath
                    (binding.recyclerViewPoints.adapter as? PublicMapLocationAdapter)?.updateMapPoint(route.pointList[response.mapLocationId]!!)
                }
            }
        }


        binding.backButton.setOnClickListener()
        {
            changeBackFragment()
        }

        binding.chooseRoute.setOnClickListener()
        {
            chooseRoute()
        }

    }

    private fun clearCache() {
        val cacheDir = requireContext().cacheDir
        cacheDir.deleteRecursively()
    }

    override fun onDestroy() {
        if (mapViewModel.route == null) {
            clearCache()
            Log.d("Warmbier", "Clearing cache")
        }
        super.onDestroy()
    }

    private fun chooseRoute() {
        Log.d("Warmbier", route.toString())
        mapViewModel.inCreationMode = false
        mapViewModel.route = route
        mapViewModel.updateCurrentSequenceNr(1)
        changeFragmentListener?.changeFragment(1)

    }

    private fun changeBackFragment() {


        (parentFragment as? RoutesFragment)?.replaceFragment(0, PublicRoutesFragment())

    }


}