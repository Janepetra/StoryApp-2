package com.storyapp.di

import android.content.Context
import com.storyapp.db.local.room.StoryDatabase
import com.storyapp.db.remote.StoryRepository
import com.storyapp.retrofit.ApiConfig

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}