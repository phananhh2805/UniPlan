package com.uniplan.app.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.uniplan.app.R
import com.uniplan.app.databinding.FragmentTodayBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodayViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnViewJar.setOnClickListener {
            findNavController().navigate(R.id.coinJarFragment)
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvDate.text = state.dateText
                binding.tvWeekLabel.text = state.weekText
                binding.tvNextClassInfo.text = state.nextClassMessage
                binding.tvJarProgress.text = "${state.coinsThisMonth.size} / ${state.jarGoal} xu"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
