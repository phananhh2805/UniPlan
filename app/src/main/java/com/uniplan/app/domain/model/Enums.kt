package com.uniplan.app.domain.model

enum class ClassType {
    LECTURE, PRACTICE, LAB
}

enum class WeekPattern {
    ALL, ODD, EVEN
}

enum class ExceptionStatus {
    CANCELED, MAKEUP, ROOM_CHANGED, TIME_CHANGED
}

enum class TaskStatus {
    TODO, DOING, DONE
}

enum class Priority {
    LOW, MEDIUM, HIGH
}

enum class TaskType {
    ASSIGNMENT, EXAM_PREP, PROJECT, PERSONAL
}

enum class BlockCategory {
    STUDY, WORK, REST, EXERCISE, OTHER
}

enum class RepeatRule {
    NONE, DAILY, WEEKLY
}

enum class ReminderOwner {
    CLASS, TASK, EXAM, BLOCK
}

enum class AlarmState {
    SCHEDULED, FIRED, CANCELED
}

enum class CoinTier {
    BRONZE, SILVER, GOLD, FOCUS
}

enum class CoinSource {
    TASK, FOCUS
}
