package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        model: String,
        systemInstruction: String?,
        history: List<Pair<String, String>> // list of (role, text) where role is "user" or "model"
    ): String = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val apiKey = rawApiKey.trim().removeSurrounding("\"").removeSurrounding("'")
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext """
                Error: 🛑 **API Key कॉन्फ़िगर नहीं है (No API Key Required for Offline Mode)**
                
                यह ऐप **बिना किसी API Key के भी 100% काम कर सकता है!**
                
                **आप बिना इंटरनेट/API Key के क्या-क्या कर सकते हैं?**
                1. **ऑफ़लाइन धार्मिक ग्रंथ**: ऐप में पहले से लोड किए गए अध्याय (जैसे: *श्रीमद्भगवद्गीता अध्याय 1 और 2*, *पवित्र क़ुरआन अल-फ़ातिहा*, *बाइबल उत्पत्ति अध्याय 1*, *जपजी साहिब*, *धम्मपद*, आदि) पूरी तरह से बिना इंटरनेट और बिना किसी API Key के काम करते हैं।
                2. **सम्पूर्ण पुस्तक PDF**: आप प्रत्येक ग्रंथ के अंतर्गत दिए गए **'सम्पूर्ण पुस्तक PDF'** बटन पर क्लिक करके पूरी पुस्तक डाउनलोड कर सकते हैं, जिसे आप भविष्य में कभी भी बिना नेटवर्क के पढ़ सकते हैं।
                
                **API Key की आवश्यकता कब होती है?**
                * जब आप 'Study Chat' में AI से अपने खुद के प्रश्न पूछते हैं, या नए अध्यायों की मांग करते हैं, तब रीयल-टाइम AI विश्लेषण (Gemini API) के लिए एक Key की आवश्यकता होती है।
                * यदि आप AI Chat सुविधाओं का लाभ उठाना चाहते हैं, तो आप AI Studio के **Secrets** पैनल में अपनी खुद की `GEMINI_API_KEY` निःशुल्क दर्ज कर सकते हैं।
            """.trimIndent()
        }

        val isThinkingEnabled = model == "gemini-3.1-pro-preview-thinking"
        val actualModel = if (isThinkingEnabled) "gemini-3.1-pro-preview" else model

        val url = "$BASE_URL$actualModel:generateContent?key=$apiKey"

        try {
            val root = JSONObject()

            // Build contents array
            val contentsArray = JSONArray()
            for ((role, text) in history) {
                val contentObj = JSONObject()
                // Map "assistant" or other roles to "model" for Gemini
                val apiRole = if (role == "assistant" || role == "model") "model" else "user"
                contentObj.put("role", apiRole)

                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", text)
                partsArray.put(partObj)
                
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            root.put("contents", contentsArray)

            // Build system instruction if present
            if (!systemInstruction.isNullOrBlank()) {
                val sysInstObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", systemInstruction)
                partsArray.put(partObj)
                sysInstObj.put("parts", partsArray)
                root.put("systemInstruction", sysInstObj)
            }

            // Optional generation config
            val genConfig = JSONObject()
            if (isThinkingEnabled) {
                val thinkingConfig = JSONObject()
                thinkingConfig.put("thinkingLevel", "ThinkingLevel.HIGH")
                genConfig.put("thinkingConfig", thinkingConfig)
            } else {
                genConfig.put("temperature", 0.7)
            }
            root.put("generationConfig", genConfig)

            val requestBodyString = root.toString()
            Log.d(TAG, "Sending request to model $actualModel: $requestBodyString")

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}, body: $responseBodyString")

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyString ?: "")
                        errObj.getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                    
                    val friendlyError = if (errorMsg.contains("quota", ignoreCase = true) || 
                                            errorMsg.contains("limit", ignoreCase = true) || 
                                            errorMsg.contains("rate", ignoreCase = true) || 
                                            response.code == 429) {
                        """
                        🛑 **API कोटा सीमा समाप्त (Quota Exceeded)**
                        
                        यह **मोबाइल स्टोरेज या इंटरनेट स्पीड** की समस्या नहीं है। यह AI सर्वर (Gemini API) की प्रति मिनट अनुरोध सीमा है क्योंकि वर्तमान में एक निःशुल्क API Key (Free Tier Key) उपयोग की जा रही है।
                        
                        **इसे कैसे ठीक करें?**
                        1. **थोड़ी देर प्रतीक्षा करें**: 15-20 सेकंड बाद पुनः प्रयास करें, सीमा स्वचालित रूप से रीसेट हो जाएगी।
                        2. **अपनी खुद की API Key जोड़ें**: AI Studio के **Secrets** पैनल में अपनी खुद की `GEMINI_API_KEY` दर्ज करें ताकि आपको असीमित और तीव्र गति से सेवा मिल सके।
                        
                        *(Error Details: $errorMsg)*
                        """.trimIndent()
                    } else {
                        errorMsg
                    }
                    return@withContext "Error: $friendlyError"
                }

                if (responseBodyString.isNullOrEmpty()) {
                    return@withContext "Error: Empty response from model"
                }

                val responseJson = JSONObject(responseBodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "Error: No candidates returned from AI"
                }

                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                if (content == null) {
                    return@withContext "Error: No content found in candidate"
                }

                val parts = content.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return@withContext "Error: No parts found in content"
                }

                val thoughts = StringBuilder()
                val answers = StringBuilder()

                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    var isThoughtPart = false
                    if (part.has("thought")) {
                        val thoughtObj = part.opt("thought")
                        if (thoughtObj is Boolean && thoughtObj == true) {
                            isThoughtPart = true
                            val text = part.optString("text")
                            if (!text.isNullOrEmpty()) {
                                thoughts.append(text)
                            }
                        } else if (thoughtObj is String) {
                            val thoughtStr = thoughtObj.trim()
                            if (thoughtStr.isNotEmpty()) {
                                thoughts.append(thoughtStr)
                            }
                        } else {
                            val thoughtStr = part.optString("thought")
                            if (thoughtStr.isNotEmpty()) {
                                thoughts.append(thoughtStr)
                            }
                        }
                    }

                    if (!isThoughtPart) {
                        val text = part.optString("text")
                        if (!text.isNullOrEmpty()) {
                            answers.append(text)
                        }
                    }
                }

                val finalAnswer = answers.toString().trim()
                val rawThoughts = thoughts.toString().trim()

                if (finalAnswer.isEmpty()) {
                    if (rawThoughts.isNotEmpty()) {
                        return@withContext rawThoughts
                    }
                    if (parts.length() > 0) {
                        val firstPart = parts.getJSONObject(0)
                        val text = firstPart.optString("text")
                        if (!text.isNullOrEmpty()) {
                            return@withContext text
                        }
                        val thoughtVal = firstPart.opt("thought")
                        if (thoughtVal is String && thoughtVal.isNotEmpty()) {
                            return@withContext thoughtVal
                        }
                    }
                    return@withContext "Error: Empty text part"
                }

                if (rawThoughts.isNotEmpty()) {
                    return@withContext "> **Thinking Process:**\n> ${rawThoughts.replace("\n", "\n> ")}\n\n$finalAnswer"
                }

                return@withContext finalAnswer
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return@withContext "Error: ${e.localizedMessage ?: "Unknown connection error"}"
        }
    }
}
