package loch.golden.waytogo.user

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentRegisterBinding
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory


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

            val user = User(username,password,login)

            register(user)

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
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun register(user: User) {
        routeViewModel.register(user)
        navigateToLoginFragment()

    }

    private fun navigateToLoginFragment() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container_main, LoginFragment())
        }
    }

}