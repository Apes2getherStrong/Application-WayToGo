package loch.golden.waytogo.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loch.golden.waytogo.databinding.FragmentMyRoutesBinding
import loch.golden.waytogo.databinding.FragmentPublicRoutesBinding
import loch.golden.waytogo.map.creation.CreationPrefManager
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import java.io.File

class MyRoutesFragment : Fragment() {

    private lateinit var binding: FragmentMyRoutesBinding
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter

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
        recyclerViewRouteAdapter = RecyclerViewRouteAdapter()
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewRouteAdapter.setOnClickListener(object : RecyclerViewRouteAdapter.OnClickListener {
            override fun onItemClick(position: Int, route: Route) {
                val id = route.routeUid
                val bundle = Bundle().apply {
                    putString("id", id)
                }
                val fr = RouteDetailFragment()
                fr.arguments = bundle // Set the arguments bundle to the fragment
                (parentFragment as? RoutesFragment)?.replaceFragment(1,fr)
            }
        })

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
            lifecycleScope.launch {
                routeViewModel.getRoutes(0, 20).collectLatest { pagingData ->
                    val filteredRoutes = pagingData.filter { route ->
                        route.name.contains(searchedQuery, ignoreCase = true)
                    }
                    recyclerViewRouteAdapter.submitData(filteredRoutes)
                }
            }
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
