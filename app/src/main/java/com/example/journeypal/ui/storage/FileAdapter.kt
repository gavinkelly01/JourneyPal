
package com.example.journeypal.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileAdapter(
    private val fileList: List<File>,
    private val actionListener: FileActionListener
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    interface FileActionListener {
        fun onDelete(file: File)
        fun onDownload(file: File)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileList[position])
    }

    override fun getItemCount(): Int = fileList.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameText: TextView = itemView.findViewById(R.id.fileName)
        private val fileInfoText: TextView = itemView.findViewById(R.id.fileInfo)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
        private val downloadButton: AppCompatImageButton = itemView.findViewById(R.id.downloadButton)
        private val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(file: File) {
            // Display original file name (without .enc extension)
            val originalName = file.name.removeSuffix(".enc")
            fileNameText.text = originalName

            // File metadata
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val lastModified = dateFormat.format(Date(file.lastModified()))
            val fileSize = formatFileSize(file.length())
            fileInfoText.text = "$fileSize • $lastModified"



            // Set up action buttons
            downloadButton.setOnClickListener { actionListener.onDownload(file) }
            deleteButton.setOnClickListener { actionListener.onDelete(file) }
        }



        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }
        }
    }
}