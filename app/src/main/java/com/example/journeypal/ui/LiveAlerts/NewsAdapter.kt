package com.example.journeypal.ui.LiveAlerts

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.journeypal.databinding.ItemNewsCardBinding

class NewsAdapter(private val newsList: List<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(val binding: ItemNewsCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsList[position]
        with(holder.binding) {
            newsTitle.text = item.title
            newsSnippet.text = item.snippet
            newsDate.text = item.publishedAt.take(10)

            Glide.with(thumbnail.context)
                .load(item.thumbnailUrl)
                .centerCrop()
                .into(thumbnail)

            root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = newsList.size
}
