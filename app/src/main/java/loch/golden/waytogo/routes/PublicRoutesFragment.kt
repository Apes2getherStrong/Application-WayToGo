package loch.golden.waytogo.routes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.android.material.snackbar.Snackbar
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
import loch.golden.waytogo.user.tokenmanager.TokenManager


class PublicRoutesFragment : Fragment() {

    private lateinit var binding: FragmentPublicRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var routeViewModel: RouteViewModel
    private val viewModel by viewModels<RouteViewModel>()
    private val appScope = CoroutineScope(SupervisorJob())
    private val routeDao: RouteDao by lazy {
        WayToGoDatabase.getDatabase(requireContext(), appScope).getRouteDao()
    }
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

        if (!isUserAuthenticatedAndTokenValid()) {
            //Snackbar.make(requireActivity().findViewById(android.R.id.content), "Login Successful", Snackbar.LENGTH_SHORT).show()
            Toast.makeText(requireContext(), "Please log in to view public routes", Toast.LENGTH_LONG).show()
        }
        initViewModel()
        initSearchView()
        initRecyclerView()
        observeRouteResponse()

        //binding.recyclerViewRoutes.addOnScrollListener(Rec)

    }


    private fun isUserAuthenticatedAndTokenValid(): Boolean {
        val token = tokenManager.getToken()
        return !token.isNullOrBlank() && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val expiresAtMillis = decodedJWT.expiresAt.time

        val currentTimeMillis = System.currentTimeMillis()

        return expiresAtMillis < currentTimeMillis
    }


    private fun initRecyclerView() {
        recyclerViewRouteAdapter = RecyclerViewRouteAdapter(viewModel, lifecycleScope)
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

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

    private fun initViewModel() {
        val repository = RouteRepository(routeDao)
        val routeViewModelFactory = RouteViewModelFactory(repository)
        routeViewModel = ViewModelProvider(this, routeViewModelFactory)[RouteViewModel::class.java]

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

