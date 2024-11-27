package loch.golden.waytogo.fragments.route.views.global

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loch.golden.waytogo.databinding.FragmentPublicRoutesBinding
import loch.golden.waytogo.fragments.route.components.adapters.RecyclerViewRouteAdapter
import loch.golden.waytogo.fragments.route.views.RoutesFragment
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.viewmodels.BackendViewModel
import loch.golden.waytogo.fragments.user.components.TokenManager

@AndroidEntryPoint
class PublicRoutesFragment : Fragment() {

    private lateinit var binding: FragmentPublicRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val viewModel by viewModels<BackendViewModel>()
    private lateinit var tokenManager: TokenManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPublicRoutesBinding.inflate(inflater, container, false)
        Log.d("Warmbier", container?.id.toString())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        initSearchView()
        initRecyclerView()
        observeRouteResponse()


    }

    private fun initRecyclerView() {
        recyclerViewRouteAdapter = RecyclerViewRouteAdapter(viewModel, lifecycleScope)
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        recyclerViewRouteAdapter.setOnClickListener(object :
            RecyclerViewRouteAdapter.OnClickListener {
            override fun onItemClick(position: Int, route: Route) {
                val id = route.routeUid
                val bundle = Bundle().apply {
                    putString("id", id)
                }
                val fr = RouteDetailFragment()
                fr.arguments = bundle
                (parentFragment as? RoutesFragment)?.replaceFragment(0, fr)
            }
        })

    }

    private fun refreshData() {
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                viewModel.getRoutes(0, 20).collectLatest { pagingData ->
                    recyclerViewRouteAdapter.submitData(pagingData)
                }
            } catch (e: Exception) {
                Log.e("RefreshData", "Error refreshing data: ${e.message}")
                //Toast.makeText(context, "Failed to refresh data", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.swipeRefreshLayout.postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false
        }, 2000)
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

