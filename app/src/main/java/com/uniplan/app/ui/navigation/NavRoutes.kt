package com.uniplan.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Today : Screen("today")
    object Schedule : Screen("schedule")
    object Tasks : Screen("tasks")
    object Stats : Screen("stats")
    object Settings : Screen("settings")

    object SemesterList : Screen("semester_list")
    object SubjectList : Screen("subject_list")
    object PeriodConfig : Screen("period_config")

    object ClassForm : Screen("class_form?sessionId={sessionId}") {
        fun createRoute(sessionId: Long = 0L) = "class_form?sessionId=$sessionId"
    }

    object TaskForm : Screen("task_form?taskId={taskId}") {
        fun createRoute(taskId: Long = 0L) = "task_form?taskId=$taskId"
    }

    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }

    object ExamForm : Screen("exam_form?examId={examId}") {
        fun createRoute(examId: Long = 0L) = "exam_form?examId=$examId"
    }

    object TimeBlockForm : Screen("time_block_form?blockId={blockId}") {
        fun createRoute(blockId: Long = 0L) = "time_block_form?blockId=$blockId"
    }

    object Focus : Screen("focus?taskId={taskId}") {
        fun createRoute(taskId: Long = 0L) = "focus?taskId=$taskId"
    }

    object CoinJar : Screen("coin_jar")
    object ReminderSettings : Screen("reminder_settings")
    object Backup : Screen("backup")
}
