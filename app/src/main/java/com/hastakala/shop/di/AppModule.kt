package com.hastakala.shop.di

import android.content.Context
import androidx.room.Room
import com.hastakala.shop.data.local.AppDatabase
import com.hastakala.shop.data.local.dao.ProductDao
import com.hastakala.shop.data.local.dao.SaleDao
import com.hastakala.shop.data.local.dao.VariantDao
import com.hastakala.shop.network.ai.AiManager
import com.hastakala.shop.network.ai.AiManagerImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hasta_kala.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideVariantDao(database: AppDatabase): VariantDao = database.variantDao()

    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBindingModule {
    @Binds
    @Singleton
    abstract fun bindAiManager(impl: AiManagerImpl): AiManager
}
