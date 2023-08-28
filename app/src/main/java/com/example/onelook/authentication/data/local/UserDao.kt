package com.example.onelook.authentication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id=:id")
    suspend fun getUserById(id: Int): UserEntity

    @Query("SELECT * FROM users WHERE firebase_uid=:firebaseUid")
    suspend fun getUserByFirebaseUid(firebaseUid: String): UserEntity

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}