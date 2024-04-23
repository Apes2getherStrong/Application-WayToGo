package loch.golden.waytogo.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import loch.golden.waytogo.databinding.FragmentMyRoutesBinding
import loch.golden.waytogo.databinding.FragmentPublicRoutesBinding
import loch.golden.waytogo.map.creation.CreationPrefManager
import java.io.File

class MyRoutesFragment : Fragment() {

    private lateinit var binding: FragmentMyRoutesBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        listFromFolder()
        listFromPref()
    }


    private fun listFromPref() {
        //TODO move this to creationRoutePref or at least a part of it
        val sharedPreferences =
            requireContext().getSharedPreferences("routes_pref", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, title) in allEntries) {
            Log.d("Warmbier", title.toString())
        }
    }

    private fun listFromFolder() {
        val filesDir = requireContext().filesDir

        val myRoutesFolder = File(filesDir, "my_routes")

        if (myRoutesFolder.exists() && myRoutesFolder.isDirectory) {
            val subFolders = myRoutesFolder.listFiles { file -> file.isDirectory }
            subFolders?.forEach { folder ->
                // Perform operations with each subfolder here
                Log.d("Warmbier", "Subfolder: ${folder.name}")
            } //TODO move all paths to an object in consts folder
        }
    }
}
