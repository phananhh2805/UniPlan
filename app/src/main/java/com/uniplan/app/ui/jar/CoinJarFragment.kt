package com.uniplan.app.ui.jar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.uniplan.app.databinding.FragmentCoinJarBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoinJarFragment : Fragment() {

    private var _binding: FragmentCoinJarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CoinJarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinJarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvMonthTitle.text = "Lọ thành tựu (${state.monthKey})"
                binding.tvCoinCount.text = "${state.coins.size} / ${state.goal} xu"
                binding.tvMotivational.text = state.motivationalText
                binding.coinJarView.setCoins(state.coins, state.goal)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
