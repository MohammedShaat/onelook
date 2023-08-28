package com.example.onelook.authentication.di

import com.example.onelook.authentication.data.repository.AuthenticationRepositoryImpl
import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModuleBinder {

    @Binds
    abstract fun provideUserDao(userRepositoryImpl: AuthenticationRepositoryImpl): AuthenticationRepository
}