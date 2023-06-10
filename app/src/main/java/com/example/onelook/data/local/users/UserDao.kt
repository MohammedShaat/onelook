package com.example.onelook.data.local.users

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: LocalUser)

    @Query("SELECT * FROM users WHERE id=:id")
    suspend fun getUser(id: Int): LocalUser

    @Delete
    suspend fun deleteUser(user: LocalUser)
}