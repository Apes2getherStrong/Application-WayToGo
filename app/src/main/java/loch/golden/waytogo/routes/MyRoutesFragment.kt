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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import loch.golden.waytogo.databinding.FragmentMyRoutesBinding
import loch.golden.waytogo.routes.adapter.SimpleMyRoutesAdapter
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.room.WayToGoDatabase
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
//        listFromFolder()
        listFromPref()
        initSearchView()
        initRecyclerView()
    }
    private fun initRecyclerView() {
        recyclerViewRouteAdapter = SimpleMyRoutesAdapter(emptyList())
        binding.recyclerViewMyRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewMyRoutes.layoutManager = LinearLayoutManager(requireContext())


        routeViewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
            allRoutes = routes
            recyclerViewRouteAdapter.setRoutes(routes)
        }

//        recyclerViewRouteAdapter.setOnClickListener(object : RecyclerViewRouteAdapter.OnClickListener {
//            override fun onItemClick(position: Int, route: Route) {
//                val id = route.routeUid
//                val bundle = Bundle().apply {
//                    putString("id", id)
//                }
//                val fr = RouteDetailFragment()
//                fr.arguments = bundle // Set the arguments bundle to the fragment
//                (parentFragment as? RoutesFragment)?.replaceFragment(1,fr)
//            }
//        })

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


    private fun listFromPref() {
        //TODO move this to creationRoutePref or at least a part of it
        val sharedPreferences =
            requireContext().getSharedPreferences("routes_pref", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, title) in allEntries) {
            Log.d("Warmbier", title.toString())
        }
    }

    private fun listFromFolder() {
        val filesDir = requireContext().filesDir

        val myRoutesFolder = File(filesDir, "my_routes")

        if (myRoutesFolder.exists() && myRoutesFolder.isDirectory) {
            val subFolders = myRoutesFolder.listFiles { file -> file.isDirectory }
            subFolders?.forEach { folder ->
                // Perform operations with each subfolder here
                Log.d("Warmbier", "Subfolder: ${folder.name}")
            } //TODO move all paths to an object in consts folder
        }
    }
}
