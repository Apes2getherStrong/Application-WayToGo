package loch.golden.waytogo.user

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar

import loch.golden.waytogo.R
import loch.golden.waytogo.custom.CustomLoading
import loch.golden.waytogo.databinding.FragmentLoginBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.tokenmanager.TokenManager


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
    private lateinit var progressDialog: AlertDialog
    private lateinit var tokenManager: TokenManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.fragment_custom_loading)
            .setCancelable(false)
            .create()

        tokenManager = TokenManager(requireContext())

        binding.buttonLogin.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val authRequest = AuthRequest(username, password)

            login(authRequest)

        }

        binding.registerText.setOnClickListener {
            navigateToRegisterFragment()
        }

        routeViewModel.authResponse.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                progressDialog.dismiss()
                tokenManager.saveToken(it.token)
                Snackbar.make(requireView(), "Login Successful", Snackbar.LENGTH_SHORT).show()
                //Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                navigateToWelcomeFragment()
            } ?: run {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
            }
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
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun navigateToRegisterFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container_main, RegisterFragment())
        }
    }

    private fun navigateToWelcomeFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container_main, WelcomeFragment())
        }
    }

    private fun login(authRequest: AuthRequest) {
        progressDialog.show()

        try {

            routeViewModel.login(authRequest)
        }catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Failed to connect.", Toast.LENGTH_LONG).show()
        }
    }




}