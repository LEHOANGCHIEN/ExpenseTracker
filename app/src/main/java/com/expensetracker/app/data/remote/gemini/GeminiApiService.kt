package com.expensetracker.app.data.remote.gemini

import com.expensetracker.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiService @Inject constructor() {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val chatModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 32
                topP = 0.95f
                maxOutputTokens = 1024
            },
        )
    }

    private val jsonModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                responseMimeType = "application/json"
            },
        )
    }

    fun isApiKeyAvailable(): Boolean = apiKey.isNotBlank()

    /**
     * Send a chat message with optional history.
     * [systemInstruction] is prepended to the user message if non-blank.
     */
    suspend fun chat(
        systemInstruction: String,
        history: List<Content>,
        userMessage: String,
    ): Result<String> {
        if (!isApiKeyAvailable()) {
            return Result.failure(IllegalStateException("Gemini API key not configured"))
        }
        return runCatching {
            val chat = chatModel.startChat(history = history)
            val fullMessage = if (systemInstruction.isNotBlank()) {
                "$systemInstruction\n\n$userMessage"
            } else {
                userMessage
            }
            chat.sendMessage(content { text(fullMessage) }).text
                ?: throw IllegalStateException("Empty response from Gemini")
        }.mapToUserFriendlyError()
    }

    /**
     * Generate a structured JSON response.
     * [systemInstruction] and [userMessage] are combined into a single prompt.
     */
    suspend fun generateJson(
        systemInstruction: String,
        userMessage: String = "",
    ): Result<String> {
        if (!isApiKeyAvailable()) {
            return Result.failure(IllegalStateException("Gemini API key not configured"))
        }
        return runCatching {
            val combined = buildString {
                if (systemInstruction.isNotBlank()) appendLine(systemInstruction)
                if (userMessage.isNotBlank()) append(userMessage)
            }.trim()
            jsonModel.generateContent(combined).text
                ?: throw IllegalStateException("Empty response from Gemini")
        }.mapToUserFriendlyError()
    }

    /**
     * Send a free-form prompt to the chat model (for debug/test purposes).
     */
    suspend fun sendPrompt(prompt: String): Result<String> {
        if (!isApiKeyAvailable()) {
            return Result.failure(IllegalStateException("Gemini API key not configured. Add GEMINI_API_KEY to local.properties."))
        }
        return runCatching {
            chatModel.generateContent(prompt).text
                ?: throw IllegalStateException("Empty response from Gemini")
        }.mapToUserFriendlyError()
    }

    private fun <T> Result<T>.mapToUserFriendlyError(): Result<T> = recoverCatching { e ->
        val msg = e.message.orEmpty()
        throw when {
            msg.contains("API key", ignoreCase = true) || msg.contains("INVALID_ARGUMENT", ignoreCase = true) ->
                IllegalStateException("Invalid Gemini API key. Check local.properties.", e)
            msg.contains("quota", ignoreCase = true) || msg.contains("429", ignoreCase = true) ->
                IllegalStateException("Gemini rate limit exceeded. Please try again later.", e)
            msg.contains("block", ignoreCase = true) || msg.contains("SAFETY", ignoreCase = true) ->
                IllegalStateException("Content blocked by Gemini safety filters.", e)
            msg.contains("UNAVAILABLE", ignoreCase = true) || msg.contains("connect", ignoreCase = true) ->
                IllegalStateException("Cannot reach Gemini. Check your internet connection.", e)
            else -> e
        }
    }
}
