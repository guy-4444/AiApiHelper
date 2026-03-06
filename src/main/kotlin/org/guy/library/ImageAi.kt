package org.guy.library

import org.guy.library.clients.ImageAiClient
import org.guy.library.clients.OpenAiImageClient
import org.guy.library.clients.GeminiImageClient
import org.guy.library.clients.GrokImageClient
import org.guy.library.models.ImageAiModel

/**
 * The entry point for generating images using AI in the Simple AI Library.
 */
class ImageAi(
    private val apiKey: String,
    private val model: ImageAiModel
) {
    private val client: ImageAiClient = when {
        model.isOpenAi() -> OpenAiImageClient(apiKey, model)
        model.isGemini() -> GeminiImageClient(apiKey, model)
        model.isGrok() -> GrokImageClient(apiKey, model)
        else -> throw IllegalArgumentException("Unsupported Image AI model: $model")
    }

    /**
     * If true, the library will automatically calculate and print the estimated API cost
     * (based on the number of images generated) to the console after every call.
     */
    var logCost: Boolean = false

    init {
        client.onImageGenerated = { imageCount ->
            if (logCost) {
                val totalCost = imageCount * model.costPerImage
                
                println("\n[Image AiApi Cost Logger]")
                println("Model: ${model.modelName}")
                println("Images Generated: $imageCount")
                println("Est. Cost: $${String.format("%.3f", totalCost)}")
                println("------------------------\n")
            }
        }
    }

    /**
     * Asks the AI to generate a single image based on the provided prompt.
     * @param prompt Text description of the desired image.
     * @return URL of the generated image.
     */
    fun generateImage(prompt: String): String {
        return client.generateImage(prompt)
    }

    /**
     * Asks the AI to generate multiple images based on the provided prompt.
     * @param prompt Text description of the desired image.
     * @param count Number of images to generate (must be supported by the provider, e.g. DALL-E 3 only supports 1).
     * @return List of URLs of the generated images.
     */
    fun generateImages(prompt: String, count: Int): List<String> {
        return client.generateImages(prompt, count)
    }
}
