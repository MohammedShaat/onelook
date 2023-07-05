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
    suspend fun getUserById(id: Int): LocalUser

    @Query("SELECT * FROM users WHERE firebase_uid=:firebaseUid")
    suspend fun getUserByFirebaseUid(firebaseUid: String): LocalUser

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<LocalUser>

    @Delete
    suspend fun deleteUser(user: LocalUser)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}