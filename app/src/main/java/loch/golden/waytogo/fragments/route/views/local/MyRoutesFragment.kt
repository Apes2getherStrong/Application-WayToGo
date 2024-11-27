package loch.golden.waytogo.fragments.route.views.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentMyRoutesBinding
import loch.golden.waytogo.fragments.route.components.adapters.SimpleMyRoutesAdapter
import loch.golden.waytogo.fragments.route.views.RoutesFragment
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.viewmodels.LocalViewModel

@AndroidEntryPoint
class MyRoutesFragment : Fragment() {

    private lateinit var binding: FragmentMyRoutesBinding
    private val localViewModel: LocalViewModel by viewModels ()
    private lateinit var recyclerViewRouteAdapter: SimpleMyRoutesAdapter
    private var allRoutes: List<Route> = emptyList()
    private var bottomNav: BottomNavigationView? = null

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

        binding.addRouteFab.setOnClickListener() {
            (parentFragment as? RoutesFragment)?.replaceFragment(1, DatabaseMyRouteDetailFragment())
        }
        bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)

        initSearchView()
        initRecyclerView()
    }

    //TODO add published status and publish / unpublish /delete

    private fun initRecyclerView() {
        recyclerViewRouteAdapter = SimpleMyRoutesAdapter(emptyList(), requireContext())
        binding.recyclerViewMyRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewMyRoutes.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewRouteAdapter.setOnClickListener(object : SimpleMyRoutesAdapter.OnClickListener {

            override fun onItemClick(position: Int, route: Route, isDelete: Boolean) {
                if (isDelete) {
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder.setTitle("Delete Route")
                    alertDialogBuilder.setMessage("Are you sure you want to delete this route?")
                    alertDialogBuilder.setPositiveButton("Delete") { dialog, which ->
                        localViewModel.deleteRouteWithMapLocations(route.routeUid)

                        Snackbar.make(view!!, "Route deleted successfully", Snackbar.LENGTH_SHORT)
                            .setAnchorView(bottomNav).show()
                    }

                    alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }

                    alertDialogBuilder.show()

                } else {
                    val id = route.routeUid
                    val bundle = Bundle().apply {
                        putString("id", id)
                    }
                    val fr = DatabaseMyRouteDetailFragment()
                    fr.arguments = bundle // Set the arguments bundle to the fragment
                    (parentFragment as? RoutesFragment)?.replaceFragment(1, fr)
                }
            }
        })

        localViewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
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
                route.name.contains(searchedQuery, ignoreCase = true)
            }
            recyclerViewRouteAdapter.setRoutes(filteredRoutes)
        }
    }

}
