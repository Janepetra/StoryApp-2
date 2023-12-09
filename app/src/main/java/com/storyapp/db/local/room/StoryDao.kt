package com.storyapp.db.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.storyapp.db.local.entity.Story

@Dao
interface StoryDao {
    //jika ada konflik (cerita dengan kunci utama yang sama sudah ada),
    // data cerita yang baru akan menggantikan data yang ada.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<Story>)

    @Query("SELECT * FROM story")
    fun getListStory(): PagingSource<Int, Story>

    @Query("DELETE FROM story")
    fun deleteAll()
}