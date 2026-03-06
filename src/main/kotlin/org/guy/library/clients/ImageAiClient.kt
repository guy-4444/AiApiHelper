package org.guy.library.clients

import java.io.File

interface ImageAiClient {
    /**
     * Generates a single image based on the prompt.
     * @param prompt The image description.
     * @return The URL of the generated image.
     */
    fun generateImage(prompt: String): String

    /**
     * Generates multiple images based on the prompt.
     * @param prompt The image description.
     * @param n The number of images to generate.
     * @return A list of URLs of the generated images.
     */
    fun generateImages(prompt: String, n: Int): List<String>

    /**
     * Optional callback for cost tracking or usage metadata.
     * imageCount: number of images successfully generated.
     */
    var onImageGenerated: ((imageCount: Int) -> Unit)?
}
