package com.example.journeypal.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.R
import java.io.File

class FileAdapter(
    private val fileList: List<File>,
    private val deleteListener: (File) -> Unit,
    private val downloadListener: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = fileList.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val downloadButton: Button = itemView.findViewById(R.id.downloadButton)

        fun bind(file: File) {
            fileNameTextView.text = file.name
            deleteButton.setOnClickListener {
                deleteListener(file)
            }

            downloadButton.setOnClickListener {
                downloadListener(file)
            }
        }
    }
}
