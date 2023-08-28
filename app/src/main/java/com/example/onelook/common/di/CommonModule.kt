package com.example.onelook.common.di

import android.content.Context
import androidx.room.Room
import com.example.onelook.common.data.local.OneLookDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OtherModule {

    @ApplicationCoroutine
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideOneLookDatabase(@ApplicationContext context: Context): OneLookDatabase {
        synchronized(this) {
            return Room.databaseBuilder(
                context.applicationContext,
                OneLookDatabase::class.java,
                "OneLook.db"
            ).build()

        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationCoroutine