package com.example.journeypal.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.journeypal.databinding.FragmentCameraBinding
import com.example.journeypal.databinding.FragmentGuideBinding

class CameraFragment : Fragment() {

private var _binding: FragmentCameraBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val dashboardViewModel =
            ViewModelProvider(this).get(CameraViewModel::class.java)

    _binding = FragmentCameraBinding.inflate(inflater, container, false)
    val root: View = binding.root

    dashboardViewModel.text.observe(viewLifecycleOwner) {
    }
    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}