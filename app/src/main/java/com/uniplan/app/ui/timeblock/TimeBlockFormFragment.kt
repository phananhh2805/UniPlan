package com.uniplan.app.ui.timeblock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uniplan.app.databinding.FragmentTimeBlockFormBinding

class TimeBlockFormFragment : Fragment() {

    private var _binding: FragmentTimeBlockFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimeBlockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeBlockFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveBlock.setOnClickListener {
            val title = binding.etBlockTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên khối thời gian", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveTimeBlock(title) {
                Toast.makeText(requireContext(), "Đã lưu khối thời gian", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
