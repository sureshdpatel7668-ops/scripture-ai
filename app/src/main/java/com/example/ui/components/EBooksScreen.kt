package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookDownloadManager
import com.example.data.DownloadState
import kotlinx.coroutines.launch

data class EBookItem(
    val name: String,
    val hindiName: String,
    val religion: String,
    val category: String, // "Dharmic", "Abrahamic", "Other"
    val emblem: String,
    val description: String,
    val pdfUrl: String,
    val coverGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EBooksScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activePdfUrl by remember { mutableStateOf<String?>(null) }
    var activePdfTitle by remember { mutableStateOf<String?>(null) }

    val downloadStates by BookDownloadManager.downloadStates.collectAsState()

    val booksList = remember {
        listOf(
            EBookItem(
                name = "Srimad Bhagavad Gita",
                hindiName = "श्रीमद्भगवद्गीता",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🕉️",
                description = "जीवन संग्राम, निष्काम कर्मयोग और आत्मज्ञान का साक्षात ईश्वर-मुख-कथित दिव्य उपदेश।",
                pdfUrl = "https://archive.org/details/gita-sanjeevani-gita-press-gorakhpur",
                coverGradient = listOf(Color(0xFFE65100), Color(0xFFFF9100)) // Deep saffron
            ),
            EBookItem(
                name = "Shri Ramcharitmanas",
                hindiName = "श्री रामचरितमानस",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🏹",
                description = "मर्यादा पुरुषोत्तम श्री राम का आदर्श जीवन चरित्र, भक्ति और लोकाचार का दिव्य सागर।",
                pdfUrl = "https://archive.org/details/ramacharitamanas-gita-press",
                coverGradient = listOf(Color(0xFFC62828), Color(0xFFFF5252)) // Deep ochre/red
            ),
            EBookItem(
                name = "Holy Quran",
                hindiName = "पवित्र क़ुरआन",
                religion = "इस्लाम धर्म (Islam)",
                category = "Abrahamic",
                emblem = "☪️",
                description = "अल्लाह का अंतिम दिव्य संदेश, मानवता के लिए संपूर्ण मार्गदर्शन और शांति का मार्ग।",
                pdfUrl = "https://archive.org/details/the-holy-quran-hindi",
                coverGradient = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50)) // Islamic emerald green
            ),
            EBookItem(
                name = "Holy Bible",
                hindiName = "पवित्र बाइबल",
                religion = "ईसाई धर्म (Christianity)",
                category = "Abrahamic",
                emblem = "✝️",
                description = "प्रभु ईसा मसीह के पावन उपदेश, प्रेम, करुणा, क्षमा और मानवता का मुक्ति संदेश।",
                pdfUrl = "https://archive.org/details/HolyBibleHindi",
                coverGradient = listOf(Color(0xFF4A148C), Color(0xFF8E24AA)) // Royal Purple/Burgundy
            ),
            EBookItem(
                name = "Sri Guru Granth Sahib",
                hindiName = "श्री गुरु ग्रंथ साहिब",
                religion = "सिख धर्म (Sikhism)",
                category = "Dharmic",
                emblem = "ੴ",
                description = "संतों और गुरुओं की पावन दिव्य वाणी, निराकार परमात्मा की अनन्य आराधना और समानता।",
                pdfUrl = "https://archive.org/details/sri-guru-granth-sahib-hindi-translation",
                coverGradient = listOf(Color(0xFFF57C00), Color(0xFFFFB74D)) // Mustard Gold
            ),
            EBookItem(
                name = "Dhammapada",
                hindiName = "धम्मपद",
                religion = "बौद्ध धर्म (Buddhism)",
                category = "Dharmic",
                emblem = "☸️",
                description = "तथागत बुद्ध के नैतिक और आचरण संबंधी पावन सूत्र, मन की शुद्धि और निर्वाण का मार्ग।",
                pdfUrl = "https://archive.org/details/dhammapada-hindi-translation",
                coverGradient = listOf(Color(0xFF3E2723), Color(0xFF5D4037)) // Buddhist Maroon/Bronze
            ),
            EBookItem(
                name = "Holy Vedas",
                hindiName = "पवित्र वेद",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🌞",
                description = "सृष्टि का प्राचीनतम ज्ञान भंडार, ईश्वरीय प्रार्थनाएं और विश्व-कल्याण के वैदिक सूत्र।",
                pdfUrl = "https://archive.org/details/RigvedaHindiVol1GangaPrasadUpadhyaya",
                coverGradient = listOf(Color(0xFFE64A19), Color(0xFFFF7043)) // Copper/Saffron
            ),
            EBookItem(
                name = "Holy Upanishads",
                hindiName = "पवित्र उपनिषद",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🧘",
                description = "वेदांत का परम दार्शनिक चिंतन, जीवात्मा, परमात्मा और अद्वैत ज्ञान की पराकाष्ठा।",
                pdfUrl = "https://archive.org/details/108-upanishads-hindi-translation-by-pandit-shriram-sharma-acharya",
                coverGradient = listOf(Color(0xFF006064), Color(0xFF0097A7)) // Mystic Teal
            ),
            EBookItem(
                name = "Mahabharata",
                hindiName = "महाभारत",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "👑",
                description = "संसार का सबसे बड़ा धर्मग्रंथ महाकाव्य, कुरुक्षेत्र युद्ध, नीति शास्त्र और ज्ञान का खजाना।",
                pdfUrl = "https://archive.org/details/MahabharatGitaPress",
                coverGradient = listOf(Color(0xFF263238), Color(0xFF455A64)) // Royal Charcoal/Gold
            ),
            EBookItem(
                name = "Valmiki Ramayana",
                hindiName = "वाल्मीकि रामायण",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🌅",
                description = "आदिकवि वाल्मीकि द्वारा रचित मूल संस्कृत रामायण, धर्म और मर्यादा का आदि स्रोत।",
                pdfUrl = "https://archive.org/details/ValmikiRamayanaGitaPress",
                coverGradient = listOf(Color(0xFF5D4037), Color(0xFF8D6E63)) // Warm Wooden/Brown
            ),
            EBookItem(
                name = "Sushruta Samhita",
                hindiName = "सुश्रुत संहिता",
                religion = "प्राचीन विज्ञान (Ayurveda)",
                category = "Other",
                emblem = "🌱",
                description = "महर्षि सुश्रुत प्रणीत शल्यचिकित्सा (Surgery) और आयुर्वेद का विश्व प्रसिद्ध प्राचीनतम ग्रंथ।",
                pdfUrl = "https://archive.org/details/SushrutaSamhitaHindi",
                coverGradient = listOf(Color(0xFF004D40), Color(0xFF00796B)) // Deep Herbal Green
            ),
            EBookItem(
                name = "Holy Puranas",
                hindiName = "पवित्र पुराण",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🔱",
                description = "सृष्टि, देव, और ऋषियों के इतिहास तथा दिव्य गाथाओं के प्रमाणिक १८ महापुराण ग्रंथ।",
                pdfUrl = "https://archive.org/details/PuranasGitaPressHindi",
                coverGradient = listOf(Color(0xFF4E342E), Color(0xFF8D6E63)) // Terrakotta
            ),
            EBookItem(
                name = "Brahmasutra",
                hindiName = "ब्रह्मसूत्र",
                religion = "सनातन धर्म (Hinduism)",
                category = "Dharmic",
                emblem = "🕸️",
                description = "महर्षि बादरायण रचित वेदांत दर्शन का अत्यंत गंभीर, दार्शनिक और प्रमाणिक सूत्र ग्रंथ।",
                pdfUrl = "https://archive.org/details/BrahmasutraGitaPress",
                coverGradient = listOf(Color(0xFF1A237E), Color(0xFF3F51B5)) // Navy Blue
            ),
            EBookItem(
                name = "Jain Agamas",
                hindiName = "जैन आगम",
                religion = "जैन धर्म (Jainism)",
                category = "Dharmic",
                emblem = "🖐️",
                description = "भगवान महावीर और तीर्थंकरों की पावन अहिंसा, अनेकांतवाद और अपरिग्रह की दिव्य वाणी।",
                pdfUrl = "https://archive.org/details/JainAgamsHindi",
                coverGradient = listOf(Color(0xFFE65100), Color(0xFFF57C00)) // Bright Amber Orange
            ),
            EBookItem(
                name = "Samayasara",
                hindiName = "समयसार",
                religion = "जैन धर्म (Jainism)",
                category = "Dharmic",
                emblem = "✨",
                description = "आचार्य कुंदकुंद देव रचित आत्मज्ञान, शुद्ध निश्चय नय और मोक्ष मार्ग का आध्यात्मिक ग्रंथ।",
                pdfUrl = "https://archive.org/details/SamayasaraKundakundaHindi",
                coverGradient = listOf(Color(0xFF311B92), Color(0xFF673AB7)) // Deep Spiritual Violet
            ),
            EBookItem(
                name = "Tattvartha Sutra",
                hindiName = "तत्त्वार्थ सूत्र",
                religion = "जैन धर्म (Jainism)",
                category = "Dharmic",
                emblem = "💎",
                description = "आचार्य उमास्वामी रचित जैन दर्शन का सर्वमान्य प्रथम संस्कृत ग्रंथ, तत्वों का विवेचन।",
                pdfUrl = "https://archive.org/details/TattvarthaSutraHindi",
                coverGradient = listOf(Color(0xFF004D40), Color(0xFF009688)) // Emerald Teal
            )
        )
    }

    var downloadedBooks by remember { mutableStateOf(setOf<String>()) }

    // Reactive Refresh when a download completes or starts
    fun refreshDownloadedList() {
        downloadedBooks = booksList.filter { BookDownloadManager.isBookDownloaded(context, it.name) }
            .map { it.name }
            .toSet()
    }

    LaunchedEffect(downloadStates) {
        refreshDownloadedList()
        // Check for download errors and notify the user with a Toast
        downloadStates.forEach { (bookName, state) ->
            if (state is DownloadState.Error) {
                val bookHindi = booksList.find { it.name == bookName }?.hindiName ?: bookName
                Toast.makeText(context, "$bookHindi को सहेजने में विफल: ${state.message}", Toast.LENGTH_LONG).show()
                BookDownloadManager.clearDownloadState(bookName)
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshDownloadedList()
    }

    // Filter list based on search, category and offline status
    val filteredBooks = remember(searchQuery, selectedCategory, downloadedBooks) {
        booksList.filter { book ->
            val matchesSearch = book.name.lowercase().contains(searchQuery.lowercase()) ||
                    book.hindiName.contains(searchQuery) ||
                    book.religion.lowercase().contains(searchQuery.lowercase())
            
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Saved" -> downloadedBooks.contains(book.name)
                else -> book.category == selectedCategory
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Library Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ई-पुस्तकालय (E-BOOKS LIBRARY)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "सभी पवित्र धर्म ग्रंथों की प्रमाणिक संपूर्ण PDF यहाँ से डाउनलोड करें और ऑफ़लाइन पढ़ें।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("पुस्तक या धर्म का नाम खोजें...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ebooks_search_field"),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        // Category Selection Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                "All" to "📚 सभी ग्रंथ",
                "Saved" to "📥 ऑफ़लाइन सहेजे गए (${downloadedBooks.size})",
                "Dharmic" to "🕉️ भारतीय परंपरा",
                "Abrahamic" to "✝️ अब्राहमिक",
                "Other" to "🌿 अन्य/विज्ञान"
            )

            categories.forEach { (catId, label) ->
                FilterChip(
                    selected = selectedCategory == catId,
                    onClick = { selectedCategory = catId },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Books Grid
        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LibraryBooks,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedCategory == "Saved") "कोई ऑफ़लाइन पुस्तक सहेजी नहीं गई है।" else "कोई पुस्तक नहीं मिली",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBooks) { book ->
                    val isDownloaded = downloadedBooks.contains(book.name)
                    val downloadState = downloadStates[book.name] ?: DownloadState.Idle

                    BookCardItem(
                        book = book,
                        isDownloaded = isDownloaded,
                        downloadState = downloadState,
                        onOpenOnline = {
                            activePdfUrl = book.pdfUrl
                            activePdfTitle = book.hindiName
                        },
                        onOpenOffline = {
                            BookDownloadManager.openDownloadedBook(context, book.name)
                        },
                        onStartDownload = {
                            scope.launch {
                                BookDownloadManager.downloadBook(context, book.name, book.pdfUrl)
                                refreshDownloadedList()
                            }
                        },
                        onDeleteFile = {
                            BookDownloadManager.deleteDownloadedBook(context, book.name)
                            refreshDownloadedList()
                            Toast.makeText(context, "${book.hindiName} ऑफ़लाइन से हटाई गई।", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (activePdfUrl != null) {
        InAppWebViewDialog(
            url = activePdfUrl!!,
            title = activePdfTitle ?: "E-Book",
            onDismiss = { activePdfUrl = null }
        )
    }
}

@Composable
fun BookCardItem(
    book: EBookItem,
    isDownloaded: Boolean,
    downloadState: DownloadState,
    onOpenOnline: () -> Unit,
    onOpenOffline: () -> Unit,
    onStartDownload: () -> Unit,
    onDeleteFile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .clickable {
                if (isDownloaded) {
                    onOpenOffline()
                } else {
                    onOpenOnline()
                }
            }
            .testTag("ebook_card_${book.name}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Cover Render (3D Hardcover Look with Gold Border)
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(155.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.verticalGradient(book.coverGradient))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFD700), // Pure Gold
                                Color(0xFFDAA520)  // Goldenrod
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp)
            ) {
                // Gold Inner Margin Box (Like antique books)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 0.75.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Small Title at top of cover
                        Text(
                            text = book.name,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            lineHeight = 9.sp,
                            fontFamily = FontFamily.Serif
                        )

                        // Big Emblem Icon in Center of cover
                        Text(
                            text = book.emblem,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Status badge inside cover
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isDownloaded) Color(0xFF2E7D32).copy(alpha = 0.9f)
                                    else Color.White.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.OfflinePin else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isDownloaded) Color.White else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(8.dp)
                                )
                                Text(
                                    text = if (isDownloaded) "Saved" else "Cloud",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Book Information Text
            Text(
                text = book.hindiName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = book.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Religion Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = book.religion.substringBefore(" ("),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Download states handling & Actions Row
            when (downloadState) {
                is DownloadState.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = downloadState.progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "सहेजा जा रहा है... ${downloadState.progress}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main Action Button (Open Online/Offline)
                        Button(
                            onClick = {
                                if (isDownloaded) onOpenOffline() else onOpenOnline()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDownloaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.FileOpen else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (isDownloaded) "ऑफ़लाइन पढ़ें" else "ऑनलाइन पढ़ें",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Auxiliary Download/Delete Button
                        IconButton(
                            onClick = {
                                if (isDownloaded) onDeleteFile() else onStartDownload()
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    color = if (isDownloaded) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.Delete else Icons.Default.Download,
                                contentDescription = if (isDownloaded) "Delete" else "Download",
                                tint = if (isDownloaded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
