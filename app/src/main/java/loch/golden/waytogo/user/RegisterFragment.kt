package loch.golden.waytogo.user

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentRegisterBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.model.User
import java.util.UUID


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private var bottomNav : BottomNavigationView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = requireActivity().findViewById(R.id.bottom_nav)
        binding.buttonRegister.setOnClickListener {
            val login = binding.login.text.toString()
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            val user = User(UUID.randomUUID().toString(), username, password, login)

            if (validateRegister(user)) {
                register(user)
            }
            this.requireContext().hideKeyboard(binding.login)
            this.requireContext().hideKeyboard(binding.username)
            this.requireContext().hideKeyboard(binding.password)
        }
        routeViewModel.registerResponse.observe(viewLifecycleOwner) { registerResponse ->
            registerResponse?.let {
                Snackbar.make(view, "Registration Successful", Snackbar.LENGTH_SHORT)
                    .setAnchorView(bottomNav).show()
            } ?: run {
                Snackbar.make(
                    view,
                    "Registration Failed",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomNav).show()
            }
        }


        binding.login.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.login.clearFocus()
                this.requireContext().hideKeyboard(binding.login)
                true
            } else false
        }

        binding.username.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.username.clearFocus()
                this.requireContext().hideKeyboard(binding.username)
                true
            } else false
        }

        binding.password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.password.clearFocus()
                this.requireContext().hideKeyboard(binding.password)
                true
            } else false
        }


    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun register(user: User) {
        try {
            routeViewModel.register(user)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
        }
        navigateToLoginFragment()

    }

    private fun validateRegister(user: User): Boolean {
        if (user.username.isEmpty() || user.login.isEmpty() || user.password.isEmpty()) {
            Snackbar.make(requireView(), "All fields are required", Snackbar.LENGTH_SHORT).setAnchorView(bottomNav).show()
            return false
        }
        if (user.password.length < 6) {
            Snackbar.make(
                requireView(),
                "Password must be at least 6 characters long",
                Snackbar.LENGTH_SHORT
            ).setAnchorView(bottomNav).show()
            return false
        }
        return true
    }

    private fun navigateToLoginFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container_main, LoginFragment())
        }
    }

}