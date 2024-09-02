package com.example.interviewtask.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.interviewtask.db.UserDatabaseHelper
import com.example.interviewtask.model.UserResponse
import com.example.interviewtask.network.UserApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = UserDatabaseHelper(application)
    private val _userList = MutableLiveData<List<UserResponse.UserData>>()
    val userList: LiveData<List<UserResponse.UserData>> get() = _userList
    private val userApi = UserApi.getApi()

    // Method to fetch users from API and save to SQLite
    fun showUser(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<UserResponse>? = userApi?.loginUser(page)
                if (response?.isSuccessful == true) {
                    response.body()?.data?.let { apiData ->
                        fetchUsersFromApi(apiData)
                    }
                } else {
                    // Handle API error
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fetch users from API and save to SQLite
    fun fetchUsersFromApi(apiData: List<UserResponse.UserData>) {
        _userList.postValue(apiData)
        // Save API data to SQLite
        for (user in apiData) {
            databaseHelper.addUser(user)
        }
    }

    // Add a new user to SQLite and refresh the user list
    fun addUserToDatabase(user: UserResponse.UserData) {
        CoroutineScope(Dispatchers.IO).launch {
            databaseHelper.addUser(user)
            // Load the updated user list from SQLite
            _userList.postValue(databaseHelper.getAllUsers())
        }
    }

    // Load all users from SQLite and update the LiveData
    fun loadAllUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            _userList.postValue(databaseHelper.getAllUsers())
        }
    }
}
