package com.expensetracker.app.domain.model

import java.time.LocalDateTime

data class Wallet(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val initialBalance: Double,
    val currency: String,
    val createdAt: LocalDateTime,
)
