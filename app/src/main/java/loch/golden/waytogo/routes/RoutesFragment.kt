package loch.golden.waytogo.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.databinding.FragmentRoutesBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.routes.repository.RouteRepository

class RoutesFragment : Fragment() {

    private lateinit var binding: FragmentRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var searchView: SearchView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchView()
        recyclerViewRouteAdapter = RecyclerViewRouteAdapter(ArrayList())
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

        //val routeList = getRoutes()
        val repository = RouteRepository()
        val routeViewModelFactory = RouteViewModelFactory(repository)
        routeViewModel = ViewModelProvider(this, routeViewModelFactory)[RouteViewModel::class.java]
        //pobierz routa
        routeViewModel.getRoutes()
        //obserwowanie i aktualizacja view adaptera
        routeViewModel.routeResponse.observe(viewLifecycleOwner, Observer { response ->
            recyclerViewRouteAdapter.updateRoutes(response)
        })
    }

    private fun setupSearchView() {

        searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })
    }

    private fun search(query: String?) {
        query?.let { searchedQuery ->
            val filteredRoutes = routeViewModel.routeResponse.value?.filter { route ->
                route.name.contains(searchedQuery, ignoreCase = true)
            }

            if (filteredRoutes.isNullOrEmpty()) {
//                Toast.makeText(
//                    requireContext(), "Nie znaleziono tras o takiej nazwie", Toast.LENGTH_SHORT
//                ).show()
            } else {
                filteredRoutes.let { recyclerViewRouteAdapter.updateRoutes(it) }
            }
        }
    }

//    private fun getRoutes(): ArrayList<DataRoutes> {
//
//        return arrayListOf(
//            DataRoutes("Przyklad", R.drawable.ic_route_24, "Przyklad")
//
//        )
//    }
}