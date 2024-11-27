package loch.golden.waytogo.fragments.user.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentLoginBinding
import loch.golden.waytogo.viewmodels.BackendViewModel
import loch.golden.waytogo.services.dto.auth.AuthRequest
import loch.golden.waytogo.fragments.user.components.TokenManager
import retrofit2.HttpException
import java.io.IOException

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val backendViewModel: BackendViewModel by viewModels ()
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
            this.requireContext().hideKeyboard(binding.username)
            this.requireContext().hideKeyboard(binding.password)
        }

        binding.registerText.setOnClickListener {
            navigateToRegisterFragment()
        }

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        backendViewModel.authResponse.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                progressDialog.dismiss()
                tokenManager.saveToken(it.token)
                val userId = tokenManager.getUserIdFromJWT()
                userId?.let { it1 -> backendViewModel.getUserByUserId(it1) }
                backendViewModel.userDTOResponse.observe(viewLifecycleOwner) { response ->
                    if (response.isSuccessful) {
                        val user = response.body()!!
                        tokenManager.saveUserData(user)
                    }

                    Snackbar.make(requireView(), "Login Successful", Snackbar.LENGTH_SHORT)
                        .setAnchorView(bottomNav).show()
                    navigateToWelcomeFragment()
                }

            } ?: run {
                progressDialog.dismiss()
                Snackbar.make(requireView(), "Login Failed", Snackbar.LENGTH_SHORT)
                    .setAnchorView(bottomNav).show()
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
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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
            backendViewModel.login(authRequest)
        } catch (e: HttpException) {
            progressDialog.dismiss()
            when (e.code()) {
                404 -> {
                    Snackbar.make(requireView(), "Connection error", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: IOException) {
            progressDialog.dismiss()
            Snackbar.make(requireView(), "Network error", Snackbar.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Snackbar.make(requireView(), e.message ?: "Login failed. Incorrect password or login.", Snackbar.LENGTH_SHORT)
                .show()
        }
    }


}