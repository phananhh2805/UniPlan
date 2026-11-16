package com.uniplan.app.ui.exam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uniplan.app.databinding.FragmentExamFormBinding

class ExamFormFragment : Fragment() {

    private var _binding: FragmentExamFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExamViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveExam.setOnClickListener {
            val examType = binding.etExamType.text.toString().trim()
            if (examType.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập loại kỳ thi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveExam(1L, examType) {
                Toast.makeText(requireContext(), "Đã lưu lịch thi", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
