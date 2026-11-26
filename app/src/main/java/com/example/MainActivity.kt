package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ReligionsData
import com.example.data.Religion
import com.example.ui.components.ReligionDashboardDialog
import com.example.ui.components.ScriptureReaderDialog
import com.example.ui.components.EBooksScreen
import com.example.data.db.BookmarkedVerse
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedChatSession
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScriptureViewModel
import com.example.ui.viewmodel.ScriptureViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// --- Pre-defined Scriptures for "Verse of the Day" ---
data class DayVerse(
    val tradition: String,
    val reference: String,
    val text: String
)

val dayVerses = listOf(
    DayVerse("Bible", "John 14:27", "Peace I leave with you; my peace I give to you. Not as the world gives do I give to you. Let not your hearts be troubled, neither let them be afraid."),
    DayVerse("Quran", "Al-Baqarah 2:286", "Allah does not burden a soul beyond that it can bear. It will have [the consequence of] what [good] it has gained, and it will bear [the consequence of] what [evil] it has earned."),
    DayVerse("Bhagavad Gita", "Chapter 2, Verse 47", "You have a right to perform your prescribed duties, but you are not entitled to the fruits of your actions. Never consider yourself to be the cause of the results of your activities, nor be attached to inactive duty."),
    DayVerse("Torah", "Numbers 6:24-26", "The Lord bless you and keep you; the Lord make his face shine upon you and be gracious to you; the Lord turn his face toward you and give you peace."),
    DayVerse("Dhammapada", "Verse 1-2", "Mind precedes all mental states. Mind is their chief; they are mind-made. If one speaks or acts with an impure mind, suffering follows him. If one speaks or acts with a pure mind, happiness follows him."),
    DayVerse("Rigveda", "10.191.2", "Sangachhadhwam samvadadhwam sam vo manamsi janatam. (Meet together, speak together, let your minds be of one accord in understanding.)"),
    DayVerse("Hadith", "Sahih Bukhari", "None of you truly believes until he loves for his brother what he loves for himself."),
    DayVerse("Upanishads", "Isha Upanishad 1.1", "All this, whatever moves in this moving world, is enveloped by God. Find your enjoyment in renunciation; do not covet what belongs to others."),
    DayVerse("Guru Granth Sahib", "Page 1349", "Recognize the Lord's Light within all, and do not consider social class or status; there are no classes or castes in the world hereafter."),
    DayVerse("Bible", "Matthew 5:9", "Blessed are the peacemakers, for they shall be called sons of God."),
    DayVerse("Quran", "Al-Hujurat 49:13", "O mankind, indeed We have created you from male and female and made you peoples and tribes that you may know one another."),
    DayVerse("Bhagavad Gita", "Chapter 6, Verse 5", "Elevate yourself through the power of your mind, and do not degrade yourself. For the mind can be the friend and also the enemy of the self."),
    DayVerse("Dhammapada", "Verse 5", "Hatred is never appeased by hatred in this world. By non-hatred alone is hatred appeased. This is a law eternal."),
    DayVerse("Guru Granth Sahib", "Page 1", "Truth is the highest virtue, but higher still is truthful living."),
    DayVerse("Mahabharata", "Shanti Parva", "One should never do that to another which one regards as injurious to one's own self. This, in brief, is the rule of righteousness.")
)

val welcomeSubtitles = listOf(
    "Explore the timeless wisdom of sacred texts with AI.",
    "Let the profound words of scripture illuminate your path today.",
    "Seek wisdom, discover peace, and understand ancient traditions.",
    "Deepen your spiritual path through thoughtful study and AI guidance.",
    "Universal truths and inspiring verses for your daily reflection.",
    "Connect with the sacred text and unravel its layers of meaning.",
    "A quiet space for reflection, translation, and ancient knowledge.",
    "Journey through the world's most sacred literatures with respect."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val repository = ScriptureApplication.repository
    val viewModel: ScriptureViewModel = viewModel(
        factory = ScriptureViewModelFactory(repository)
    )

    val currentScreen = remember { mutableStateOf("home") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe DB States
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val currentSessionMessages by viewModel.currentSessionMessages.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()

    // Manage add bookmark dialog state
    var showAddBookmarkDialog by remember { mutableStateOf(false) }

    // Select a verse of the day based on day of year (changes dynamically every 24 hours)
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val dailyVerse = dayVerses[dayOfYear % dayVerses.size]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCRIPTURE AI",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (currentScreen.value == "bookmarks") {
                        IconButton(
                            onClick = { showAddBookmarkDialog = true },
                            modifier = Modifier.testTag("add_bookmark_top_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Bookmark")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen.value == "home",
                    onClick = { currentScreen.value = "home" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = currentScreen.value == "chat",
                    onClick = { currentScreen.value = "chat" },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
                    label = { Text("Study Chat") },
                    modifier = Modifier.testTag("tab_chat")
                )
                NavigationBarItem(
                    selected = currentScreen.value == "ebooks",
                    onClick = { currentScreen.value = "ebooks" },
                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = null) },
                    label = { Text("E-Books") },
                    modifier = Modifier.testTag("tab_ebooks")
                )
                NavigationBarItem(
                    selected = currentScreen.value == "bookmarks",
                    onClick = { currentScreen.value = "bookmarks" },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    label = { Text("Bookmarks") },
                    modifier = Modifier.testTag("tab_bookmarks")
                )
                NavigationBarItem(
                    selected = currentScreen.value == "history",
                    onClick = { currentScreen.value = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen.value) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    dailyVerse = dailyVerse,
                    bookmarks = bookmarks,
                    onNavigateToChat = { currentScreen.value = "chat" }
                )
                "chat" -> ChatScreen(
                    viewModel = viewModel,
                    currentSession = currentSession,
                    messages = currentSessionMessages
                )
                "ebooks" -> EBooksScreen()
                "bookmarks" -> BookmarksScreen(
                    viewModel = viewModel,
                    bookmarks = bookmarks
                )
                "history" -> HistoryScreen(
                    viewModel = viewModel,
                    sessions = chatSessions,
                    onSessionSelected = { currentScreen.value = "chat" }
                )
            }

            // Dialog for manual bookmark creation
            if (showAddBookmarkDialog) {
                AddBookmarkDialog(
                    onDismiss = { showAddBookmarkDialog = false },
                    onConfirm = { tradition, citation, text, notes ->
                        viewModel.addBookmark(tradition, citation, text, notes)
                        showAddBookmarkDialog = false
                        Toast.makeText(context, "Bookmark Saved!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME / DASHBOARD SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: ScriptureViewModel,
    dailyVerse: DayVerse,
    bookmarks: List<BookmarkedVerse>,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val aiSearchQuery by viewModel.aiSearchQuery.collectAsStateWithLifecycle()
    val aiSearchResult by viewModel.aiSearchResult.collectAsStateWithLifecycle()
    val isSearchingAI by viewModel.isSearchingAI.collectAsStateWithLifecycle()

    var personalNoteInput by remember { mutableStateOf("") }
    var selectedGroupIndex by remember { mutableStateOf(0) }
    var activeReligionDetail by remember { mutableStateOf<Religion?>(null) }
    var activeBookReaderDetail by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- Warm Hero Welcome ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "Scripture Scholar",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    val welcomeSubtitle = welcomeSubtitles[dayOfYear % welcomeSubtitles.size]
                    Text(
                        text = welcomeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Verse of the Day Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VERSE OF THE DAY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(dailyVerse.tradition) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "“${dailyVerse.text}”",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dailyVerse.reference,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.startNewSession(
                                    title = "Study: ${dailyVerse.reference}",
                                    role = "Theological Scholar",
                                    model = "gemini-1.5-flash"
                                )
                                viewModel.inputText.value = "Explain the deep theological and historical meaning of ${dailyVerse.reference}: \"${dailyVerse.text}\""
                                onNavigateToChat()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("study_ai_daily_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Study with AI", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.addBookmark(
                                    scriptureName = dailyVerse.tradition,
                                    reference = dailyVerse.reference,
                                    text = dailyVerse.text,
                                    notes = "Saved from Verse of the Day."
                                )
                                Toast.makeText(context, "Added to Bookmarks!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bookmark_daily_button")
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- AI Scripture Discover Tool ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AI DISCOVER SCRIPTURE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Describe an emotion, topic, or question to find relevant scripture across traditions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = aiSearchQuery,
                        onValueChange = { viewModel.aiSearchQuery.value = it },
                        placeholder = { Text("e.g. comfort in grief, Bhagavad Gita on duty...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_search_input_field"),
                        singleLine = true,
                        trailingIcon = {
                            if (isSearchingAI) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = { viewModel.findScriptureViaAI() },
                                    modifier = Modifier.testTag("ai_search_button")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search AI")
                                }
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = aiSearchResult != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        aiSearchResult?.let { result ->
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = personalNoteInput,
                                    onValueChange = { personalNoteInput = it },
                                    placeholder = { Text("Add personal reflection/study notes...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.saveDiscoverResultToBookmarks(personalNoteInput)
                                            personalNoteInput = ""
                                            Toast.makeText(context, "Saved to Bookmarks!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .testTag("save_discover_to_bookmarks_button")
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save to Bookmarks", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            // Start a chat dialogue about this discover result
                                            viewModel.startNewSession(
                                                title = "AI Scripture Study",
                                                role = "Theological Scholar",
                                                model = "gemini-1.5-flash"
                                            )
                                            viewModel.inputText.value = "Provide deep theological analysis and historical insight on the scripture I just discovered:\n\n$result"
                                            onNavigateToChat()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Discuss", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- RELIGIONS DASHBOARD ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "धर्म डैशबोर्ड (RELIGION DASHBOARDS)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "विभिन्न धर्मों के पवित्र ग्रंथों, दर्शन और ज्ञान का अन्वेषण करें।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Category Selection Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReligionsData.groups.forEachIndexed { index, group ->
                        val groupEmoji = when {
                            group.groupName.contains("भारतीय") -> "🕉️"
                            group.groupName.contains("अब्राहमिक") -> "✡️"
                            group.groupName.contains("पूर्वी") -> "☯️"
                            else -> "🌿"
                        }
                        val cleanName = group.groupName.substringBefore(" (")
                        FilterChip(
                            selected = selectedGroupIndex == index,
                            onClick = { selectedGroupIndex = index },
                            label = { Text("$groupEmoji $cleanName") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Display Religions list for the selected category
        val currentGroup = ReligionsData.groups.getOrNull(selectedGroupIndex)
        if (currentGroup != null) {
            items(currentGroup.religions) { religion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeReligionDetail = religion }
                        .testTag("religion_card_${religion.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = religion.symbol, fontSize = 22.sp)
                            }
                            Column {
                                Text(
                                    text = religion.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val booksCount = religion.books.size
                                Text(
                                    text = if (religion.name == "आदिवासी धार्मिक परंपराएँ") "मौखिक परंपरा आधारित" else "$booksCount पवित्र ${if (booksCount > 1) "पुस्तकें" else "पुस्तक"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Explore",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Spacing spacer after dashboards list
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Recent Bookmarks Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT BOOKMARKS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "${bookmarks.size} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (bookmarks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No scriptures saved yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Bookmark verses or use Discover to add some!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        } else {
            items(bookmarks.take(3)) { bookmark ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bookmark.reference,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bookmark.scriptureName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bookmark.text,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (bookmark.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = bookmark.notes,
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeReligionDetail != null) {
        ReligionDashboardDialog(
            religion = activeReligionDetail!!,
            onDismiss = { activeReligionDetail = null },
            onStartStudy = { bookName ->
                viewModel.startNewSession(
                    title = "Study: $bookName",
                    role = "Theological Scholar",
                    model = "gemini-1.5-flash"
                )
                viewModel.inputText.value = "Tell me about '$bookName' of ${activeReligionDetail!!.name}. What is its historical context, main themes, and key teachings?"
                activeReligionDetail = null
                onNavigateToChat()
            },
            onReadBook = { bookName ->
                activeBookReaderDetail = bookName
                activeReligionDetail = null
            },
            onAskAI = { query ->
                viewModel.startNewSession(
                    title = "Study: ${activeReligionDetail!!.name}",
                    role = "Theological Scholar",
                    model = "gemini-1.5-flash"
                )
                viewModel.inputText.value = query
                activeReligionDetail = null
                onNavigateToChat()
            }
        )
    }

    if (activeBookReaderDetail != null) {
        val matchingReligion = ReligionsData.groups.flatMap { it.religions }.firstOrNull { r ->
            r.books.any { b -> b.name == activeBookReaderDetail }
        }
        val religionName = matchingReligion?.name ?: "Sacred Tradition"
        ScriptureReaderDialog(
            bookName = activeBookReaderDetail!!,
            religionName = religionName,
            onDismiss = { activeBookReaderDetail = null },
            onAddBookmark = { scripture, reference, text, notes ->
                viewModel.addBookmark(scripture, reference, text, notes)
            },
            onAskAI = { query ->
                viewModel.startNewSession(
                    title = "Study: $activeBookReaderDetail",
                    role = "Theological Scholar",
                    model = "gemini-1.5-flash"
                )
                viewModel.inputText.value = query
                activeBookReaderDetail = null
                onNavigateToChat()
            }
        )
    }
}

// ==========================================
// SCREEN 2: ACTIVE STUDY CHAT SCREEN
// ==========================================
@Composable
fun ChatScreen(
    viewModel: ScriptureViewModel,
    currentSession: SavedChatSession?,
    messages: List<ChatMessageEntity>
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Scroll to bottom when messages list size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    if (currentSession == null) {
        // --- Create New Chat Session Panel ---
        var newTitle by remember { mutableStateOf("") }
        var chosenRole by remember { mutableStateOf("Theological Scholar") }
        var chosenModel by remember { mutableStateOf("gemini-1.5-flash") }

        val roles = listOf(
            Triple("Theological Scholar", Icons.Default.School, "Deep theology, parallel doctrine & respect studies."),
            Triple("Historical Analyst", Icons.Default.HourglassEmpty, "Cultural contexts, ancient history & Consensuses."),
            Triple("Spiritual Companion", Icons.Default.Favorite, "Encouraging guidance, moral context & daily life companion."),
            Triple("Language Translator", Icons.Default.Translate, "Dissect source root words (Greek, Hebrew, Sanskrit, Arabic).")
        )

        val models = listOf(
            Triple("gemini-1.5-flash", "General Study (Flash)", "Balanced speed & accurate references."),
            Triple("gemini-1.5-pro", "Deep Analysis (Pro)", "Advanced reasoning, complex queries.")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Begin Study Dialogue",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Study Topic or Title") },
                placeholder = { Text("e.g. Theological Meaning of Hope") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Scholar Role Selector ---
            Text(
                text = "Select Scholar Personality Role:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            roles.forEach { (roleName, icon, desc) ->
                Card(
                    onClick = { chosenRole = roleName },
                    colors = CardDefaults.cardColors(
                        containerColor = if (chosenRole == roleName)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (chosenRole == roleName)
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else
                        null,
                    modifier = Modifier.fillMaxWidth().testTag("role_chip_$roleName")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (chosenRole == roleName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = roleName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (chosenRole == roleName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // --- LLM Model Selector ---
            Text(
                text = "Select Gemini Model:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            models.forEach { (modelId, name, detail) ->
                Card(
                    onClick = { chosenModel = modelId },
                    colors = CardDefaults.cardColors(
                        containerColor = if (chosenModel == modelId)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (chosenModel == modelId)
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else
                        null,
                    modifier = Modifier.fillMaxWidth().testTag("model_chip_$modelId")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosenModel == modelId,
                            onClick = { chosenModel = modelId }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (chosenModel == modelId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.startNewSession(newTitle, chosenRole, chosenModel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .testTag("start_chat_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Dialogue Session")
            }
        }
    } else {
        // --- Active Chat Session ---
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat session subheader
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSession.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentSession.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = currentSession.model.replace("gemini-", "").replace("-preview", ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.selectSession(null) },
                        modifier = Modifier.testTag("exit_chat_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Study")
                    }
                }
            }

            Divider()

            // Message list thread
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isUser = message.sender == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    color = if (isUser)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUser)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            if (isUser) {
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                com.example.ui.components.MarkdownText(
                                    text = message.text,
                                    baseColor = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUser) "You" else currentSession.role,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                if (isGenerating) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = 0.dp,
                                            bottomEnd = 12.dp
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scholar is formulating insight...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            // Chat input row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.inputText.value = it },
                        placeholder = { Text("Ask your scripture scholar...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4,
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                enabled = inputText.isNotBlank() && !isGenerating,
                                modifier = Modifier.testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank() && !isGenerating)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: BOOKMARKS & STUDY NOTES SCREEN
// ==========================================
@Composable
fun BookmarksScreen(
    viewModel: ScriptureViewModel,
    bookmarks: List<BookmarkedVerse>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBookmarkToEdit by remember { mutableStateOf<BookmarkedVerse?>(null) }
    val context = LocalContext.current

    val filteredBookmarks = bookmarks.filter {
        it.reference.contains(searchQuery, ignoreCase = true) ||
                it.text.contains(searchQuery, ignoreCase = true) ||
                it.scriptureName.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search bookmarks, traditions, references...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("bookmark_search_bar"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        if (filteredBookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (bookmarks.isEmpty()) "No scriptures saved." else "No matches found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBookmarks) { bookmark ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bookmark.reference,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = bookmark.scriptureName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "“${bookmark.text}”",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (bookmark.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Study Reflection & Notes",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bookmark.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            lineHeight = 16.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { selectedBookmarkToEdit = bookmark }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Notes", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.deleteBookmark(bookmark.id)
                                        Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for editing reflection notes
        selectedBookmarkToEdit?.let { bookmark ->
            EditNotesDialog(
                initialNotes = bookmark.notes,
                onDismiss = { selectedBookmarkToEdit = null },
                onConfirm = { newNotes ->
                    viewModel.updateBookmarkNotes(bookmark, newNotes)
                    selectedBookmarkToEdit = null
                    Toast.makeText(context, "Notes Updated!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ==========================================
// SCREEN 4: CHAT HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(
    viewModel: ScriptureViewModel,
    sessions: List<SavedChatSession>,
    onSessionSelected: () -> Unit
) {
    val context = LocalContext.current
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Saved Study Sessions",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved study chats yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sessions) { session ->
                    val isCurrent = currentSession?.sessionId == session.sessionId
                    val formatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
                    val formattedDate = formatter.format(Date(session.timestamp))

                    Card(
                        onClick = {
                            viewModel.selectSession(session)
                            onSessionSelected()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isCurrent)
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        else
                            null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = session.role,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = " • ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = session.model.replace("gemini-", "").replace("-preview", ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteSession(session.sessionId)
                                        Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Session",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE DIALOGS
// ==========================================

@Composable
fun AddBookmarkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var tradition by remember { mutableStateOf("Bible") }
    var citation by remember { mutableStateOf("") }
    var verseText by remember { mutableStateOf("") }
    var reflectionNotes by remember { mutableStateOf("") }

    val traditionsList = listOf("Bible", "Quran", "Bhagavad Gita", "Torah", "Dhammapada", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Bookmarked Scripture",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dropdown/Selector for Tradition
                Text(
                    "Sacred Tradition",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    traditionsList.forEach { item ->
                        FilterChip(
                            selected = tradition == item,
                            onClick = { tradition = item },
                            label = { Text(item) }
                        )
                    }
                }

                OutlinedTextField(
                    value = citation,
                    onValueChange = { citation = it },
                    label = { Text("Citation / Reference") },
                    placeholder = { Text("e.g. John 3:16, Al-Fatihah 1:1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = verseText,
                    onValueChange = { verseText = it },
                    label = { Text("Scripture Verse / Text") },
                    placeholder = { Text("Enter the actual sacred words...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = reflectionNotes,
                    onValueChange = { reflectionNotes = it },
                    label = { Text("Study Reflections / Notes") },
                    placeholder = { Text("What did you learn or feel study-wise?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (citation.isNotBlank() && verseText.isNotBlank()) {
                        onConfirm(tradition, citation, verseText, reflectionNotes)
                    }
                },
                enabled = citation.isNotBlank() && verseText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditNotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notesInput by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reflection Notes", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Study Reflections & Notes") },
                placeholder = { Text("Add personal reflections, historical details, etc...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(notesInput) }
            ) {
                Text("Save Notes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
