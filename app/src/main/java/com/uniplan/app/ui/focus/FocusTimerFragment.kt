package com.uniplan.app.ui.focus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.uniplan.app.databinding.FragmentFocusTimerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FocusTimerFragment : Fragment() {

    private var _binding: FragmentFocusTimerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusTimerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.btnStartTimer.setOnClickListener {
            viewModel.startTimer(25)
        }

        binding.btnStopTimer.setOnClickListener {
            viewModel.stopTimer()
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvTimerCountdown.text = state.formattedTime
                binding.btnStartTimer.isEnabled = !state.isRunning
                binding.btnStopTimer.isEnabled = state.isRunning
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _binding = null
    }
}
