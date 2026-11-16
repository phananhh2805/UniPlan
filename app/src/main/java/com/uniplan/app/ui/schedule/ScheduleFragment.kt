package com.uniplan.app.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.uniplan.app.R
import com.uniplan.app.databinding.FragmentScheduleBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_mode_week -> viewModel.setMode("WEEK")
                    R.id.btn_mode_day -> viewModel.setMode("DAY")
                    R.id.btn_mode_month -> viewModel.setMode("MONTH")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvScheduleTitle.text = state.titleText
                if (state.occurrences.isEmpty()) {
                    binding.tvEmptySchedule.visibility = View.VISIBLE
                } else {
                    binding.tvEmptySchedule.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
