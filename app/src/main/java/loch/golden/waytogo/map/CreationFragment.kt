package loch.golden.waytogo.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import loch.golden.waytogo.databinding.FragmentMarkerCreationBinding
import loch.golden.waytogo.databinding.FragmentPointMapBinding

class CreationFragment : Fragment() {

    private lateinit var binding: FragmentMarkerCreationBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarkerCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

}