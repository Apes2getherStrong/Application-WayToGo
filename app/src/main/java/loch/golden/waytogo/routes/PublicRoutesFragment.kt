package loch.golden.waytogo.routes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loch.golden.waytogo.databinding.FragmentPublicRoutesBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.repository.RouteRepository
import loch.golden.waytogo.routes.room.WayToGoDatabase
import loch.golden.waytogo.routes.room.dao.RouteDao
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory

class PublicRoutesFragment() : Fragment() {

    private lateinit var binding: FragmentPublicRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var routeViewModel: RouteViewModel
    private val viewModel by viewModels<RouteViewModel>()
    private val appScope = CoroutineScope(SupervisorJob())
    private val routeDao: RouteDao by lazy {
        WayToGoDatabase.getDatabase(requireContext(), appScope).getRouteDao()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPublicRoutesBinding.inflate(inflater, container, false)
        Log.d("Warmbier",container?.id.toString())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchView()
        initRecyclerView()
        initViewModel()

        //binding.recyclerViewRoutes.addOnScrollListener(Rec)

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
                (parentFragment as? RoutesFragment)?.replaceFragment(0,fr)
            }
        })

    }

    private fun initViewModel() {
        val repository = RouteRepository(routeDao)
        val routeViewModelFactory = RouteViewModelFactory(repository)
        routeViewModel = ViewModelProvider(this, routeViewModelFactory)[RouteViewModel::class.java]
        observeRouteResponse()
    }

    private fun observeRouteResponse() {

        lifecycleScope.launch {
            viewModel.getRoutes(0, 20).collectLatest { pagingData ->

                recyclerViewRouteAdapter.submitData(pagingData)

            }
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
            lifecycleScope.launch {
                viewModel.getRoutes(0, 20).collectLatest { pagingData ->
                    val filteredRoutes = pagingData.filter { route ->
                        route.name.contains(searchedQuery, ignoreCase = true)
                    }
                    recyclerViewRouteAdapter.submitData(filteredRoutes)
                }
            }
        }
    }




}

