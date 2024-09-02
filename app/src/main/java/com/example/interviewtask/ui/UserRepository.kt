package com.example.interviewtask.ui

import com.example.interviewtask.model.UserResponse
import com.example.interviewtask.network.UserApi
import retrofit2.Response

class UserRepository {

    suspend fun showUser(loginRequest: Int): Response<UserResponse>? {
        return  UserApi.getApi()?.loginUser(loginRequest)
    }
}