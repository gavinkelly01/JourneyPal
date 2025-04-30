package com.example.journeypal.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)

        val moreItems = listOf(
            MoreMenuItem(R.drawable.folder_24px, getString(R.string.storage), R.id.navigation_storage),
            MoreMenuItem(R.drawable.photo_camera_24px, getString(R.string.title_camera), R.id.navigation_camera),
            MoreMenuItem(R.drawable.emergency_icon, getString(R.string.title_emergency), R.id.emergency_sos),
            MoreMenuItem(R.drawable.alertsicon, getString(R.string.title_livealerts), R.id.livealerts),
            MoreMenuItem(R.drawable.weather_icon, getString(R.string.title_weather), R.id.WeatherFragment),
        )

        val adapter = MoreMenuAdapter(moreItems) { menuItem ->
            findNavController().navigate(menuItem.destinationId)
        }

        binding.recyclerViewMoreMenu.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

