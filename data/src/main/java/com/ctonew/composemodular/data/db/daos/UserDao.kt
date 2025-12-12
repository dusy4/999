package com.ctonew.composemodular.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ctonew.composemodular.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
