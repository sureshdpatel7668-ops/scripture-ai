package com.example.data.repository

import com.example.data.db.BookmarkedVerse
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedChatSession
import com.example.data.db.ScriptureCache
import com.example.data.db.ScriptureDao
import kotlinx.coroutines.flow.Flow

class ScriptureRepository(private val scriptureDao: ScriptureDao) {

    suspend fun getCachedScripture(cacheKey: String): String? {
        return scriptureDao.getCache(cacheKey)?.responseText
    }

    suspend fun saveScriptureCache(cacheKey: String, responseText: String) {
        scriptureDao.insertCache(ScriptureCache(cacheKey, responseText))
    }

    val bookmarks: Flow<List<BookmarkedVerse>> = scriptureDao.getAllBookmarks()

    suspend fun addBookmark(verse: BookmarkedVerse) {
        scriptureDao.insertBookmark(verse)
    }

    suspend fun deleteBookmark(id: Int) {
        scriptureDao.deleteBookmark(id)
    }

    val chatSessions: Flow<List<SavedChatSession>> = scriptureDao.getAllChatSessions()

    suspend fun addChatSession(session: SavedChatSession) {
        scriptureDao.insertChatSession(session)
    }

    suspend fun deleteChatSession(sessionId: String) {
        scriptureDao.deleteChatSession(sessionId)
        scriptureDao.clearMessagesForSession(sessionId)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return scriptureDao.getMessagesForSession(sessionId)
    }

    suspend fun addMessage(message: ChatMessageEntity) {
        scriptureDao.insertChatMessage(message)
    }

    suspend fun clearMessagesForSession(sessionId: String) {
        scriptureDao.clearMessagesForSession(sessionId)
    }
}
