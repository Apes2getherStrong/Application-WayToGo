package loch.golden.waytogo.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import loch.golden.waytogo.R
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentWelcomeBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.tokenmanager.TokenManager

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var tokenManager: TokenManager
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        val username = tokenManager.getUsername()

        username?.let { welcome(it) }
    }
    private fun welcome(username: String) {
//        routeViewModel.getUserByUserId(userId)
//        routeViewModel.userResponse.observe(viewLifecycleOwner) { response ->
//            if (response.isSuccessful) {
//
//
//            }
//        }
        binding.welcomeText.text = "Welcome, ${username}"
        binding.usernameEditText.setText(username)

        binding.saveProfileButton.setOnClickListener{
            Toast.makeText(requireContext(),"Successfully changed username. BTW NOT WORKING YEt.",Toast.LENGTH_LONG).show()
        }

        binding.logoutButton.setOnClickListener{
            logout()
        }
    }

    private fun logout() {
        tokenManager.clearToken()
        Toast.makeText(requireContext(),"Logout Successful",Toast.LENGTH_LONG).show()

        val loginFragment = LoginFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_main, loginFragment)
            .addToBackStack(null)
            .commit()

    }
}