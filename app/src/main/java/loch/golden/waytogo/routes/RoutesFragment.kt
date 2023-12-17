package loch.golden.waytogo.routes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.databinding.FragmentRoutesBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.routes.datamodel.DataRoutes
import loch.golden.waytogo.routes.repository.RouteRepository

class RoutesFragment : Fragment() {

    private lateinit var binding: FragmentRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var routeViewModel: RouteViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val routeList = getRoutes()

        recyclerViewRouteAdapter = RecyclerViewRouteAdapter(ArrayList())
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

        val repository = RouteRepository()
        val routeViewModelFactory = RouteViewModelFactory(repository)
        routeViewModel = ViewModelProvider(this,routeViewModelFactory)[RouteViewModel::class.java]
        //pobierz routa
        routeViewModel.getRoutes()
        //obserwowanie i aktualizacja view adaptera
        routeViewModel.routeResponse.observe(viewLifecycleOwner, Observer { response ->
            recyclerViewRouteAdapter.updateRoutes(response)

        })
    }

//    private fun getRoutes(): ArrayList<DataRoutes> {
//
//        return arrayListOf(
//            DataRoutes("Przyklad", R.drawable.ic_route_24, "Przyklad")
//
//        )
//    }
}