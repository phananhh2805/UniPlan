package com.uniplan.app.data.local.db

import androidx.room.TypeConverter
import com.uniplan.app.domain.model.*

class Converters {
    @TypeConverter fun fromClassType(v: ClassType): String = v.name
    @TypeConverter fun toClassType(v: String): ClassType = ClassType.valueOf(v)

    @TypeConverter fun fromWeekPattern(v: WeekPattern): String = v.name
    @TypeConverter fun toWeekPattern(v: String): WeekPattern = WeekPattern.valueOf(v)

    @TypeConverter fun fromExceptionStatus(v: ExceptionStatus): String = v.name
    @TypeConverter fun toExceptionStatus(v: String): ExceptionStatus = ExceptionStatus.valueOf(v)

    @TypeConverter fun fromTaskStatus(v: TaskStatus): String = v.name
    @TypeConverter fun toTaskStatus(v: String): TaskStatus = TaskStatus.valueOf(v)

    @TypeConverter fun fromPriority(v: Priority): String = v.name
    @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)

    @TypeConverter fun fromTaskType(v: TaskType): String = v.name
    @TypeConverter fun toTaskType(v: String): TaskType = TaskType.valueOf(v)

    @TypeConverter fun fromBlockCategory(v: BlockCategory): String = v.name
    @TypeConverter fun toBlockCategory(v: String): BlockCategory = BlockCategory.valueOf(v)

    @TypeConverter fun fromRepeatRule(v: RepeatRule): String = v.name
    @TypeConverter fun toRepeatRule(v: String): RepeatRule = RepeatRule.valueOf(v)

    @TypeConverter fun fromReminderOwner(v: ReminderOwner): String = v.name
    @TypeConverter fun toReminderOwner(v: String): ReminderOwner = ReminderOwner.valueOf(v)

    @TypeConverter fun fromAlarmState(v: AlarmState): String = v.name
    @TypeConverter fun toAlarmState(v: String): AlarmState = AlarmState.valueOf(v)

    @TypeConverter fun fromCoinTier(v: CoinTier): String = v.name
    @TypeConverter fun toCoinTier(v: String): CoinTier = CoinTier.valueOf(v)

    @TypeConverter fun fromCoinSource(v: CoinSource): String = v.name
    @TypeConverter fun toCoinSource(v: String): CoinSource = CoinSource.valueOf(v)
}
