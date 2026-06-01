package com.expensetracker.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * GeminiApiService is provided via @Singleton @Inject constructor() — no explicit @Provides needed.
 * This module is kept as a placeholder for future network dependencies (e.g., Retrofit, OkHttp).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule
