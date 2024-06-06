package loch.golden.waytogo.routes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentLoginBinding
import loch.golden.waytogo.routes.api.ApiService
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.auth.AuthRequest
import loch.golden.waytogo.routes.model.auth.AuthResponse
import loch.golden.waytogo.routes.model.user.User
import loch.golden.waytogo.routes.tokenmanager.TokenManager
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import loch.golden.waytogo.user.RegisterFragment
import retrofit2.Response


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }
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

        tokenManager = TokenManager(requireContext())

        binding.buttonLogin.setOnClickListener{
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val authRequest = AuthRequest(username,password)

            login(authRequest)

        }

        binding.registerText.setOnClickListener {
            navigateToRegisterFragment()
        }

        routeViewModel.authResponse.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                tokenManager.saveToken(it.token)
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun navigateToRegisterFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container_main,RegisterFragment())
        }
    }

    private fun login(authRequest: AuthRequest) {

        routeViewModel.login(authRequest)

    }


}