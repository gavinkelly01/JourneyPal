// FilesFragment.kt
package com.example.journeypal.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.R

class FilesFragment : Fragment(R.layout.fragment_files) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var uploadButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // Initialize the RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the file adapter (You will define this later)
        fileAdapter = FileAdapter(getSampleFiles())
        recyclerView.adapter = fileAdapter

        // Setup the Upload Button
        uploadButton = view.findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            // Handle file upload action here
            Toast.makeText(requireContext(), "Upload functionality here", Toast.LENGTH_SHORT).show()
        }
    }

    // Sample function to generate file data (You can replace this with actual data)
    private fun getSampleFiles(): List<String> {
        return listOf("File 1", "File 2", "File 3", "File 4", "File 5")
    }
}
