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
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentRouteDetailBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.OnNavigateToMapListener
import loch.golden.waytogo.routes.adapter.MapLocationAdapter
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory


class RouteDetailFragment() : Fragment() {

    private lateinit var binding: FragmentRouteDetailBinding
    private lateinit var mapLocationRecyclerView: MapLocationAdapter
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
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
        val routeId = arguments?.getString("id") ?: "" //TODO add error message


            routeViewModel.getRouteById(routeId)
            routeViewModel.myRouteResponse.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    Log.d("Warmbier", response.body().toString())
                    route = MapRoute(
                        response.body()!!.routeUid,
                        response.body()!!.name,
                        response.body()!!.description,
                        mutableMapOf()
                    )
                    binding.routeTitle.text = response.body()?.name
                    binding.routeDescription.text = response.body()?.description
                    Log.d("Response id", response.body()!!.routeUid)
                    Log.d("Response title", response.body()!!.name)

                } else {
                    Log.d("Response", response.errorBody().toString())

                }

                routeViewModel.getMapLocationsByRouteId(routeId)
                routeViewModel.myMapLocationsResponse.observe(viewLifecycleOwner) { response ->
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
                }


            }
            binding.backButton.setOnClickListener {
                changeBackFragment()
            }

            binding.chooseRoute.setOnClickListener {
                chooseRoute()
            }

    }


    private fun chooseRoute() {
        val mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        Log.d("Warmbier", route.toString())
        mapViewModel.route = route
        navigateToMapListener?.navigateToMap()
    }

    private fun changeBackFragment() {


        (parentFragment as? RoutesFragment)?.replaceFragment(0, PublicRoutesFragment())

    }


}