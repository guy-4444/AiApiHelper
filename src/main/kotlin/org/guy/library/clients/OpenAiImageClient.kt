package org.guy.library.clients

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.guy.library.models.ImageAiModel
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OpenAiImageClient(
    private val apiKey: String,
    private val model: ImageAiModel
) : ImageAiClient {

    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()
    private val apiUrl = "https://api.openai.com/v1/images/generations"

    override fun generateImage(prompt: String): String {
        return generateImages(prompt, 1).first()
    }

    override fun generateImages(prompt: String, n: Int): List<String> {
        val requestBody = JsonObject().apply {
            addProperty("prompt", prompt)
            addProperty("n", n)

            when (model) {
                ImageAiModel.OPENAI_DALL_E_2 -> {
                    addProperty("model", "dall-e-2")
                    addProperty("size", "1024x1024")
                }
                ImageAiModel.OPENAI_DALL_E_3 -> {
                    addProperty("model", "dall-e-3")
                    addProperty("size", "1024x1024")
                }
                ImageAiModel.OPENAI_DALL_E_3_HD -> {
                    addProperty("model", "dall-e-3")
                    addProperty("size", "1024x1024")
                    addProperty("quality", "hd")
                }
                else -> throw IllegalArgumentException("Unsupported OpenAI Image model: $model")
            }
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw Exception("OpenAI Image API error (Status ${response.statusCode()}): ${response.body()}")
        }

        val urls = parseResponse(response.body())
        onImageGenerated?.invoke(urls.size)
        return urls
    }

    override var onImageGenerated: ((imageCount: Int) -> Unit)? = null

    private fun parseResponse(jsonResponse: String): List<String> {
        val rootObject = gson.fromJson(jsonResponse, JsonObject::class.java)
        
        val dataArray = rootObject.getAsJsonArray("data")
            ?: throw Exception("Failed to parse image response: $jsonResponse")

        val urls = mutableListOf<String>()
        for (i in 0 until dataArray.size()) {
            val element = dataArray.get(i).asJsonObject
            urls.add(element.get("url").asString)
        }

        return urls
    }
}
