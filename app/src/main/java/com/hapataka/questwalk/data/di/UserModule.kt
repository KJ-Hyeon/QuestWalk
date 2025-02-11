package com.hapataka.questwalk.data.di

import com.hapataka.questwalk.data.datasource.remote.FirebaseUserRDS
import com.hapataka.questwalk.data.repository.ImageRepositoryImpl
import com.hapataka.questwalk.data.repository.UserRepositoryImpl
import com.hapataka.questwalk.data.repository.backup.UserRepoImpl
import com.hapataka.questwalk.domain.data.remote.UserRDS
import com.hapataka.questwalk.domain.repository.ImageRepository
import com.hapataka.questwalk.domain.repository.UserRepo
import com.hapataka.questwalk.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {
    @Binds
    abstract fun provideImageRepository(imageRepo: ImageRepositoryImpl): ImageRepository

    @Binds
    abstract fun provideUserRepo(userRepoImpl: UserRepoImpl): UserRepo

    @Binds
    @Named("FirebaseUserRDS")
    abstract fun provideUserRDS(userRDS: FirebaseUserRDS): UserRDS

    @Binds
    abstract fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository


}