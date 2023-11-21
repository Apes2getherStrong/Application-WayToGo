package loch.golden.waytogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import loch.golden.waytogo.databinding.DummyFragmentBinding
import loch.golden.waytogo.databinding.FragmentPointMapBinding

class DummyFragment : Fragment() {
    private lateinit var binding: DummyFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DummyFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }
}