package com.uniplan.app.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uniplan.app.databinding.FragmentSubjectFormBinding

class SubjectFormFragment : Fragment() {

    private var _binding: FragmentSubjectFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveSubject.setOnClickListener {
            val name = binding.etSubjectName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên môn học", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val code = binding.etSubjectCode.text.toString().trim().ifEmpty { null }
            val credits = binding.etSubjectCredits.text.toString().toIntOrNull()

            viewModel.saveSubject(name, code, credits) {
                Toast.makeText(requireContext(), "Đã lưu môn học", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
