package org.guy.library.models

enum class AiModel(
    val modelName: String,
    val inputCostPerMillion: Double,
    val outputCostPerMillion: Double
) {
    // Google Gemini Models (Latest generation)
    GEMINI_3_1_FLASH_LITE_PREVIEW("gemini-3.1-flash-lite-preview", 0.125, 0.375),
    GEMINI_3_1_FLASH_PREVIEW("gemini-3.1-flash-preview", 0.25, 0.75),
    GEMINI_3_1_PRO_PREVIEW("gemini-3.1-pro-preview", 2.00, 6.00),
    GEMINI_2_5_FLASH_LITE("gemini-2.5-flash-lite", 0.10, 0.30),
    GEMINI_2_5_FLASH("gemini-2.5-flash", 0.15, 0.45),
    GEMINI_2_5_PRO("gemini-2.5-pro", 1.25, 3.75),

    // OpenAI Models (Latest generation)
    OPENAI_GPT_5_NANO("gpt-5-nano", 0.05, 0.15),
    OPENAI_GPT_5_MINI("gpt-5-mini", 0.25, 1.00),
    OPENAI_GPT_5_2("gpt-5.2", 1.75, 7.00),
    OPENAI_GPT_5_2_PRO("gpt-5.2-pro", 21.00, 84.00),
    OPENAI_O3_MINI("o3-mini", 1.10, 4.40),
    OPENAI_O3_PRO("o3-pro", 150.00, 600.00),

    // xAI (Grok) Models (Latest generation)
    GROK_4_1_FAST("grok-4-1-fast-non-reasoning", 0.20, 0.80),
    GROK_4_1_FAST_REASONING("grok-4-1-fast-reasoning", 0.30, 1.20);

    fun isGemini() = this.name.startsWith("GEMINI")
    fun isOpenAi() = this.name.startsWith("OPENAI")
    fun isGrok() = this.name.startsWith("GROK")
}
