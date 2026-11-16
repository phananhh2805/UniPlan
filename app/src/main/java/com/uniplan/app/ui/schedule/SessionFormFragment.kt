package com.uniplan.app.ui.schedule

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SubjectEntity
import com.uniplan.app.databinding.FragmentSessionFormBinding
import com.uniplan.app.domain.model.ClassType
import com.uniplan.app.domain.model.WeekPattern
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionFormFragment : Fragment() {

    private var _binding: FragmentSessionFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private var subjectsList: List<SubjectEntity> = emptyList()
    private var selectedSubjectId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.subjects.collectLatest { list ->
                subjectsList = list
                val names = list.map { it.name }
                val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, names)
                binding.actvSubject.setAdapter(adapter)
            }
        }

        binding.actvSubject.setOnItemClickListener { _, _, position, _ ->
            if (position in subjectsList.indices) {
                selectedSubjectId = subjectsList[position].id
            }
        }

        binding.btnSave.setOnClickListener {
            val startP = binding.etStartPeriod.text.toString().toIntOrNull() ?: 1
            val endP = binding.etEndPeriod.text.toString().toIntOrNull() ?: 3
            val room = binding.etRoom.text.toString().trim()

            val session = ClassSessionEntity(
                subjectId = selectedSubjectId,
                dayOfWeek = 1,
                startPeriod = startP,
                endPeriod = endP,
                startTime = "07:00",
                endTime = "09:35",
                room = room,
                campus = null,
                type = ClassType.LECTURE,
                weekFrom = 1,
                weekTo = 18,
                weekPattern = WeekPattern.ALL
            )
            viewModel.saveSession(session, 30)
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is SessionFormEvent.SaveSuccess -> {
                        Toast.makeText(requireContext(), "Đã lưu buổi học", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is SessionFormEvent.ValidationError -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    is SessionFormEvent.ConflictFound -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Trùng lịch")
                            .setMessage("Buổi học này bị trùng với buổi học khác. Bạn vẫn muốn lưu?")
                            .setPositiveButton("Vẫn lưu") { _, _ ->
                                val startP = binding.etStartPeriod.text.toString().toIntOrNull() ?: 1
                                val endP = binding.etEndPeriod.text.toString().toIntOrNull() ?: 3
                                val room = binding.etRoom.text.toString().trim()
                                val session = ClassSessionEntity(
                                    subjectId = selectedSubjectId,
                                    dayOfWeek = 1,
                                    startPeriod = startP,
                                    endPeriod = endP,
                                    startTime = "07:00",
                                    endTime = "09:35",
                                    room = room,
                                    campus = null,
                                    type = ClassType.LECTURE,
                                    weekFrom = 1,
                                    weekTo = 18,
                                    weekPattern = WeekPattern.ALL
                                )
                                viewModel.forceSaveSession(session, 30)
                            }
                            .setNegativeButton("Sửa lại", null)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
