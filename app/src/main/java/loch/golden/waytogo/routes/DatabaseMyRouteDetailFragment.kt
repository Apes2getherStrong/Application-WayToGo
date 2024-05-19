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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentDatabaseMyRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnNavigateToMapListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory


class DatabaseMyRouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentDatabaseMyRouteDetailBinding
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private var navigateToMapListener: OnNavigateToMapListener? = null
    private lateinit var route: MapRoute
    private lateinit var routeEntity: Route

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

        //pobranie id kliknietego argumentu, zobacz publicRoutesFragment bundle
        // dzieki gogi za notatke
        val routeId = arguments?.getString("id") ?: "" //TODO add error message

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
            Toast.makeText(requireContext(), "Udalo sie zupdatowac routa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun publishRoute() {
        routeViewModel.postRoute(routeEntity)
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