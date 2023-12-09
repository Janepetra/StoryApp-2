package com.storyapp.retrofit

import com.storyapp.response.AddStory
import com.storyapp.response.AllStories
import com.storyapp.response.Login
import com.storyapp.response.Signup
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
     fun getSignup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Signup>

    @FormUrlEncoded
    @POST("login")
    fun getLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Login>

    @GET("stories")
    suspend fun getListStory(
        @Header("Authorization") token:String,
        @Query("size") size:Int,
        @Query("page") page: Int,
        @Query("location") location: Int
    ): AllStories

    @GET("stories")
    fun getLocStory(
        @Header("Authorization") token:String,
        @Query("size") size:Int,
        @Query("location") location: Int
    ): Call<AllStories>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token:String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<AddStory>
}
