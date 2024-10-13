package loch.golden.waytogo.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentRegisterBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import java.util.UUID


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener{
            val login = binding.login.text.toString()
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            val user = User(UUID.randomUUID().toString(),username,password,login)

            if(validateRegister(user)){
                register(user)
            }
        }

        routeViewModel.registerResponse.observe(viewLifecycleOwner) { registerResponse ->
            registerResponse?.let {
                //Snackbar.make(requireView(), "Registration Successful", Snackbar.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "Registration Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun register(user: User) {
        try {
            routeViewModel.register(user)
        }catch (e: Exception){
            Toast.makeText(requireContext(), e.toString(),Toast.LENGTH_LONG).show()
        }
        navigateToLoginFragment()

    }

    private fun validateRegister(user: User) : Boolean {
        if( user.username.isEmpty() || user.login.isEmpty() || user.password.isEmpty()) {
            Snackbar.make(requireView(), "All fields are required", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (user.password.length < 6) {
            Snackbar.make(requireView(), "Password must be at least 6 characters long", Snackbar.LENGTH_SHORT).show()
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