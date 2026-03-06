package org.guy.library.models

enum class ImageAiModel(
    val modelName: String,
    val costPerImage: Double
) {
    // OpenAI Image Models
    OPENAI_DALL_E_2("dall-e-2", 0.020), // Default 1024x1024
    OPENAI_DALL_E_3("dall-e-3", 0.040), // Standard 1024x1024
    OPENAI_DALL_E_3_HD("dall-e-3", 0.080), // HD 1024x1024 (Distinguished by quality param later if needed, but keeping separate for cost tracking)

    // Google Gemini Models
    GEMINI_2_5_FLASH_IMAGE_EXP("gemini-2.0-flash-exp-image-generation", 0.00000001),
    GEMINI_2_5_FLASH_IMAGE("gemini-2.5-flash-image", 0.00000001),
    GEMINI_3_PRO_IMAGE_PREVIEW("gemini-3.0-pro-image-preview", 0.24),
    IMAGEN_4_FAST("imagen-4.0-fast-generate-001", 0.02),

    // xAI (Grok) Models
    GROK_2_IMAGE_LATEST("grok-2-image-1212", 0.07);

    fun isOpenAi() = this.name.startsWith("OPENAI")
    fun isGemini() = this.name.startsWith("GEMINI") || this.name.startsWith("IMAGEN")
    fun isGrok() = this.name.startsWith("GROK")
}
