# Simple AiApi Library (Android & Kotlin/Java)

[![](https://jitpack.io/v/guy-4444/AiApiHelper.svg)](https://jitpack.io/#guy-4444/AiApiHelper)

A lightweight, zero-dependency helper library to seamlessly connect your Android or Java/Kotlin applications to modern AI APIs like Google Gemini, OpenAI, and xAI. 

Designed specifically for students and hobbyists to bypass complex REST configuration and focus solely on AI integration.

## Features
- **Plug and Play**: Swap between Google Gemini, OpenAI, and Grok with just an enum change.
- **Structured JSON Parsing**: Ask the AI to return data in a specific format (`data class`), and the library automatically enforces the schema and parses it seamlessly using Gson.
- **Cost Tracking**: Logs estimated USD cost based on token usage.
- **Built for Android**: Compiled with Java 11 bytecode to ensure 100% compatibility across all modern Android versions.

---

## Installation 

### Gradle (Kotlin DSL)

**1. Add the Repository**
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

**2. Add the Dependency**
In your module-level `build.gradle.kts` (usually `app/build.gradle.kts`), add the dependency:
```kotlin
dependencies {
    implementation("com.github.guy-4444:aiapi:1.01") 
}
```

### Maven (`pom.xml`)
If you are using a pure Java Maven project, add JitPack to your repositories and then add the dependency constraint:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.guy-4444</groupId>
    <artifactId>aiapi</artifactId>
    <version>1.01</version>
</dependency>
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

### Complex Typed Objects (Data Classes)
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

### Fetching Lists of Objects
You can ask the AI to generate a `List` of objects directly. The library handles the root JSON array parsing automatically.

```kotlin
data class City(val name: String, val population: Int)

fun main() {
    val ai = SimpleAi("your-api-key", AiModel.GEMINI_3_1_FLASH_PREVIEW)
    
    // Ask for a list by explicitly typing it as List<City>
    val cities: List<City> = ai.askForType("Give me the top 3 major cities in Israel.")
    
    cities.forEach { city ->
        println("${city.name} - Pop: ${city.population}")
    }
}
```

### Deeply Nested Output (Lists and Sub-Objects)
You can even nest properties and arrays. Simply define the models, and the library handles the rest.
*(Note: Be sure to use nullable types `?` for nested properties to prevent Gson extraction crashes on empty AI outputs).*

```kotlin
data class Address(val street: String, val city: String)
data class User(val name: String, val age: Int, val address: Address?, val tags: List<String>?)

fun main() {
    val ai = SimpleAi("your-api-key", AiModel.GEMINI_3_1_FLASH_PREVIEW)
    val user: User = ai.askForType("Generate a random fictional user profile living in Paris with 3 hobby tags.")
    
    println("Name: ${user.name}")
    println("City: ${user.address?.city}")
    println("Tags: ${user.tags?.joinToString()}")
}
```

### Tracking Costs
To view how much your API calls are costing regarding exact token usage:
```kotlin
ai.logCost = true

// ... any `ai.ask` or `ai.askForType` call

// Console Output:
// [AiApi Cost Logger]
// Model: gemini-3.1-flash-lite-preview
// Tokens: 120 in | 45 out
// Est. Cost: $0.000031
```
