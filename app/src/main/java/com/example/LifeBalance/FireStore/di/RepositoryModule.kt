package com.example.LifeBalance.FireStore.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.LifeBalance.FireStore.Repository.Repository
import com.example.LifeBalance.FireStore.Repository.RepositoryImp
import com.google.firebase.firestore.CollectionReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@RequiresApi(Build.VERSION_CODES.O)
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(
        database: CollectionReference,
    ): Repository {
        return RepositoryImp(database)
    }
}