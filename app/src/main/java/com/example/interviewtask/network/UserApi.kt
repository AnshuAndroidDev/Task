package com.example.interviewtask.network

import com.example.interviewtask.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("users")
    suspend fun loginUser(@Query("page") page: Int): Response<UserResponse>


    companion object {
        fun getApi(): UserApi? {
            return ApiClient.client?.create(UserApi::class.java)
        }
    }
}