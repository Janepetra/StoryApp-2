package com.storyapp.retrofit

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.storyapp.db.local.entity.RemoteKeys
import com.storyapp.db.local.entity.Story
import com.storyapp.db.local.room.StoryDatabase

@ExperimentalPagingApi
class StoryRemoteMediator (
    private val db: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
): RemoteMediator<Int, Story>() {

    //The load function is the main entry point for loading data. It is called when the PagingSource needs to load data based on different load types.
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val data: List<Story>
            // Database transactions to update local database with fetched data and remote keys
            val responseData =
                apiService.getListStory("Bearer $token", page, state.config.pageSize, LOCATION )

            data = responseData.listStory

            // Handling pagination and updating remote keys in the local database
            val endOfPaginationReached = data.isEmpty()
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.getRemoteKeysDao().deleteRemoteKeys()
                    with(db) { getStoryDao().deleteAll() }
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = data.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                db.getRemoteKeysDao().insertAll(keys)
                db.getStoryDao().insertStory(data)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    //These functions are helpers to retrieve remote keys based on different scenarios
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            db.getRemoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            db.getRemoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                db.getRemoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val LOCATION = 0

    }
}