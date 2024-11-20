package com.example.journeypal.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.R

// Use a proper Kotlin class declaration
class FileAdapter(private val files: List<String>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // Create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileName = files[position]
        holder.fileNameTextView.text = fileName
    }

    // Return total item count
    override fun getItemCount(): Int {
        return files.size
    }

    // ViewHolder to hold the views for each item
    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
    }
}
