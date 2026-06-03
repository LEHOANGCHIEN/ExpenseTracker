package com.expensetracker.app.di

import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.CurrentUserProviderImpl
import com.expensetracker.app.data.repository.AuthRepositoryImpl
import com.expensetracker.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(impl: CurrentUserProviderImpl): CurrentUserProvider

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
