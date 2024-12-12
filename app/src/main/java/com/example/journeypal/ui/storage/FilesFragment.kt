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
import java.io.File

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

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fileList = getSampleFiles()
        fileAdapter = FileAdapter(fileList, { file -> deleteFile(file) }, { file -> downloadFile(file) })
        recyclerView.adapter = fileAdapter

        uploadButton = view.findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            Toast.makeText(requireContext(), "Upload functionality here", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSampleFiles(): List<File> {
        return listOf(
            File("/path/to/file1.txt"),
            File("/path/to/file2.txt"),
            File("/path/to/file3.txt")
        )
    }

    private fun deleteFile(file: File) {
        Toast.makeText(requireContext(), "Deleted ${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun downloadFile(file: File) {
        Toast.makeText(requireContext(), "Download ${file.name} clicked", Toast.LENGTH_SHORT).show()
    }
}
