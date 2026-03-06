package org.guy.library.clients

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.guy.library.models.ImageAiModel
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GeminiImageClient(
    private val apiKey: String,
    private val model: ImageAiModel
) : ImageAiClient {

    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()
    
    private val isPredictEndpoint: Boolean
        get() = model.name.startsWith("IMAGEN")

    private val apiUrl: String
        get() = if (isPredictEndpoint) {
            "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:predict?key=$apiKey"
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:generateContent?key=$apiKey"
        }

    override fun generateImage(prompt: String): String {
        return generateImages(prompt, 1).first()
    }

    override fun generateImages(prompt: String, n: Int): List<String> {
        val requestBody = if (isPredictEndpoint) {
            val instanceObj = JsonObject().apply { addProperty("prompt", prompt) }
            val instancesArray = JsonArray().apply { add(instanceObj) }
            val parametersObj = JsonObject().apply { addProperty("sampleCount", n) }
            JsonObject().apply {
                add("instances", instancesArray)
                add("parameters", parametersObj)
            }
        } else {
            // gemini-2.5-flash-image generateContent payload
            val textPart = JsonObject().apply { addProperty("text", prompt) }
            val partsArray = JsonArray().apply { add(textPart) }
            val contentObj = JsonObject().apply { add("parts", partsArray) }
            val contentsArray = JsonArray().apply { add(contentObj) }
            
            JsonObject().apply {
                add("contents", contentsArray)
                // Gemini currently only supports returning 1 image per generateContent call usually, 
                // but we map it if they ever add n.
            }
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("Gemini Image API error (Status ${response.statusCode()}): ${response.body()}")
        }

        val urls = parseResponse(response.body())
        onImageGenerated?.invoke(urls.size)
        return urls
    }

    override var onImageGenerated: ((imageCount: Int) -> Unit)? = null

    private fun parseResponse(jsonResponse: String): List<String> {
        val rootObject = gson.fromJson(jsonResponse, JsonObject::class.java)
        val urls = mutableListOf<String>()

        if (isPredictEndpoint) {
            val predictionsArray = rootObject.getAsJsonArray("predictions")
                ?: throw Exception("Failed to parse image response: $jsonResponse")

            for (i in 0 until predictionsArray.size()) {
                val element = predictionsArray.get(i).asJsonObject
                val base64Bytes = element.get("bytesBase64")?.asString 
                if (base64Bytes != null) {
                    urls.add("data:image/png;base64,$base64Bytes")
                } else {
                    val url = element.get("url")?.asString
                    if (url != null) urls.add(url)
                }
            }
        } else {
            val candidatesArray = rootObject.getAsJsonArray("candidates")
                ?: throw Exception("Failed to parse image response: $jsonResponse")

            for (i in 0 until candidatesArray.size()) {
                val candidate = candidatesArray.get(i).asJsonObject
                val parts = candidate.getAsJsonObject("content")?.getAsJsonArray("parts")
                if (parts != null) {
                    for (j in 0 until parts.size()) {
                        val part = parts.get(j).asJsonObject
                        val inlineData = part.getAsJsonObject("inlineData")
                        if (inlineData != null) {
                            val base64Bytes = inlineData.get("data")?.asString
                            val mimeType = inlineData.get("mimeType")?.asString ?: "image/png"
                            if (base64Bytes != null) {
                                urls.add("data:$mimeType;base64,$base64Bytes")
                            }
                        }
                    }
                }
            }
        }

        if (urls.isEmpty()) {
            throw Exception("No images found in the response: $jsonResponse")
        }

        return urls
    }
}
