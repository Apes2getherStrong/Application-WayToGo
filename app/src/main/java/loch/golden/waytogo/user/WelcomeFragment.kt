package loch.golden.waytogo.user

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentWelcomeBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.user.tokenmanager.TokenManager

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var tokenManager: TokenManager
    private var bottomNav : BottomNavigationView? = null
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

        bottomNav = requireActivity().findViewById(R.id.bottom_nav)
        tokenManager = TokenManager(requireContext())

        val user = tokenManager.getUserData()

        user?.let {
            Log.d("Welcome", "Username found: $it.username")
            welcome(user)
        }

    }

    private fun welcome(user: User) {
        binding.welcomeText.text = "Welcome, ${user.username}"
        binding.usernameEditText.setText(user.username)

        binding.saveProfileButton.setOnClickListener {
            val newUsername = binding.usernameEditText.text.toString()
            tokenManager.saveUsername(newUsername)
            val userId = tokenManager.getUserIdFromJWT()
            if (userId != null) {
                val updatedUser = user.copy(username = newUsername )
                tokenManager.saveUserData(updatedUser)

                routeViewModel.putUserByUserId(userId, updatedUser)

                routeViewModel.putUserResponse.observe(viewLifecycleOwner) { response ->
                    if (response.isSuccessful) {
                        Snackbar.make(requireView(), "Username updated successfully!", Snackbar.LENGTH_LONG).setAnchorView(bottomNav).show()
                    } else {
                        Snackbar.make(requireView(), "Failed to update username", Snackbar.LENGTH_SHORT).setAnchorView(bottomNav).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_LONG).show()
            }
        }

        binding.logoutButton.setOnClickListener {
            Log.d("Logout", "test_logout")
            logout()
        }
    }

    private fun logout() {
        tokenManager.clearToken()
        Snackbar.make(
            requireView(),
            "Logout successful",
            Snackbar.LENGTH_SHORT
        ).setAnchorView(bottomNav).show()

        val loginFragment = LoginFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_main, loginFragment)
            .addToBackStack(null)
            .commit()

    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}