package org.guy.library.clients

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.guy.library.models.AiModel
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OpenAiClient(
    private val apiKey: String,
    private val model: AiModel
) : AiClient {

    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()

    private val apiUrl: String = when {
        model.isGrok() -> "https://api.x.ai/v1/chat/completions"
        else -> "https://api.openai.com/v1/chat/completions"
    }

    override fun generate(prompt: String): String {
        // Build the request body
        val messageObj = JsonObject().apply {
            addProperty("role", "user")
            addProperty("content", prompt)
        }
        val messagesArray = JsonArray().apply { add(messageObj) }
        
        val requestBody = JsonObject().apply {
            addProperty("model", model.modelName)
            add("messages", messagesArray)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("API error (Status ${response.statusCode()}): ${response.body()}")
        }

        return parseResponse(response.body())
    }

    override fun <T> generateObject(prompt: String, type: java.lang.reflect.Type): T {
        val systemInstructionText = org.guy.library.util.buildSystemInstruction(type)

        val systemMessageObj = JsonObject().apply {
            addProperty("role", "system")
            addProperty("content", systemInstructionText)
        }
        val userMessageObj = JsonObject().apply {
            addProperty("role", "user")
            addProperty("content", prompt)
        }
        val messagesArray = JsonArray().apply { 
            add(systemMessageObj)
            add(userMessageObj) 
        }
        
        val responseFormatObj = JsonObject().apply {
            addProperty("type", "json_object")
        }

        val requestBody = JsonObject().apply {
            addProperty("model", model.modelName)
            add("messages", messagesArray)
            add("response_format", responseFormatObj)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("API error (Status ${response.statusCode()}): ${response.body()}")
        }

        var jsonText = parseResponse(response.body())
        if (org.guy.library.util.isCollectionType(type)) {
            val root = gson.fromJson(jsonText, JsonObject::class.java)
            if (root.has("data")) {
                jsonText = root.get("data").toString()
            }
        }
        return gson.fromJson(jsonText, type)
    }

    override var onTokenUsage: ((inputTokens: Int, outputTokens: Int) -> Unit)? = null

    private fun parseResponse(jsonResponse: String): String {
        val rootObject = gson.fromJson(jsonResponse, JsonObject::class.java)
        
        // Extract usage tokens
        rootObject.getAsJsonObject("usage")?.let { usage ->
            val promptTokens = usage.get("prompt_tokens")?.asInt ?: 0
            val completionTokens = usage.get("completion_tokens")?.asInt ?: 0
            onTokenUsage?.invoke(promptTokens, completionTokens)
        }

        return rootObject
            .getAsJsonArray("choices")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString
            ?: throw Exception("Failed to parse response: $jsonResponse")
    }
}
