package org.guy.library.clients

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.guy.library.models.AiModel
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GeminiClient(
    private val apiKey: String,
    private val model: AiModel
) : AiClient {

    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()

    override fun generate(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:generateContent?key=$apiKey"

        // Build the JSON request body
        val textPart = JsonObject().apply { addProperty("text", prompt) }
        val partsArray = com.google.gson.JsonArray().apply { add(textPart) }
        val contentObject = JsonObject().apply { add("parts", partsArray) }
        val contentsArray = com.google.gson.JsonArray().apply { add(contentObject) }
        val requestBody = JsonObject().apply { add("contents", contentsArray) }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("Gemini API error: ${response.statusCode()} - ${response.body()}")
        }

        return parseResponse(response.body())
    }

    override fun <T> generateObject(prompt: String, type: java.lang.reflect.Type): T {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:generateContent?key=$apiKey"

        val systemInstructionText = org.guy.library.util.buildSystemInstruction(type)

        val textPart = JsonObject().apply { addProperty("text", prompt) }
        val partsArray = com.google.gson.JsonArray().apply { add(textPart) }
        val contentObject = JsonObject().apply { add("parts", partsArray) }
        val contentsArray = com.google.gson.JsonArray().apply { add(contentObject) }
        
        val systemInstructionPart = JsonObject().apply { addProperty("text", systemInstructionText) }
        val systemInstructionPartsArray = com.google.gson.JsonArray().apply { add(systemInstructionPart) }
        val systemInstructionObject = JsonObject().apply { add("parts", systemInstructionPartsArray) }

        val generationConfig = JsonObject().apply {
            addProperty("response_mime_type", "application/json")
        }

        val requestBody = JsonObject().apply { 
            add("contents", contentsArray)
            add("system_instruction", systemInstructionObject)
            add("generationConfig", generationConfig)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("Gemini API error: ${response.statusCode()} - ${response.body()}")
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
        rootObject.getAsJsonObject("usageMetadata")?.let { usage ->
            val promptTokens = usage.get("promptTokenCount")?.asInt ?: 0
            val completionTokens = usage.get("candidatesTokenCount")?.asInt ?: 0
            onTokenUsage?.invoke(promptTokens, completionTokens)
        }

        return rootObject
            .getAsJsonArray("candidates")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString
            ?: throw Exception("Failed to parse response: $jsonResponse")
    }
}
