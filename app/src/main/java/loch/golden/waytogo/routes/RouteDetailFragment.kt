package loch.golden.waytogo.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import loch.golden.waytogo.MainActivity
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnNavigateToMapListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.repository.RouteRepository
import loch.golden.waytogo.routes.room.WayToGoDatabase
import loch.golden.waytogo.routes.room.dao.RouteDao
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory


class RouteDetailFragment(private val origin: String) : Fragment() {

    private lateinit var binding: FragmentRouteDetailBinding
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val appScope = CoroutineScope(SupervisorJob())
    private val routeDao: RouteDao by lazy {
        WayToGoDatabase.getDatabase(requireContext(), appScope).getRouteDao()
    }
    private var navigateToMapListener : OnNavigateToMapListener? = null
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
        val repository = RouteRepository(routeDao)
        val viewModelFactory = RouteViewModelFactory(repository)
        routeViewModel = ViewModelProvider(this, viewModelFactory)[RouteViewModel::class.java]
        val routeId = arguments?.getString("id") ?: "" //TODO add error message
        if (origin == "myRoutes") {
            routeViewModel.getRouteFromDbById(routeId)
            routeViewModel.routeWithLocations.observe(viewLifecycleOwner) { routeWithLocations ->
                if (routeWithLocations.isNotEmpty()) {
                    val route = routeWithLocations[0].route
                    binding.routeTitle.text = route.name
                    binding.routeDescription.text = route.description
                } else {
                    Toast.makeText(requireContext(), "Route not found", Toast.LENGTH_SHORT).show()
                }
            }
        }else if (origin == "publicRoutes"){

            routeViewModel.getRouteById(routeId)
            routeViewModel.myRouteResponse.observe(viewLifecycleOwner, Observer { response ->
                if (response.isSuccessful) {
                    Log.d("Warmbier", response.body().toString())
                    route = MapRoute(
                        response.body()!!.routeUid,
                        response.body()!!.name,
                        response.body()!!.description,
                        mutableMapOf()
                    )
                    binding.routeTitle.text = response.body()?.name;
                    binding.routeDescription.text = response.body()?.description;
                    Log.d("Response id", response.body()!!.routeUid)
                    Log.d("Response title", response.body()!!.name)

                } else {
                    Log.d("Response", response.errorBody().toString())

                }


            })

            routeViewModel.getMapLocationsByRouteId(routeId)
            routeViewModel.myMapLocationsResponse.observe(viewLifecycleOwner, Observer { response ->
                if (response.isSuccessful) {
                    Log.d("Warmbier", response.body().toString())
                    val mapLocationAdapter =
                        MapLocationAdapter(response.body()?.content ?: emptyList())
                    response.body()?.content.let {
                        Log.d("Warmbier", it.toString())
                        for (mapLocation in it!!) {
                            route.pointList[mapLocation.id] = (MapPoint(mapLocation))
                        }
                    }
                    binding.recyclerViewPoints.layoutManager = LinearLayoutManager(requireContext())

                    binding.recyclerViewPoints.adapter = mapLocationAdapter
                } else {
                    Log.d("Map Locations Response", response.errorBody().toString())
                }
            })
        }
        binding.backButton.setOnClickListener {
            changeBackFragment()
        }

        binding.chooseRoute.setOnClickListener {
            chooseRoute()
        }
    }

    private fun chooseRoute() {
        val mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        Log.d("Warmbier", route.toString())
        mapViewModel.route = route
        navigateToMapListener?.navigateToMap()
    }

    private fun changeBackFragment() {
        if (origin == "myRoutes")
            (parentFragment as? RoutesFragment)?.replaceFragment(1, MyRoutesFragment())
        else if (origin == "publicRoutes")
            (parentFragment as? RoutesFragment)?.replaceFragment(0, PublicRoutesFragment())

    }


}