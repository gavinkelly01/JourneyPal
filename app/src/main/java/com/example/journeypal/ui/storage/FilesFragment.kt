package com.example.journeypal.ui.storage

import android.os.Bundle
import android.view.View
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

        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fileList = getSampleFiles()
        fileAdapter = FileAdapter(fileList, object : FileAdapter.FileActionListener {
            override fun onDelete(file: File) {
                deleteFile(file)
            }

            override fun onDownload(file: File) {
                downloadFile(file)
            }
        })

        recyclerView.adapter = fileAdapter

        uploadButton = view.findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            Toast.makeText(requireContext(), "Upload functionality here", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSampleFiles(): List<File> {
        val dir = requireContext().filesDir
        return listOf(
            File(dir, "file1.txt"),
            File(dir, "file2.png"),
            File(dir, "file3.jpg")
        ).filter { it.exists() } // Ensuring files actually exist
    }

    private fun deleteFile(file: File) {
        if (file.delete()) {
            Toast.makeText(requireContext(), "Deleted ${file.name}", Toast.LENGTH_SHORT).show()
            refreshFileList()
        } else {
            Toast.makeText(requireContext(), "Failed to delete ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(file: File) {
        Toast.makeText(requireContext(), "Download ${file.name} clicked", Toast.LENGTH_SHORT).show()
    }

    private fun refreshFileList() {
        val updatedFiles = getSampleFiles()
        fileAdapter = FileAdapter(updatedFiles, object : FileAdapter.FileActionListener {
            override fun onDelete(file: File) {
                deleteFile(file)
            }

            override fun onDownload(file: File) {
                downloadFile(file)
            }
        })
        recyclerView.adapter = fileAdapter
    }
}
