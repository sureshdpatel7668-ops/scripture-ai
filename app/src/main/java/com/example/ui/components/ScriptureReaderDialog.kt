package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.GeminiApiClient
import kotlinx.coroutines.launch

data class ParsedVerse(
    val verseNo: String,
    val original: String,
    val translation: String,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureReaderDialog(
    bookName: String,
    religionName: String,
    onDismiss: () -> Unit,
    onAddBookmark: (String, String, String, String) -> Unit,
    onAskAI: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedChapter by remember { mutableStateOf("1") }
    var selectedPart by remember { mutableStateOf("भाग 1 (Verse 1-15)") }
    var language by remember { mutableStateOf("Hindi") } // "Hindi" or "English"
    var fontSizeMultiplier by remember { mutableFloatStateOf(1f) } // 1.0 = standard, 1.2 = large, 1.4 = extra large

    var isLoading by remember { mutableStateOf(false) }
    var versesList by remember { mutableStateOf<List<ParsedVerse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCustomChapterInput by remember { mutableStateOf(false) }
    var customChapterText by remember { mutableStateOf("") }
    var viewPdfInApp by remember { mutableStateOf(false) }
    val pdfUrl = remember(bookName) { com.example.data.OfflineScriptures.getFullBookPdfUrl(bookName) }

    val popularChapters = listOf("1", "2", "3", "4", "5", "6", "7")
    val partsOptions = listOf("भाग 1 (Verse 1-15)", "भाग 2 (Verse 16-30)", "भाग 3 (Verse 31-45)", "भाग 4 (Verse 46-60)", "भाग 5 (Verse 61+)", "पूरा अध्याय")

    // Function to load scripture from Gemini API
    fun loadScripture() {
        isLoading = true
        errorMessage = null
        versesList = emptyList()

        coroutineScope.launch {
            // 1. Check pre-bundled offline scriptures first (completely offline, zero quota, instant load)
            val offlineContent = com.example.data.OfflineScriptures.getOfflineScripture(bookName, selectedChapter, selectedPart, language)
            if (offlineContent != null) {
                val parsed = parseResponse(offlineContent)
                if (parsed.isNotEmpty()) {
                    versesList = parsed
                    isLoading = false
                    return@launch
                }
            }

            // 2. Otherwise check local database cache
            val repository = com.example.ScriptureApplication.repository
            val cacheKey = "${bookName}_${selectedChapter}_${selectedPart}_${language}"
            try {
                val cachedText = repository.getCachedScripture(cacheKey)
                if (cachedText != null && cachedText.isNotBlank()) {
                    val parsed = parseResponse(cachedText)
                    if (parsed.isNotEmpty()) {
                        versesList = parsed
                        isLoading = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Fail-safe: continue to load from API if cache lookup errors
            }

            val bookSpecificInstructions = when {
                // Quran & Islamic
                bookName.contains("कुरान", ignoreCase = true) || bookName.contains("Quran", ignoreCase = true) || 
                religionName.contains("इस्लाम", ignoreCase = true) || religionName.contains("Islam", ignoreCase = true) -> {
                    """
                    SPECIAL RULES FOR ISLAMIC TEXTS (QURAN, HADITH):
                    - ORIGINAL: The original sacred Arabic text with full vowels/diacritics (harakat/erab) in Arabic script.
                    - TRANSLATION: The beautiful, authentic, and traditional Urdu translation (written in Urdu script).
                    - EXPLANATION: Detailed explanation containing BOTH a Hindi भावार्थ and an English Explanation, clearly labeled:
                      हिन्दी भावार्थ: [Detailed Hindi explanation of the verse, its meaning and context...]
                      English Explanation: [Detailed English explanation and context...]
                    """.trimIndent()
                }
                // Hindu, Buddhist, Jain scriptures (Sanskrit/Pali)
                bookName.contains("गीता", ignoreCase = true) || bookName.contains("Gita", ignoreCase = true) || 
                bookName.contains("वेद", ignoreCase = true) || bookName.contains("Veda", ignoreCase = true) || 
                bookName.contains("उपनिषद", ignoreCase = true) || bookName.contains("Upanishad", ignoreCase = true) || 
                bookName.contains("रामायण", ignoreCase = true) || bookName.contains("Ramayan", ignoreCase = true) || 
                bookName.contains("महाभारत", ignoreCase = true) || bookName.contains("Mahabharat", ignoreCase = true) || 
                bookName.contains("पुराण", ignoreCase = true) || bookName.contains("Puran", ignoreCase = true) || 
                bookName.contains("धम्मपद", ignoreCase = true) || bookName.contains("Dhammapada", ignoreCase = true) || 
                bookName.contains("त्रिपिटक", ignoreCase = true) || bookName.contains("Tripitaka", ignoreCase = true) || 
                bookName.contains("आगम", ignoreCase = true) || bookName.contains("Agam", ignoreCase = true) || 
                bookName.contains("तत्त्वार्थ", ignoreCase = true) || bookName.contains("Tattvartha", ignoreCase = true) || 
                bookName.contains("समयसार", ignoreCase = true) || bookName.contains("Samayasara", ignoreCase = true) -> {
                    """
                    SPECIAL RULES FOR INDIAN/DHARMIC TEXTS (SANSKRIT/PALI/PRAKRIT):
                    - ORIGINAL: The authentic original Sanskrit Shloka or Pali/Prakrit Gatha in Devanagari script.
                    - TRANSLATION: The most standard and faithful traditional Hindi translation (written in Devanagari).
                    - EXPLANATION: Detailed explanation containing BOTH a Hindi भावार्थ and an English Explanation, clearly labeled:
                      हिन्दी भावार्थ: [Detailed Hindi spiritual and philosophical commentary...]
                      English Explanation: [Detailed English spiritual and philosophical commentary...]
                    """.trimIndent()
                }
                // Sikh scriptures
                bookName.contains("ग्रंथ", ignoreCase = true) || bookName.contains("Granth", ignoreCase = true) || 
                religionName.contains("सिख", ignoreCase = true) || religionName.contains("Sikh", ignoreCase = true) -> {
                    """
                    SPECIAL RULES FOR SIKH TEXTS (GURU GRANTH SAHIB, DASAM GRANTH):
                    - ORIGINAL: The authentic original Gurbani text in its native Gurmukhi script.
                    - TRANSLATION: The beautiful and respectful translation in Hindi (Devanagari).
                    - EXPLANATION: Detailed explanation containing BOTH a Hindi भावार्थ and an English Explanation, clearly labeled:
                      हिन्दी भावार्थ: [Detailed Hindi moral and spiritual explanation of the Shabad...]
                      English Explanation: [Detailed English moral and spiritual explanation of the Shabad...]
                    """.trimIndent()
                }
                // Jewish & Christian (Bible, Torah, Talmud)
                bookName.contains("बाइबल", ignoreCase = true) || bookName.contains("Bible", ignoreCase = true) || 
                bookName.contains("तोराह", ignoreCase = true) || bookName.contains("Torah", ignoreCase = true) || 
                bookName.contains("तनाख", ignoreCase = true) || bookName.contains("Tanakh", ignoreCase = true) || 
                bookName.contains("तल्मूड", ignoreCase = true) || bookName.contains("Talmud", ignoreCase = true) -> {
                    """
                    SPECIAL RULES FOR ABRAHAMIC TEXTS (BIBLE, TORAH, TALMUD):
                    - ORIGINAL: The original sacred Biblical Hebrew (for Old Testament/Torah) or Greek (for New Testament) text.
                    - TRANSLATION: The accurate, elegant translation in $language.
                    - EXPLANATION: Detailed explanation containing BOTH a Hindi भावार्थ and an English Explanation, clearly labeled:
                      हिन्दी भावार्थ: [Detailed Hindi biblical commentary and moral lesson...]
                      English Explanation: [Detailed English biblical commentary and moral lesson...]
                    """.trimIndent()
                }
                // Default rule for all other scriptures (Tao, Shinto, Zoroastrian, Bahai, etc.)
                else -> {
                    """
                    SPECIAL RULES FOR SACRED TEXTS:
                    - ORIGINAL: The authentic original verse in its original native script (e.g., Classical Chinese for Tao Te Ching, Avestan for Avesta, Japanese/Kanji for Kojiki, etc.).
                    - TRANSLATION: Elegant and faithful translation in the reader's preferred language ($language).
                    - EXPLANATION: Detailed explanation containing BOTH a Hindi भावार्थ and an English Explanation, clearly labeled:
                      हिन्दी भावार्थ: [Detailed Hindi spiritual and ethical commentary...]
                      English Explanation: [Detailed English spiritual and ethical commentary...]
                    """.trimIndent()
                }
            }

            val rangeInstruction = if (selectedPart == "पूरा अध्याय") {
                "You MUST retrieve and generate ALL the verses of the selected chapter/section. Do not leave out any verses. If the chapter is long, provide the complete text from verse 1 to the very last verse of this chapter."
            } else {
                "The user has selected a specific part: $selectedPart. You MUST retrieve and generate only the verses within that range (e.g., if Part 1 is selected, retrieve verses 1 to 15; if Part 2, retrieve verses 16 to 30, and so on). Make sure to be highly accurate and detailed for that specific range."
            }

            val systemPrompt = """
                You are an expert theological translator, scholar, and scripture archivist.
                Your task is to retrieve, format, and present the actual sacred verses from the specified book and chapter/section with absolute accuracy, authenticity, and respect.
                
                The target book is: "$bookName" belonging to "$religionName".
                The requested chapter/section is: "$selectedChapter".
                The user wants the translation and explanation in: "$language".
                
                $bookSpecificInstructions
                
                $rangeInstruction
                
                Format your output EXACTLY in the following structured layout for each verse. Separate multiple verses with "===":
                
                VERSE_NO: [Verse Number or identifier, e.g. 1]
                ORIGINAL: [Original text in its authentic traditional script as specified above]
                TRANSLATION: [Accurate, elegant, and faithful translation of this verse as specified above]
                EXPLANATION: [Brief spiritual explanation, historical context, or moral lesson of the verse. Always provide BOTH a Hindi explanation (हिन्दी भावार्थ) AND an English explanation (English Explanation) as specified above]
                ===
                
                Important guidelines:
                1. Do NOT write any introductory remarks, conversation chat, greetings, or extra explanations before or after.
                2. Only output the structured verses separated by "===".
                3. Do NOT truncate or shorten the requested range or chapter. Retrieve all verses belonging to the requested chapter/section (or requested part range).
                4. Ensure the translation is authentic, dignified, and highly faithful to traditional interpretations.
                5. If the book or chapter is invalid or cannot be retrieved, output exactly: "ERROR: This chapter or scripture could not be retrieved. Please verify the chapter/section or try another."
            """.trimIndent()

            val queryText = "Please load verses for $bookName, chapter $selectedChapter ($selectedPart) in $language."

            try {
                val response = GeminiApiClient.generateContent(
                    model = "gemini-1.5-flash",
                    systemInstruction = systemPrompt,
                    history = listOf(Pair("user", queryText))
                )

                if (response.startsWith("Error:") || response.startsWith("ERROR:")) {
                    // Try fallback scripture database first before showing error
                    val fallbackContent = com.example.data.OfflineScriptures.getFallbackScripture(bookName, selectedChapter, selectedPart, language, response)
                    if (fallbackContent != null) {
                        val parsed = parseResponse(fallbackContent)
                        if (parsed.isNotEmpty()) {
                            versesList = parsed
                            errorMessage = null
                            return@launch
                        }
                    }
                    errorMessage = response.replace("Error:", "").replace("ERROR:", "").trim()
                } else {
                    val parsed = parseResponse(response)
                    if (parsed.isEmpty()) {
                        // Try fallback scripture database if parse failed
                        val fallbackContent = com.example.data.OfflineScriptures.getFallbackScripture(bookName, selectedChapter, selectedPart, language, null)
                        if (fallbackContent != null) {
                            val parsedFB = parseResponse(fallbackContent)
                            if (parsedFB.isNotEmpty()) {
                                versesList = parsedFB
                                errorMessage = null
                                return@launch
                            }
                        }
                        errorMessage = "सामग्री को पार्स करने में त्रुटि हुई। कृपया पुन: प्रयास करें।"
                    } else {
                        versesList = parsed
                        try {
                            repository.saveScriptureCache(cacheKey, response)
                        } catch (e: Exception) {
                            // Fail-safe: ignore cache write failures
                        }
                    }
                }
            } catch (e: Exception) {
                // Try fallback scripture database on network/API exception
                val fallbackContent = com.example.data.OfflineScriptures.getFallbackScripture(bookName, selectedChapter, selectedPart, language, e.localizedMessage)
                if (fallbackContent != null) {
                    val parsed = parseResponse(fallbackContent)
                    if (parsed.isNotEmpty()) {
                        versesList = parsed
                        errorMessage = null
                        return@launch
                    }
                }
                errorMessage = "नेटवर्क कनेक्शन में त्रुटि: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Trigger loading when chapter, language, or part changes
    LaunchedEffect(selectedChapter, language, selectedPart) {
        loadScripture()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- Header Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = bookName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$religionName • पवित्र ग्रंथ पाठक",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isOfflineCurrent = remember(bookName, selectedChapter, selectedPart, language) {
                    com.example.data.OfflineScriptures.getOfflineScripture(bookName, selectedChapter, selectedPart, language) != null
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(
                                if (isOfflineCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOfflineCurrent) Icons.Default.CheckCircle else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (isOfflineCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isOfflineCurrent) "ऑफ़लाइन मोड (Zero Data)" else "ऑनलाइन मोड (Gemini AI)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isOfflineCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (pdfUrl != null) {
                        Button(
                            onClick = {
                                viewPdfInApp = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = "PDF",
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "सम्पूर्ण पुस्तक PDF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // --- Chapter Selection & Configuration ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    // Chapter selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "अध्याय / भाग चुनें:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Language selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "भाषा:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = language == "Hindi",
                                onClick = { language = "Hindi" },
                                label = { Text("हिन्दी", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                            FilterChip(
                                selected = language == "English",
                                onClick = { language = "English" },
                                label = { Text("English", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Popular chapters chips and Custom selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        popularChapters.forEach { ch ->
                            val isSelected = selectedChapter == ch && !showCustomChapterInput
                            InputChip(
                                selected = isSelected,
                                onClick = {
                                    showCustomChapterInput = false
                                    selectedChapter = ch
                                },
                                label = { Text("अध्याय $ch") },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Custom chapter input trigger chip
                        InputChip(
                            selected = showCustomChapterInput,
                            onClick = { showCustomChapterInput = !showCustomChapterInput },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (showCustomChapterInput) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(if (showCustomChapterInput) "सहेजें" else "अन्य (Custom)")
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        )
                    }

                    if (showCustomChapterInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customChapterText,
                                onValueChange = { customChapterText = it },
                                placeholder = { Text("उदा. 12, या सूरा 114, या कांड 2") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                label = { Text("अध्याय / सूरा / कांड दर्ज करें") }
                            )
                            Button(
                                onClick = {
                                    if (customChapterText.isNotBlank()) {
                                        selectedChapter = customChapterText.trim()
                                        showCustomChapterInput = false
                                    }
                                }
                            ) {
                                Text("खोलें")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "भाग / श्लोक सीमा (Select Part / Range):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        partsOptions.forEach { opt ->
                            val isSelected = selectedPart == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPart = opt },
                                label = { Text(opt, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text-size and controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font Size",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "अक्षर का आकार:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { if (fontSizeMultiplier > 0.8f) fontSizeMultiplier -= 0.1f },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("A-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { if (fontSizeMultiplier < 1.6f) fontSizeMultiplier += 0.1f },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { fontSizeMultiplier = 1f },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Reset", fontSize = 10.sp)
                            }
                        }

                        // Refresh button
                        IconButton(
                            onClick = { loadScripture() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- Main Reading Content Box ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "पवित्र लाइब्रेरी से '$bookName' अध्याय $selectedChapter के पावन शब्द संकलित किए जा रहे हैं...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 22.sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "AI अनुवादक और स्कॉलर आपके लिए मूल ग्रंथ और भावार्थ तैयार कर रहे हैं।",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    } else if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                                MarkdownText(
                                    text = errorMessage!!,
                                    baseColor = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { loadScripture() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("पुन: प्रयास करें")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(versesList) { verse ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verse_item_${verse.verseNo}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        // Verse Number Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "पद / श्लोक ${verse.verseNo}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }

                                            // Action Buttons for this specific verse
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Quick Bookmark
                                                IconButton(
                                                    onClick = {
                                                        val fullVerseContent = if (verse.original.isNotBlank()) {
                                                            "${verse.original}\n\n${verse.translation}"
                                                        } else {
                                                            verse.translation
                                                        }
                                                        onAddBookmark(
                                                            bookName,
                                                            "अध्याय $selectedChapter, पद ${verse.verseNo}",
                                                            fullVerseContent,
                                                            "Saved from scripture reader"
                                                        )
                                                        Toast.makeText(context, "Bookmark Saved!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.BookmarkAdd,
                                                        contentDescription = "Save Verse",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                // Deep AI Study for this verse
                                                IconButton(
                                                    onClick = {
                                                        val query = "Analyze $bookName Chapter $selectedChapter Verse ${verse.verseNo} in deep detail. Verse content:\n${verse.translation}\n\nProvide the linguistic word study, cultural history, theological significance, and how we can apply it in modern life."
                                                        onAskAI(query)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = "Deep AI Analysis",
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // 1. Original Script block (if available)
                                        if (verse.original.isNotBlank() && verse.original != "N/A" && verse.original != "none") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = verse.original,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontStyle = FontStyle.Italic,
                                                        fontSize = (15.sp.value * fontSizeMultiplier).sp,
                                                        lineHeight = (22.sp.value * fontSizeMultiplier).sp,
                                                        fontFamily = FontFamily.Serif,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        // 2. Translation Block
                                        Text(
                                            text = verse.translation,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (14.sp.value * fontSizeMultiplier).sp,
                                                lineHeight = (21.sp.value * fontSizeMultiplier).sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // 3. Explanation Block
                                        if (verse.explanation.isNotBlank() && verse.explanation != "N/A" && verse.explanation != "none") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = if (language == "Hindi") "भावार्थ / व्याख्या" else "Explanation & Insight",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                com.example.ui.components.MarkdownText(
                                                    text = verse.explanation,
                                                    baseColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSizeMultiplier = fontSizeMultiplier
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewPdfInApp && pdfUrl != null) {
        InAppWebViewDialog(
            url = pdfUrl,
            title = "$bookName - सम्पूर्ण पुस्तक PDF",
            onDismiss = { viewPdfInApp = false }
        )
    }
}

// Function to parse Gemini response sequentially line by line
private fun parseResponse(rawResponse: String): List<ParsedVerse> {
    if (rawResponse.startsWith("ERROR:") || rawResponse.contains("could not be retrieved")) {
        return emptyList()
    }
    val list = mutableListOf<ParsedVerse>()
    
    var verseNo = ""
    var original = ""
    var translation = ""
    var explanation = ""
    var currentField = "" // "VERSE_NO", "ORIGINAL", "TRANSLATION", "EXPLANATION"

    val lines = rawResponse.split("\n")
    for (line in lines) {
        val trimmedLine = line.trim()
        
        // Skip separators or markdown divider lines
        if (trimmedLine == "===" || trimmedLine == "---" || trimmedLine.matches(Regex("^[=\\-*#\\s]+$"))) {
            continue
        }

        // Remove all occurrences of markdown bold/italic asterisks to handle e.g. **VERSE_NO:** or **ORIGINAL**:
        var cleanLine = trimmedLine.replace("**", "").replace("*", "").trim()
        
        // Clean common markdown prefix/formatting (lists, bullet points)
        if (cleanLine.startsWith("-") || cleanLine.startsWith("+") || cleanLine.startsWith("•")) {
            cleanLine = cleanLine.substring(1).trim()
        }
        cleanLine = cleanLine.trim()

        if (cleanLine.startsWith("VERSE_NO:", ignoreCase = true) || cleanLine.startsWith("VERSE NO:", ignoreCase = true) || cleanLine.startsWith("VERSE:", ignoreCase = true)) {
            // Save current accumulated verse before starting a new one
            if (verseNo.isNotEmpty() || translation.isNotEmpty()) {
                list.add(
                    ParsedVerse(
                        verseNo = verseNo.trim(),
                        original = original.trim(),
                        translation = translation.trim(),
                        explanation = explanation.trim()
                    )
                )
                verseNo = ""
                original = ""
                translation = ""
                explanation = ""
            }
            verseNo = if (cleanLine.startsWith("VERSE_NO:", ignoreCase = true)) {
                cleanLine.substringAfter("VERSE_NO:").trim()
            } else if (cleanLine.startsWith("VERSE NO:", ignoreCase = true)) {
                cleanLine.substringAfter("VERSE NO:").trim()
            } else {
                cleanLine.substringAfter("VERSE:").trim()
            }
            currentField = "VERSE_NO"
        } else if (cleanLine.startsWith("ORIGINAL:", ignoreCase = true)) {
            original = cleanLine.substringAfter("ORIGINAL:").trim()
            currentField = "ORIGINAL"
        } else if (cleanLine.startsWith("TRANSLATION:", ignoreCase = true)) {
            translation = cleanLine.substringAfter("TRANSLATION:").trim()
            currentField = "TRANSLATION"
        } else if (cleanLine.startsWith("EXPLANATION:", ignoreCase = true)) {
            explanation = cleanLine.substringAfter("EXPLANATION:").trim()
            currentField = "EXPLANATION"
        } else {
            // Continuation of current field
            if (trimmedLine.isNotEmpty()) {
                when (currentField) {
                    "VERSE_NO" -> verseNo += (if (verseNo.isEmpty()) "" else "\n") + trimmedLine
                    "ORIGINAL" -> original += (if (original.isEmpty()) "" else "\n") + trimmedLine
                    "TRANSLATION" -> translation += (if (translation.isEmpty()) "" else "\n") + trimmedLine
                    "EXPLANATION" -> explanation += (if (explanation.isEmpty()) "" else "\n") + trimmedLine
                }
            }
        }
    }

    // Add final verse
    if (verseNo.isNotEmpty() || translation.isNotEmpty()) {
        list.add(
            ParsedVerse(
                verseNo = verseNo.trim(),
                original = original.trim(),
                translation = translation.trim(),
                explanation = explanation.trim()
            )
        )
    }

    return list
}
