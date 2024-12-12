package com.example.journeypal.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.journeypal.R

class CountryGuideAdapter(
    private val context: Context
) : RecyclerView.Adapter<CountryGuideAdapter.CountryGuideViewHolder>() {

    private var countryGuides = listOf<CountryGuide>()
    fun submitList(list: List<CountryGuide>) {
        countryGuides = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryGuideViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_country_guide, parent, false)
        return CountryGuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryGuideViewHolder, position: Int) {
        val countryGuide = countryGuides[position]

        holder.countryNameTextView.text = countryGuide.countryName
        holder.safetyTipsTextView.text = countryGuide.safetyTips
        Glide.with(context)
            .load(countryGuide.imageUrl)
            .into(holder.countryImageView)
        holder.webView.loadUrl(countryGuide.videoUrl)
    }

    override fun getItemCount(): Int {
        return countryGuides.size
    }

    inner class CountryGuideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryNameTextView: TextView = view.findViewById(R.id.countryNameTextView)
        val safetyTipsTextView: TextView = view.findViewById(R.id.safetyTipsTextView)
        val countryImageView: ImageView = view.findViewById(R.id.countryImageView)
        val webView: WebView = view.findViewById(R.id.webView)  // To show video
    }
}
