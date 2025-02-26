package com.example.journeypal.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.journeypal.R
import java.io.File

class FileAdapter(
    private val fileList: List<File>,
    private val actionListener: FileActionListener
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    interface FileActionListener {
        fun onDelete(file: File)
        fun onDownload(file: File)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileList[position])
    }

    override fun getItemCount(): Int = fileList.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val downloadButton: Button = itemView.findViewById(R.id.downloadButton)
        private val filePreview: ImageView = itemView.findViewById(R.id.filePreview)
        private val textPreview: TextView = itemView.findViewById(R.id.textPreview)

        fun bind(file: File) {
            fileNameTextView.text = file.name
            textPreview.visibility = View.GONE
            filePreview.visibility = View.GONE

            when {
                file.extension == "txt" -> {
                    textPreview.visibility = View.VISIBLE
                    textPreview.text = readTextPreview(file)
                }
                file.extension in listOf("png", "jpg", "jpeg") -> {
                    filePreview.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(file).into(filePreview)
                }
            }

            deleteButton.setOnClickListener { actionListener.onDelete(file) }
            downloadButton.setOnClickListener { actionListener.onDownload(file) }
        }

        private fun readTextPreview(file: File): String {
            return try {
                file.bufferedReader().useLines { lines ->
                    lines.take(3).joinToString("\n")
                }
            } catch (e: Exception) {
                "Error loading preview"
            }
        }
    }
}
