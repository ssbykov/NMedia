package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.StateType

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    suspend fun getAllsync(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id =:id")
    suspend fun removeById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               shares = shares + 1
           WHERE id = :id;
        """
    )
    suspend fun shareById(id: Long)

    @Query(
        """SELECT * FROM PostEntity WHERE id = :id;"""
    )
    suspend fun getById(id: Long): PostEntity

    @Query(
        """SELECT MAX(id) FROM PostEntity;"""
    )
    suspend fun getLastId(): Long?
}