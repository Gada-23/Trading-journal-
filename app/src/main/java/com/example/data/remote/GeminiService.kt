package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    // Use the latest supported stable lightweight model
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getTradeReview(
        asset: String,
        isBuy: Boolean,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        exitPrice: Double,
        lotSize: Double,
        strategy: String,
        notes: String,
        mistakes: String,
        emotionalState: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured. Please enter your GEMINI_API_KEY inside the Secrets panel in Google AI Studio to enable detailed AI feedback!"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        
        val systemInstruction = """
            You are an elite, highly critical forex/crypto multi-asset risk manager and trading behavioral psychologist.
            Review this completed trade entry. Under three structured markdown headings (*Risk Management*, *Tactical Execution*, *Psychological Analysis*), 
            provide clear, extremely professional, direct, and actionable critique. Be encouraging but honest. Mention risk-to-reward metrics.
            Keep the response visual, clean, and concise (max 3 short bullet points per section, no extra banter).
        """.trimIndent()

        val prompt = """
            Please analyze this trade details and provide a professional review:
            - Asset: $asset
            - Direction: ${if (isBuy) "BUY (Long)" else "SELL (Short)"}
            - Entry Price: $entryPrice
            - Stop Loss: $stopLoss
            - Take Profit: $takeProfit
            - Close/Exit Price: $exitPrice
            - Lot size: $lotSize
            - Trading Strategy: $strategy
            - Trader's Emotional State: $emotionalState
            - Trader's Mistakes checklist: $mistakes
            - User's reflections & notes: $notes
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val systemInstructionObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", systemInstruction)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put("systemInstruction", systemInstructionObj)
            }

            val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Failed response: code=${response.code}, body=$errBody")
                    return@withContext "Could not analyze trade. The API returned error code ${response.code}."
                }

                val bodyStr = response.body?.string() ?: return@withContext "Empty response from Gemini server."
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "AI response text not found.")
                        }
                    }
                }
                return@withContext "No response candidates returned by the trading analysis model."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini review request", e)
            return@withContext "Failed to generate AI feedback: ${e.localizedMessage ?: "Network connection error"}"
        }
    }
}
