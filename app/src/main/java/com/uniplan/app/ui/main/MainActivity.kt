package com.uniplan.app.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.uniplan.app.ui.coinjar.CoinJarScreen
import com.uniplan.app.ui.coinjar.CoinJarViewModel
import com.uniplan.app.ui.focus.FocusScreen
import com.uniplan.app.ui.focus.FocusViewModel
import com.uniplan.app.ui.navigation.Screen
import com.uniplan.app.ui.onboarding.OnboardingScreen
import com.uniplan.app.ui.onboarding.OnboardingViewModel
import com.uniplan.app.ui.schedule.*
import com.uniplan.app.ui.settings.*
import com.uniplan.app.ui.stats.StatsScreen
import com.uniplan.app.ui.stats.StatsViewModel
import com.uniplan.app.ui.tasks.*
import com.uniplan.app.ui.theme.UniPlanTheme
import com.uniplan.app.ui.timeline.TimeBlockFormScreen
import com.uniplan.app.ui.timeline.TimeBlockFormViewModel
import com.uniplan.app.ui.today.TodayScreen
import com.uniplan.app.ui.today.TodayViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniPlanTheme {
                val onboardingDoneState by mainViewModel.onboardingDone.collectAsState()

                if (onboardingDoneState != null) {
                    UniPlanAppContent(startDestination = if (onboardingDoneState == true) Screen.Today.route else Screen.Onboarding.route)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniPlanAppContent(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Today.route,
        Screen.Schedule.route,
        Screen.Tasks.route,
        Screen.Stats.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Hôm nay") },
                        selected = currentRoute == Screen.Today.route,
                        onClick = {
                            navController.navigate(Screen.Today.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Lịch") },
                        selected = currentRoute == Screen.Schedule.route,
                        onClick = {
                            navController.navigate(Screen.Schedule.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                        label = { Text("Nhiệm vụ") },
                        selected = currentRoute == Screen.Tasks.route,
                        onClick = {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                        label = { Text("Thống kê") },
                        selected = currentRoute == Screen.Stats.route,
                        onClick = {
                            navController.navigate(Screen.Stats.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Cài đặt") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                val vm: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(viewModel = vm, onFinished = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Today.route) {
                val vm: TodayViewModel = hiltViewModel()
                TodayScreen(
                    viewModel = vm,
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToCoinJar = { navController.navigate(Screen.CoinJar.route) },
                    onNavigateToClassForm = { id -> navController.navigate(Screen.ClassForm.createRoute(id)) },
                    onNavigateToTaskForm = { id -> navController.navigate(Screen.TaskForm.createRoute(id)) },
                    onNavigateToExamForm = { id -> navController.navigate(Screen.ExamForm.createRoute(id)) },
                    onNavigateToTimeBlockForm = { id -> navController.navigate(Screen.TimeBlockForm.createRoute(id)) },
                    onNavigateToCreateSemester = { navController.navigate(Screen.SemesterList.route) }
                )
            }

            composable(Screen.Schedule.route) {
                val vm: ScheduleViewModel = hiltViewModel()
                ScheduleScreen(
                    viewModel = vm,
                    onNavigateToClassDetail = { sessionId, _ ->
                        navController.navigate(Screen.ClassForm.createRoute(sessionId))
                    },
                    onNavigateToClassForm = { id -> navController.navigate(Screen.ClassForm.createRoute(id)) }
                )
            }

            composable(Screen.Tasks.route) {
                val vm: TasksViewModel = hiltViewModel()
                TasksScreen(
                    viewModel = vm,
                    onNavigateToTaskForm = { id -> navController.navigate(Screen.TaskForm.createRoute(id)) },
                    onNavigateToExamForm = { id -> navController.navigate(Screen.ExamForm.createRoute(id)) }
                )
            }

            composable(Screen.Stats.route) {
                val vm: StatsViewModel = hiltViewModel()
                StatsScreen(
                    viewModel = vm,
                    onNavigateToCoinJar = { navController.navigate(Screen.CoinJar.route) },
                    onNavigateToFocus = { navController.navigate(Screen.Focus.createRoute(0L)) }
                )
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = vm,
                    onNavigateToSemesters = { navController.navigate(Screen.SemesterList.route) },
                    onNavigateToSubjects = { navController.navigate(Screen.SubjectList.route) },
                    onNavigateToPeriods = { navController.navigate(Screen.PeriodConfig.route) },
                    onNavigateToReminders = { navController.navigate(Screen.ReminderSettings.route) },
                    onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
                )
            }

            composable(Screen.SemesterList.route) {
                val vm: SemesterListViewModel = hiltViewModel()
                SemesterListScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.SubjectList.route) {
                val vm: SubjectListViewModel = hiltViewModel()
                SubjectListScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.PeriodConfig.route) {
                val vm: PeriodConfigViewModel = hiltViewModel()
                PeriodConfigScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.ReminderSettings.route) {
                val vm: ReminderSettingsViewModel = hiltViewModel()
                ReminderSettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.Backup.route) {
                val vm: BackupViewModel = hiltViewModel()
                BackupScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.CoinJar.route) {
                val vm: CoinJarViewModel = hiltViewModel()
                CoinJarScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.ClassForm.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                val vm: ClassFormViewModel = hiltViewModel()
                ClassFormScreen(viewModel = vm, sessionId = sessionId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.TaskForm.route,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                val vm: TaskFormViewModel = hiltViewModel()
                TaskFormScreen(viewModel = vm, taskId = taskId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.ExamForm.route,
                arguments = listOf(navArgument("examId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
                val vm: ExamFormViewModel = hiltViewModel()
                ExamFormScreen(viewModel = vm, examId = examId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.TimeBlockForm.route,
                arguments = listOf(navArgument("blockId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val blockId = backStackEntry.arguments?.getLong("blockId") ?: 0L
                val vm: TimeBlockFormViewModel = hiltViewModel()
                TimeBlockFormScreen(viewModel = vm, blockId = blockId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Focus.route,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                val vm: FocusViewModel = hiltViewModel()
                FocusScreen(viewModel = vm, taskId = taskId, onBack = { navController.popBackStack() })
            }
        }
    }
}
