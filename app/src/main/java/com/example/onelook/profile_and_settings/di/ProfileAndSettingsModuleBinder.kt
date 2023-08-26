package com.example.onelook.profile_and_settings.di

import com.example.onelook.profile_and_settings.data.repository.ProfileRepositoryImpl
import com.example.onelook.profile_and_settings.doamin.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileAndSettingsModuleBinder {

    @Binds
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository
}