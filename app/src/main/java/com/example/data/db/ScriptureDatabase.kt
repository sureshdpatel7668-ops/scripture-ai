package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarked_verses")
data class BookmarkedVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scriptureName: String,
    val reference: String,
    val text: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class SavedChatSession(
    @PrimaryKey val sessionId: String,
    val title: String,
    val role: String,
    val model: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scripture_cache")
data class ScriptureCache(
    @PrimaryKey val cacheKey: String,
    val responseText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScriptureDao {
    @Query("SELECT * FROM scripture_cache WHERE cacheKey = :cacheKey")
    suspend fun getCache(cacheKey: String): ScriptureCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ScriptureCache)

    @Query("SELECT * FROM bookmarked_verses ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(verse: BookmarkedVerse)

    @Query("DELETE FROM bookmarked_verses WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllChatSessions(): Flow<List<SavedChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: SavedChatSession)

    @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteChatSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: String)
}

@Database(entities = [BookmarkedVerse::class, SavedChatSession::class, ChatMessageEntity::class, ScriptureCache::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptureDao(): ScriptureDao
}
