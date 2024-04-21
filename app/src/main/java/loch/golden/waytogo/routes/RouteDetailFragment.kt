package loch.golden.waytogo.routes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import loch.golden.waytogo.databinding.FragmentRouteDetailBinding
import loch.golden.waytogo.routes.model.Route


class RouteDetailFragment : Fragment() {

    private lateinit var binding: FragmentRouteDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


}