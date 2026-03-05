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

    override fun generate(prompt: String, vararg files: java.io.File): String {
        // Build the request body
        val contentArray = JsonArray()
        
        // Add file parts if provided
        for (file in files) {
            val mimeType = org.guy.library.util.FileUtil.getMimeType(file)
            
            if (mimeType.startsWith("image/")) {
                val base64Data = org.guy.library.util.FileUtil.encodeFileToBase64(file)
                val imageUrlObj = JsonObject().apply {
                    addProperty("url", "data:$mimeType;base64,$base64Data")
                }
                val imagePart = JsonObject().apply {
                    addProperty("type", "image_url")
                    add("image_url", imageUrlObj)
                }
                contentArray.add(imagePart)
            } else {
                val fileId = uploadFile(file)
                val fileObj = JsonObject().apply {
                    addProperty("file_id", fileId)
                }
                val filePart = JsonObject().apply {
                    addProperty("type", "file")
                    add("file", fileObj)
                }
                contentArray.add(filePart)
            }
        }
        
        // Add text prompt
        val textPart = JsonObject().apply {
            addProperty("type", "text")
            addProperty("text", prompt)
        }
        contentArray.add(textPart)

        val messageObj = JsonObject().apply {
            addProperty("role", "user")
            // If no files, we can just pass the prompt string (simpler). If files exist, we MUST pass the array.
            if (files.isEmpty()) {
                addProperty("content", prompt)
            } else {
                add("content", contentArray)
            }
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

    override fun <T> generateObject(prompt: String, type: java.lang.reflect.Type, vararg files: java.io.File): T {
        val systemInstructionText = org.guy.library.util.buildSystemInstruction(type)

        val systemMessageObj = JsonObject().apply {
            addProperty("role", "system")
            addProperty("content", systemInstructionText)
        }
        
        val contentArray = JsonArray()
        
        // Add file parts if provided
        for (file in files) {
            val mimeType = org.guy.library.util.FileUtil.getMimeType(file)
            
            if (mimeType.startsWith("image/")) {
                val base64Data = org.guy.library.util.FileUtil.encodeFileToBase64(file)
                val imageUrlObj = JsonObject().apply {
                    addProperty("url", "data:$mimeType;base64,$base64Data")
                }
                val imagePart = JsonObject().apply {
                    addProperty("type", "image_url")
                    add("image_url", imageUrlObj)
                }
                contentArray.add(imagePart)
            } else {
                val fileId = uploadFile(file)
                val fileObj = JsonObject().apply {
                    addProperty("file_id", fileId)
                }
                val filePart = JsonObject().apply {
                    addProperty("type", "file")
                    add("file", fileObj)
                }
                contentArray.add(filePart)
            }
        }
        
        val textPart = JsonObject().apply {
            addProperty("type", "text")
            addProperty("text", prompt)
        }
        contentArray.add(textPart)

        val userMessageObj = JsonObject().apply {
            addProperty("role", "user")
            // If no files, we can just pass the prompt string (simpler). If files exist, we MUST pass the array.
            if (files.isEmpty()) {
                addProperty("content", prompt)
            } else {
                add("content", contentArray)
            }
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

    private fun uploadFile(file: java.io.File): String {
        val filesEndpoint = when {
            model.isGrok() -> "https://api.x.ai/v1/files"
            else -> "https://api.openai.com/v1/files"
        }
        val boundary = "Boundary-" + System.currentTimeMillis()
        val requestBody = buildMultipartBody(file, boundary)
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(filesEndpoint))
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
            .build()
            
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw Exception("Failed to upload file to OpenAI: ${response.statusCode()} - ${response.body()}")
        }
        val rootObject = gson.fromJson(response.body(), JsonObject::class.java)
        return rootObject.get("id").asString
    }
    
    private fun buildMultipartBody(file: java.io.File, boundary: String): ByteArray {
        val mimeType = org.guy.library.util.FileUtil.getMimeType(file)
        val charset = Charsets.UTF_8
        val os = java.io.ByteArrayOutputStream()
        val writer = java.io.PrintWriter(java.io.OutputStreamWriter(os, charset), true)

        writer.append("--").append(boundary).append("\r\n")
        writer.append("Content-Disposition: form-data; name=\"purpose\"").append("\r\n")
        writer.append("\r\n")
        writer.append("user_data").append("\r\n")

        writer.append("--").append(boundary).append("\r\n")
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.name).append("\"").append("\r\n")
        writer.append("Content-Type: ").append(mimeType).append("\r\n")
        writer.append("\r\n")
        writer.flush()
        
        os.write(java.nio.file.Files.readAllBytes(file.toPath()))
        os.flush()
        
        writer.append("\r\n")
        writer.append("--").append(boundary).append("--").append("\r\n")
        writer.flush()
        
        return os.toByteArray()
    }
}
