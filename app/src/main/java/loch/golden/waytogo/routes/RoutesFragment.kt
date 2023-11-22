package loch.golden.waytogo.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import loch.golden.waytogo.R
import loch.golden.waytogo.routes.adapter.RecyclerViewRouteAdapter
import loch.golden.waytogo.databinding.FragmentRoutesBinding
import loch.golden.waytogo.map.MapViewModel

class RoutesFragment : Fragment() {

    private lateinit var binding: FragmentRoutesBinding
    private lateinit var recyclerViewRouteAdapter: RecyclerViewRouteAdapter
    private val mapViewModel by activityViewModels<MapViewModel>()
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

        val routeList = getRoutes()

        recyclerViewRouteAdapter = RecyclerViewRouteAdapter(routeList)
        binding.recyclerViewRoutes.adapter = recyclerViewRouteAdapter
        binding.recyclerViewRoutes.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun getRoutes(): ArrayList<DataRoutes> {

        return arrayListOf(
            DataRoutes("Przyklad", R.drawable.ic_route_24, "Przyklad")

        )
    }
}