package com.uniplan.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.uniplan.app.R
import com.uniplan.app.UniPlanApp
import com.uniplan.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val settingsDataStore = UniPlanApp.instance.settingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todayFragment, R.id.scheduleFragment, R.id.taskListFragment,
                R.id.statsFragment, R.id.settingsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabQuickAdd.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabQuickAdd.visibility = View.GONE
                }
            }
        }

        binding.fabQuickAdd.setOnClickListener { view ->
            showQuickAddMenu(view)
        }

        lifecycleScope.launch {
            val isOnboardingDone = settingsDataStore.isOnboardingDone.first()
            if (!isOnboardingDone) {
                navController.navigate(R.id.onboardingFragment)
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun showQuickAddMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "Thêm Buổi học")
        popup.menu.add(0, 2, 1, "Thêm Khối thời gian")
        popup.menu.add(0, 3, 2, "Thêm Nhiệm vụ")
        popup.menu.add(0, 4, 3, "Thêm Lịch thi")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> navController.navigate(R.id.sessionFormFragment)
                2 -> navController.navigate(R.id.timeBlockFormFragment)
                3 -> navController.navigate(R.id.taskFormFragment)
                4 -> navController.navigate(R.id.examFormFragment)
            }
            true
        }
        popup.show()
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "uniplan") {
            when (data.host) {
                "class" -> navController.navigate(R.id.sessionDetailFragment)
                "task" -> navController.navigate(R.id.taskDetailFragment)
                "exam" -> navController.navigate(R.id.examFormFragment)
                "today" -> navController.navigate(R.id.todayFragment)
            }
        }
    }
}
