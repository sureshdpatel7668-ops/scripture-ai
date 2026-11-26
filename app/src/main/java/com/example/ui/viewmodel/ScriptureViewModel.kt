package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.BookmarkedVerse
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedChatSession
import com.example.data.repository.ScriptureRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ScriptureViewModel(private val repository: ScriptureRepository) : ViewModel() {

    // --- State ---
    val bookmarks: StateFlow<List<BookmarkedVerse>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatSessions: StateFlow<List<SavedChatSession>> = repository.chatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSession = MutableStateFlow<SavedChatSession?>(null)
    val currentSession: StateFlow<SavedChatSession?> = _currentSession.asStateFlow()

    val currentSessionMessages: StateFlow<List<ChatMessageEntity>> = _currentSession
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(emptyList())
            } else {
                repository.getMessagesForSession(session.sessionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inputText = MutableStateFlow("")
    val isGenerating = MutableStateFlow(false)

    val selectedModel = MutableStateFlow("gemini-1.5-flash")
    val selectedRole = MutableStateFlow("Theological Scholar")

    val errorState = MutableStateFlow<String?>(null)

    // --- Active Search / Discover AI tool ---
    val aiSearchQuery = MutableStateFlow("")
    val aiSearchResult = MutableStateFlow<String?>(null)
    val isSearchingAI = MutableStateFlow(false)

    // --- Actions ---

    fun selectSession(session: SavedChatSession?) {
        _currentSession.value = session
        if (session != null) {
            selectedModel.value = session.model
            selectedRole.value = session.role
        }
    }

    fun startNewSession(title: String, role: String, model: String) {
        val sessionId = UUID.randomUUID().toString()
        val session = SavedChatSession(
            sessionId = sessionId,
            title = title.ifBlank { "$role Session" },
            role = role,
            model = model,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.addChatSession(session)
            _currentSession.value = session
            selectedModel.value = model
            selectedRole.value = role

            // Add an initial greeting from the AI based on its role
            val greeting = getInitialGreeting(role)
            repository.addMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    sender = "assistant",
                    text = greeting,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            if (_currentSession.value?.sessionId == sessionId) {
                _currentSession.value = null
            }
            repository.deleteChatSession(sessionId)
        }
    }

    fun sendMessage() {
        val text = inputText.value.trim()
        val session = _currentSession.value
        if (text.isEmpty() || session == null || isGenerating.value) return

        inputText.value = ""
        errorState.value = null

        viewModelScope.launch {
            // 1. Add user message
            val userMsg = ChatMessageEntity(
                sessionId = session.sessionId,
                sender = "user",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            repository.addMessage(userMsg)

            // 2. Fetch entire history to send to Gemini
            isGenerating.value = true
            val currentList = currentSessionMessages.value
            // Ensure userMsg is included in the history we send to the API, since DB Flow update is async
            val historyEntities = if (currentList.any { it.sender == "user" && it.text == text }) {
                currentList
            } else {
                currentList + userMsg
            }
            val apiHistory = historyEntities.map { Pair(it.sender, it.text) }

            val systemInstruction = getSystemInstruction(session.role)

            // 3. Call API
            val response = GeminiApiClient.generateContent(
                model = session.model,
                systemInstruction = systemInstruction,
                history = apiHistory
            )

            // 4. Add assistant response
            val assistantMsg = ChatMessageEntity(
                sessionId = session.sessionId,
                sender = "assistant",
                text = response,
                timestamp = System.currentTimeMillis()
            )
            repository.addMessage(assistantMsg)
            isGenerating.value = false
        }
    }

    // --- Bookmarks ---

    fun addBookmark(scriptureName: String, reference: String, text: String, notes: String) {
        viewModelScope.launch {
            repository.addBookmark(
                BookmarkedVerse(
                    scriptureName = scriptureName,
                    reference = reference,
                    text = text,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateBookmarkNotes(bookmark: BookmarkedVerse, newNotes: String) {
        viewModelScope.launch {
            repository.addBookmark(bookmark.copy(notes = newNotes))
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    // --- AI Discover / Find Scripture ---

    fun findScriptureViaAI() {
        val query = aiSearchQuery.value.trim()
        if (query.isEmpty() || isSearchingAI.value) return

        isSearchingAI.value = true
        aiSearchResult.value = null

        viewModelScope.launch {
            val systemPrompt = """
                You are a scripture database assistant. Locate, extract, and present a beautiful, accurate, and inspiring scripture verse or passage matching the user's topic, emotion, or specific citation request.
                
                Respond in EXACTLY the following format:
                TRADITION: [Name of Sacred Tradition, e.g. Bible, Quran, Bhagavad Gita, etc.]
                CITATION: [Book Chapter:Verse, e.g. John 14:27]
                VERSE: [The actual scripture text in full]
                CONTEXT: [A 1-2 sentence description of the theological context or spiritual meaning of this verse]
                
                Be extremely respectul, authentic, and accurate. Do not make up scripture. If you cannot find a precise match, provide the closest known scripture from a major sacred tradition.
            """.trimIndent()

            val response = GeminiApiClient.generateContent(
                model = "gemini-1.5-flash",
                systemInstruction = systemPrompt,
                history = listOf(Pair("user", "Find an inspiring scripture about: $query"))
            )

            aiSearchResult.value = response
            isSearchingAI.value = false
        }
    }

    fun saveDiscoverResultToBookmarks(notes: String) {
        val raw = aiSearchResult.value ?: return
        try {
            var tradition = "Scripture"
            var citation = "Reference"
            var verseText = ""

            val lines = raw.split("\n")
            for (line in lines) {
                if (line.startsWith("TRADITION:", ignoreCase = true)) {
                    tradition = line.substringAfter("TRADITION:").trim()
                } else if (line.startsWith("CITATION:", ignoreCase = true)) {
                    citation = line.substringAfter("CITATION:").trim()
                } else if (line.startsWith("VERSE:", ignoreCase = true)) {
                    verseText = line.substringAfter("VERSE:").trim()
                } else if (verseText.isNotEmpty() && !line.startsWith("CONTEXT:", ignoreCase = true)) {
                    // Collect multiline verse text
                    verseText += "\n" + line
                } else if (line.startsWith("CONTEXT:", ignoreCase = true)) {
                    break
                }
            }

            if (verseText.isEmpty()) {
                verseText = raw
            }

            addBookmark(
                scriptureName = tradition,
                reference = citation,
                text = verseText,
                notes = notes
            )
            aiSearchResult.value = null
            aiSearchQuery.value = ""
        } catch (e: Exception) {
            // Fallback: save raw text
            addBookmark(
                scriptureName = "AI Search",
                reference = "Result",
                text = raw,
                notes = notes
            )
            aiSearchResult.value = null
            aiSearchQuery.value = ""
        }
    }

    // --- Helpers ---

    private fun getInitialGreeting(role: String): String {
        return when (role) {
            "Theological Scholar" -> "Greetings, seeker of truth. I am your theological scholar. Let us embark on a rigorous and respectful exploration of sacred texts, examining their deep theological structures, parallels, and interpretations. What text or topic shall we study today?"
            "Historical Analyst" -> "Hello. As an ancient history scholar, I am here to help you unpack the cultural, political, and historical contexts of scripture. We will explore the ancient world of the authors and their original audiences. Which passage should we contextualize?"
            "Spiritual Companion" -> "Peace be with you. I am here as a gentle, encouraging mentor to walk alongside you in your scripture journey. Let us look at how these timeless teachings can guide, comfort, and inspire your daily path. What are you holding in your heart today?"
            "Language Translator" -> "Welcome. I specialize in the philological study and translation of ancient biblical and sacred languages. We can dissect original terms (Greek, Hebrew, Sanskrit, Arabic, etc.), explore alternative readings, and analyze the poetry of the source. Which verse shall we translate?"
            else -> "Hello. I am here to assist you in your scripture study. How can I help you today?"
        }
    }

    private fun getSystemInstruction(role: String): String {
        val baseRule = " Answer any question the user asks. If the user asks general, historical, scientific, or personal questions, you MUST answer them clearly and helpfully. Never refuse to answer, and do not say you cannot discuss certain topics. Always be respectful, informative, and encouraging."
        return when (role) {
            "Theological Scholar" -> "You are an elite scripture scholar and theologian. Your role is to analyze sacred texts deeply, exploring theology, word studies, parallel interpretations, and doctrinal implications. Provide comprehensive, respectful, and scholarly insights. Use markdown to highlight key concepts.$baseRule"
            "Historical Analyst" -> "You are an expert ancient historian. Your role is to examine sacred texts from an archaeological, historical, and cultural standpoint. Explain what the verses meant to the original ancient audience, the cultural context of the era, and historical events surrounding them. Keep your tone objective, educational, and grounded in historical consensus.$baseRule"
            "Spiritual Companion" -> "You are an encouraging, empathetic spiritual mentor. Your role is to help the reader apply scripture to daily life, offering words of comfort, wisdom, ethical reflections, and encouragement. Keep answers uplifting, practical, compassionate, and focused on personal growth and ethics.$baseRule"
            "Language Translator" -> "You are a philologist and language translator specializing in ancient sacred languages. Your role is to dissect translations, explaining root words (like Hebrew, Greek, Sanskrit, Arabic, etc.), historical idioms, alternative translation options, and poetic devices. Provide clear root word breakdowns and compare different translations.$baseRule"
            else -> "You are a helpful and respectful scripture study assistant.$baseRule"
        }
    }
}

class ScriptureViewModelFactory(private val repository: ScriptureRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScriptureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScriptureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
