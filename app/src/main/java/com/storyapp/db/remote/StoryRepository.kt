package com.storyapp.db.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.storyapp.db.local.entity.Story
import com.storyapp.db.local.room.StoryDatabase
import com.storyapp.response.AddStory
import com.storyapp.response.AllStories
import com.storyapp.response.Login
import com.storyapp.response.Signup
import com.storyapp.retrofit.ApiConfig
import com.storyapp.retrofit.ApiService
import com.storyapp.retrofit.StoryRemoteMediator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) {
    private val _login = MutableLiveData<Login>()
    val login: LiveData<Login> = _login

    private val _signup = MutableLiveData<Signup>()
    val signup: LiveData<Signup> = _signup

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addStory = MutableLiveData<AddStory>()
    val addStory: LiveData<AddStory> = _addStory

    val listStory = MutableLiveData<List<Story>>()

    var error = MutableLiveData("")

    private val _messageLogin = MutableLiveData<String>()

    companion object {
        private const val TAG = "StoryViewModel"
    }

    fun getLogin (email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getLogin(email, password)
        client.enqueue(object: Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let {
                        _login.value = it
                    }
                } else {
                    when (response.code()) {
                        400 -> _messageLogin.value = ("Invalid Email format")
                        401 -> _messageLogin.value = ("Wrong email or password")
                        else -> Log.e(TAG, "Error Message: ${response.message()}")
                    }
                }
            }
            override fun onFailure(call: Call<Login>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun getSignup (name: String, email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getSignup(name, email, password)
        client.enqueue(object: Callback<Signup> {
            override fun onResponse(call: Call<Signup>, response: Response<Signup>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let {
                        _signup.value = it
                    }
                } else {
                    when (response.code()) {
                        400 -> _messageLogin.value = ("Invalid Email format")
                        401 -> _messageLogin.value = ("Wrong email or password")
                        else -> Log.e(TAG, "Error Message: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<Signup>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
    }

    fun getListStory(token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getLocStory(token, 32, 1)
        client.enqueue(object : Callback<AllStories> {
            override fun onResponse(call: Call<AllStories>, response: Response<AllStories>) {
                if (response.isSuccessful) {
                    listStory.postValue(response.body()?.listStory)
                } else {
                    error.postValue("ERROR ${response.code()} : ${response.message()}")
                }
                _isLoading.value = false
            }

            override fun onFailure(call: Call<AllStories>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Call: ${t.message}")
                error.postValue("${"error di get list story"} : ${t.message}")
            }
        })
    }

    fun addStory(token: String, image: File, description: String, lat: RequestBody? = null, lon: RequestBody? = null) {
        _isLoading.value = true
        "${image.length() / 1024 / 1024} MB" // manual parse from bytes to Mega Bytes
        //convert desc to requestBody format
        val storyDescription = description.toRequestBody("text/plain".toMediaType())
        //convert photo to requestBody format
        val requestImageFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
        //make multipartBody for photo
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            image.name,
            requestImageFile
        )
        val client = ApiConfig.getApiService().addStory(token, imageMultipart, storyDescription, lat, lon)
        client.enqueue(object : Callback<AddStory> {
            override fun onResponse(call: Call<AddStory>, response: Response<AddStory>) {
                when (response.code()) {
                    201 -> _addStory.value
                    else -> error.postValue("Error ${response.code()} : ${response.message()}")
                }
                _isLoading.value = false
            }

            override fun onFailure(call: Call<AddStory>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Call: ${t.message}")
                error.postValue("${"error di add story"} : ${t.message}")
            }
        })
    }
    @ExperimentalPagingApi
    fun getPagingStory(token: String): LiveData<PagingData<com.storyapp.db.local.entity.Story>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.getStoryDao().getListStory()
            }
        )
        return pager.liveData
    }
}