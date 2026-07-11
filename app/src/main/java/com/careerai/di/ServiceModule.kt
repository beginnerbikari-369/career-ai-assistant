package com.careerai.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    // All services are provided via @Inject constructors
    // This module can be extended if we need to provide interfaces or complex dependencies
}