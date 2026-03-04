package org.guy.library

import org.guy.library.clients.GeminiClient
import org.guy.library.clients.OpenAiClient
import org.guy.library.models.AiModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class SimpleAiTest {

    @Test
    fun `test GeminiClient json parsing`() {
        val client = GeminiClient("dummy", AiModel.GEMINI_3_1_FLASH_PREVIEW)
        val parseMethod = GeminiClient::class.java.getDeclaredMethod("parseResponse", String::class.java)
        parseMethod.isAccessible = true

        val dummyResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "Hello from Gemini!"
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = parseMethod.invoke(client, dummyResponse) as String
        assertEquals("Hello from Gemini!", result)
    }

    @Test
    fun `test OpenAiClient json parsing`() {
        val client = OpenAiClient("dummy", AiModel.OPENAI_GPT_5_2)
        val parseMethod = OpenAiClient::class.java.getDeclaredMethod("parseResponse", String::class.java)
        parseMethod.isAccessible = true

        val dummyResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "content": "Hello from OpenAI!"
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = parseMethod.invoke(client, dummyResponse) as String
        assertEquals("Hello from OpenAI!", result)
    }

    @Test
    fun `test SimpleAi object creation`() {
        val aiGemini = SimpleAi("key1", AiModel.GEMINI_3_1_FLASH_LITE_PREVIEW)
        val aiOpenAi = SimpleAi("key2", AiModel.OPENAI_GPT_5_MINI)
        val aiGrok = SimpleAi("key3", AiModel.GROK_4_1_FAST)

        assertNotNull(aiGemini)
        assertNotNull(aiOpenAi)
        assertNotNull(aiGrok)
    }

    data class Joke(val genre: String, val content: String)

    @Test
    fun `test GeminiClient json object parsing`() {
        val client = GeminiClient("dummy", AiModel.GEMINI_3_1_FLASH_PREVIEW)
        val parseMethod = GeminiClient::class.java.getDeclaredMethod("parseResponse", String::class.java)
        parseMethod.isAccessible = true

        val dummyResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "{\"genre\": \"Programming\", \"content\": \"Why do programmers prefer dark mode? Because light attracts bugs.\"}"
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val jsonText = parseMethod.invoke(client, dummyResponse) as String
        val joke = com.google.gson.Gson().fromJson(jsonText, Joke::class.java)
        
        assertEquals("Programming", joke.genre)
        assertEquals("Why do programmers prefer dark mode? Because light attracts bugs.", joke.content)
    }

    @Test
    fun `test OpenAiClient json object parsing`() {
        val client = OpenAiClient("dummy", AiModel.OPENAI_GPT_5_2)
        val parseMethod = OpenAiClient::class.java.getDeclaredMethod("parseResponse", String::class.java)
        parseMethod.isAccessible = true

        val dummyResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "content": "{\"genre\": \"Dad Joke\", \"content\": \"I'm reading a book on anti-gravity. I can't put it down.\"}"
                        }
                    }
                ]
            }
        """.trimIndent()

        val jsonText = parseMethod.invoke(client, dummyResponse) as String
        val joke = com.google.gson.Gson().fromJson(jsonText, Joke::class.java)

        assertEquals("Dad Joke", joke.genre)
        assertEquals("I'm reading a book on anti-gravity. I can't put it down.", joke.content)
    }
}
