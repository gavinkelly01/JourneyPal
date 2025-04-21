package com.example.journeypal.ui.more

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.databinding.ItemMoreMenuBinding

class MoreMenuAdapter(
    private val items: List<MoreMenuItem>,
    private val onItemClick: (MoreMenuItem) -> Unit
) : RecyclerView.Adapter<MoreMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoreMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemMoreMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MoreMenuItem) {
            binding.imageViewIcon.setImageResource(item.iconResId)
            binding.textViewTitle.text = item.title

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
