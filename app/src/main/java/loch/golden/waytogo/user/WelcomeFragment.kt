package loch.golden.waytogo.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        welcome()
    }
    private fun welcome() {
        binding.welcomeText.text = "Welcome, username"

        binding.saveProfileButton.setOnClickListener{
            Toast.makeText(requireContext(),"Succesfully changed username. BTW NOT WORKING YEt.",Toast.LENGTH_LONG).show()
        }
    }
}