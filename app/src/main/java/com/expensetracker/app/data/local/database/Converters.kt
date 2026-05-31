package com.expensetracker.app.data.local.database

import androidx.room.TypeConverter
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.ChatRole
import com.expensetracker.app.domain.model.InsightType
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    // ---- LocalDate ↔ String ----
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    // ---- LocalDateTime ↔ String ----
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    // ---- List<String> ↔ String (JSON) ----
    @TypeConverter
    fun stringListToJson(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> =
        runCatching { Json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

    // ---- Enum converters ----
    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)

    @TypeConverter
    fun budgetPeriodToString(value: BudgetPeriod): String = value.name

    @TypeConverter
    fun stringToBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun recurrenceFrequencyToString(value: RecurrenceFrequency): String = value.name

    @TypeConverter
    fun stringToRecurrenceFrequency(value: String): RecurrenceFrequency =
        RecurrenceFrequency.valueOf(value)

    @TypeConverter
    fun chatRoleToString(value: ChatRole): String = value.name

    @TypeConverter
    fun stringToChatRole(value: String): ChatRole = ChatRole.valueOf(value)

    @TypeConverter
    fun insightTypeToString(value: InsightType): String = value.name

    @TypeConverter
    fun stringToInsightType(value: String): InsightType = InsightType.valueOf(value)
}
