# Simple AiApi Library (Android & Kotlin/Java)

A lightweight, zero-dependency helper library to seamlessly connect your Android or Java/Kotlin applications to modern AI APIs like Google Gemini, OpenAI, and xAI. 

Designed specifically for students and hobbyists to bypass complex REST configuration and focus solely on AI integration.

## Features
- **Plug and Play**: Swap between Google Gemini, OpenAI, and Grok with just an enum change.
- **Structured JSON Parsing**: Ask the AI to return data in a specific format (`data class`), and the library automatically enforces the schema and parses it seamlessly using Gson.
- **Cost Tracking**: Logs estimated USD cost based on token usage.
- **Built for Android**: Compiled with Java 11 bytecode to ensure 100% compatibility across all modern Android versions.

---

## Installation (JitPack)

### 1. Add the Repository
In your project-level `settings.gradle.kts` (or `build.gradle`), ensure JitPack is added to your repositories block:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the Dependency
In your module-level `build.gradle.kts` (usually `app/build.gradle.kts`), add the dependency:
```kotlin
dependencies {
    // Replace 'YourUsername' with the GitHub username hosting the repo
    implementation("com.github.YourUsername:aiapi:1.0.0") 
}
```

---

## Usage

### Simple Text Prompts
To ask the AI a simple text-based question, provide an API key and a supported Model variant:

```kotlin
import org.guy.library.SimpleAi
import org.guy.library.models.AiModel

fun main() {
    val ai = SimpleAi("your-api-key-here", AiModel.GEMINI_3_1_FLASH_LITE_PREVIEW)
    
    val response = ai.ask("Tell me a short poem about coding.")
    println(response)
}
```

### Complex Typed Objects
You can define a Kotlin `data class` with any nested structure you want, and the AI will populate it.

```kotlin
data class Character(val name: String, val classType: String, val level: Int)

fun main() {
    val ai = SimpleAi("your-api-key-here", AiModel.OPENAI_GPT_5_MINI)
    
    // The library automatically tells the AI to format its JSON exactly like the Character class!
    val character: Character = ai.askForType("Create a level 5 wizard character.")
    
    println(character.name)      // Auto-Generated Name
    println(character.classType) // "Wizard"
    println(character.level)     // 5
}
```

### Tracking Costs
To view how much your API calls are costing regarding token usage:
```kotlin
ai.logCost = true

// ... any `ai.ask` or `ai.askForType` call

// Console Output:
// [AiApi Cost Logger]
// Model: gemini-3.1-flash-lite-preview
// Tokens: 120 in | 45 out
// Est. Cost: $0.000031
```
