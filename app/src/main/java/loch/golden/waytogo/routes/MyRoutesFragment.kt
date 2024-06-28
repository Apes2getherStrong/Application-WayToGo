package loch.golden.waytogo.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.databinding.FragmentMyRoutesBinding
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.routes.adapter.SimpleMyRoutesAdapter
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import java.io.File

class MyRoutesFragment : Fragment() {

    private lateinit var binding: FragmentMyRoutesBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private lateinit var recyclerViewRouteAdapter: SimpleMyRoutesAdapter
    private var allRoutes: List<Route> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchView()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerViewRouteAdapter = SimpleMyRoutesAdapter(emptyList())
        binding.recyclerViewMyRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewMyRoutes.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewRouteAdapter.setOnClickListener(object : SimpleMyRoutesAdapter.OnClickListener{

            override fun onItemClick(position: Int, route: Route) {
                val id = route.routeUid
                val bundle = Bundle().apply {
                    putString("id", id)
                }
                val fr = DatabaseMyRouteDetailFragment()
                fr.arguments = bundle // Set the arguments bundle to the fragment
                (parentFragment as? RoutesFragment)?.replaceFragment(1,fr)
            }

        })

        routeViewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
            allRoutes = routes
            recyclerViewRouteAdapter.setRoutes(routes)
        }

    }

    private fun initSearchView() {

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            val filteredRoutes = allRoutes.filter { route ->
                route.name.contains(searchedQuery,ignoreCase = true )
            }
            recyclerViewRouteAdapter.setRoutes(filteredRoutes)
        }
    }

}
