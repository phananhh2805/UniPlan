package com.uniplan.app.data.local.db

import androidx.room.TypeConverter
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.BlockCategory
import com.uniplan.app.domain.model.ClassType
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.RepeatRule
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.model.TaskType
import com.uniplan.app.domain.model.WeekPattern

class Converters {
    @TypeConverter
    fun fromClassType(value: ClassType): String = value.name
    @TypeConverter
    fun toClassType(value: String): ClassType = ClassType.valueOf(value)

    @TypeConverter
    fun fromWeekPattern(value: WeekPattern): String = value.name
    @TypeConverter
    fun toWeekPattern(value: String): WeekPattern = WeekPattern.valueOf(value)

    @TypeConverter
    fun fromExceptionStatus(value: ExceptionStatus): String = value.name
    @TypeConverter
    fun toExceptionStatus(value: String): ExceptionStatus = ExceptionStatus.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name
    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name
    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name
    @TypeConverter
    fun toTaskType(value: String): TaskType = TaskType.valueOf(value)

    @TypeConverter
    fun fromBlockCategory(value: BlockCategory): String = value.name
    @TypeConverter
    fun toBlockCategory(value: String): BlockCategory = BlockCategory.valueOf(value)

    @TypeConverter
    fun fromRepeatRule(value: RepeatRule): String = value.name
    @TypeConverter
    fun toRepeatRule(value: String): RepeatRule = RepeatRule.valueOf(value)

    @TypeConverter
    fun fromReminderOwner(value: ReminderOwner): String = value.name
    @TypeConverter
    fun toReminderOwner(value: String): ReminderOwner = ReminderOwner.valueOf(value)

    @TypeConverter
    fun fromAlarmState(value: AlarmState): String = value.name
    @TypeConverter
    fun toAlarmState(value: String): AlarmState = AlarmState.valueOf(value)

    @TypeConverter
    fun fromCoinTier(value: CoinTier): String = value.name
    @TypeConverter
    fun toCoinTier(value: String): CoinTier = CoinTier.valueOf(value)

    @TypeConverter
    fun fromCoinSource(value: CoinSource): String = value.name
    @TypeConverter
    fun toCoinSource(value: String): CoinSource = CoinSource.valueOf(value)
}
