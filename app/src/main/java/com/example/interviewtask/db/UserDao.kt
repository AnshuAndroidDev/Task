package com.example.interviewtask.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.interviewtask.model.UserResponse

class UserDao(context: Context) {

    private val dbHelper = UserDatabaseHelper(context)

    fun insertUser(user: UserResponse.UserData) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COLUMN_ID, user.id)
            put(UserDatabaseHelper.COLUMN_FIRST_NAME, user.firstName)
            put(UserDatabaseHelper.COLUMN_LAST_NAME, user.lastName)
            put(UserDatabaseHelper.COLUMN_EMAIL, user.email)
            put(UserDatabaseHelper.COLUMN_AVATAR, user.avatar)
        }

        db.insert(UserDatabaseHelper.TABLE_USERS, null, values)  // Correctly reference TABLE_USERS
        db.close()
    }

    fun getAllUsers(): List<UserResponse.UserData> {
        val userList = mutableListOf<UserResponse.UserData>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * FROM ${UserDatabaseHelper.TABLE_USERS}", null)  // Correctly reference TABLE_USERS

        if (cursor.moveToFirst()) {
            do {
                val user = UserResponse.UserData(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ID)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_LAST_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_EMAIL)),
                    avatar = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_AVATAR))
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userList
    }
}
