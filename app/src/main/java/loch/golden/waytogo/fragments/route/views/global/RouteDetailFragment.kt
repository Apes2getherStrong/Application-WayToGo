package loch.golden.waytogo.fragments.route.views.global

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import loch.golden.waytogo.databinding.FragmentRouteDetailBinding
import loch.golden.waytogo.fragments.route.components.adapters.PublicMapLocationAdapter
import loch.golden.waytogo.fragments.route.views.RoutesFragment
import loch.golden.waytogo.utils.OnChangeFragmentListener
import loch.golden.waytogo.viewmodels.MapViewModel
import loch.golden.waytogo.viewmodels.BackendViewModel
import loch.golden.waytogo.viewmodels.classes.MapPoint
import loch.golden.waytogo.viewmodels.classes.MapRoute
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class RouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentRouteDetailBinding
    private val backendViewModel: BackendViewModel by viewModels()
    private val mapViewModel: MapViewModel by activityViewModels()
    private var changeFragmentListener: OnChangeFragmentListener? = null
    private lateinit var route: MapRoute

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
        backendViewModel.getRouteById(routeId)

        // Observe route response
        backendViewModel.myRouteResponse.observe(viewLifecycleOwner) { response ->
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
                backendViewModel.getMapLocationsByRouteId(routeId)
            }
            backendViewModel.getRouteImage(routeId)
            backendViewModel.currentRouteImage.observe(viewLifecycleOwner) { response ->
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
        backendViewModel.myMapLocationsResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.content?.let { mapLocations ->
                    for (mapLocation in mapLocations) {
                        val mapPoint = MapPoint(mapLocation, ++sequenceNr)
                        route.pointList[mapLocation.id] = mapPoint
                        backendViewModel.getAudioByMapLocationId(mapLocation.id)
                        backendViewModel.getMapLocationImage(mapLocation.id)
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
        backendViewModel.audioResponse.observe(viewLifecycleOwner) { audioResponse ->
            audioResponse?.body()?.content?.let { audios ->
                for (audio in audios) {
                    backendViewModel.getAudioFile(audio.id, audio.mapLocationRequest.id)
                }
            }
        }
        //TODO move these componenets to seperate functions

        backendViewModel.audioFile.observe(viewLifecycleOwner) { response ->
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

        backendViewModel.currentMapImage.observe(viewLifecycleOwner) { response ->
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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun chooseRoute() {
        Log.d("Warmbier", route.toString())
        mapViewModel.inCreationMode = false
        mapViewModel.route = route
        Log.d("DaggerWarmbier", mapViewModel.route.toString())
        mapViewModel.updateCurrentSequenceNr(1)
        val bundle = Bundle().apply {
            putBoolean("reset", true)
        }
        changeFragmentListener?.changeFragment(1, bundle)
    }

    private fun changeBackFragment() {


        (parentFragment as? RoutesFragment)?.replaceFragment(0, PublicRoutesFragment())

    }


}