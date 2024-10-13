package loch.golden.waytogo.user

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
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
import loch.golden.waytogo.databinding.FragmentLoginBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.tokenmanager.TokenManager
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID


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

        if (tokenManager.getToken() != null && !tokenManager.isTokenExpired(tokenManager.getToken()!!)) {
            navigateToWelcomeFragment()
            return
        }
        binding.buttonLogin.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val authRequest = AuthRequest(username, password)

            login(authRequest)

        }

        binding.registerText.setOnClickListener {
            navigateToRegisterFragment()
        }

        val bottomNav =  requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        routeViewModel.authResponse.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                progressDialog.dismiss()
                tokenManager.saveToken(it.token)
                val username = tokenManager.getUserFromJWT()
                username?.let { it1 -> tokenManager.saveUsername(it1) }
                Snackbar.make(requireView(), "Login Successful", Snackbar.LENGTH_SHORT).setAnchorView(bottomNav).show()
                navigateToWelcomeFragment()
            } ?: run {
                progressDialog.dismiss()
                Snackbar.make(requireView(), "Login Failed", Snackbar.LENGTH_SHORT).setAnchorView(bottomNav).show()
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
        } catch (e: HttpException) {
            progressDialog.dismiss()
            when (e.code()) {
                404 -> {
                    Toast.makeText(
                        requireContext(),
                        "Connection error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: IOException) {
            progressDialog.dismiss()
            Toast.makeText(
                requireContext(),
                "Network error",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), e.message ?: "Login failed. Incorrect password or login.", Toast.LENGTH_LONG)
                .show()
        }
    }




}