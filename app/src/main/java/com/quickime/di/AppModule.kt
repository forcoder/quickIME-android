package com.quickime.di

import android.content.Context
import androidx.room.Room
import com.quickime.core.ai.InferenceEngine
import com.quickime.core.ai.AiInferenceEngine
import com.quickime.core.kb.KbDatabase
import com.quickime.core.kb.KnowledgeBase
import com.quickime.core.cs.CustomerServiceManager
import com.quickime.core.wubi.WubiEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKbDatabase(@ApplicationContext context: Context): KbDatabase {
        return Room.databaseBuilder(
            context,
            KbDatabase::class.java,
            KbDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideKnowledgeBase(
        @ApplicationContext context: Context
    ): KnowledgeBase {
        return KnowledgeBase(context)
    }

    @Provides
    @Singleton
    fun provideAiEngine(
        @ApplicationContext context: Context
    ): InferenceEngine {
        return AiInferenceEngine(context)
    }

    @Provides
    @Singleton
    fun provideWubiEngine(): WubiEngine {
        return WubiEngine()
    }

    @Provides
    @Singleton
    fun provideCustomerServiceManager(
        knowledgeBase: KnowledgeBase,
        aiEngine: InferenceEngine
    ): CustomerServiceManager {
        return CustomerServiceManager(knowledgeBase, aiEngine)
    }
}
