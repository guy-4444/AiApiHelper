package org.guy.library

import org.guy.library.models.AiModel
import java.io.File
import java.io.FileInputStream
import java.util.Properties

data class Joke(val genre: String, val content: String)
data class City(val name: String, val population: Int, val neighboringCities: List<String>?)
// 1. Define the nested classes first
data class Address(
    val street: String,
    val city: String,
    val country: String,
    val zipCode: String // Using String for zip code is safer to preserve leading zeros or letters
)

data class Game(
    val title: String,
    val genre: String
)

// 2. Define the main class that holds the nested class and the list
data class User(
    val name: String,
    val age: Int,
    val address: Address,
    val games: List<Game>
)

data class InvoiceItem(
    val name: String,
    val amount: Double,
    val priceEach: Double,
    val priceTotal: Double,
    val currency: String,
    val currencySymbol: String,
)

data class Invoice(
    val seller: String,
    val buyer: String,
    val amount: Double,
    val currency: String,
    val currencySymbol: String,
    val items: List<InvoiceItem>?
)

lateinit var OPENAI_API_KEY: String
lateinit var GROK_API_KEY: String
lateinit var GEMINI_API_KEY: String

fun main() {
    initSecretKeys()


    val ai = SimpleAi(GEMINI_API_KEY, AiModel.GEMINI_3_1_FLASH_LITE_PREVIEW)
    ai.logCost = true
//        val response = ai.ask("Tell me a funny programming joke.")
//        println(response)

//         The library automatically tells the AI to format it as JSON matching the Joke class!
//        val joke: Joke = ai.askForType("Tell me a networking joke")
//        println(joke.genre)   // Prints: "Networking"
//        println(joke.content) // Prints: "I'd tell you a joke about UDP, but you might not get it."


        // Ask the AI for a list of 10 major cities in Israel
//        val cities: List<City> = ai.askForType("Give me 10 major cities in Israel (name, population and neighboring Cities)")
//
//        // Loop through the list and print out the details for each city
//        println("--- 10 Major Cities in Israel ---")
//        cities.forEach { city ->
//            println("Name: ${city.name}")
//            println("Population: ${city.population}")
//            println("Neighbors: ${city.neighboringCities?.joinToString(", ") ?: "None"}")
//            println("---------------------------------")
//        }



    // Ask the AI to generate a profile matching our complex nested structure
    val pdfFile = File("invoice_demo.pdf")
    if (!pdfFile.exists()) {
        println("⚠️ Please place a file named 'invoice_demo.png' in the project root to test multimodal features.")
        return
    }

    println("Parsing invoice with AI model...")
    val prompt = "Analyze this attached invoice. Extract the seller, buyer, total amount, and a list of all items with their prices. Please validate the data to ensure it's accurate from the document."
    val invoice: Invoice = ai.askForType(prompt, pdfFile)

    println("\n--- Parsed Invoice Data ---")
    println("Seller: ${invoice.seller}")
    println("Buyer: ${invoice.buyer}")
    println("Total Amount: $${invoice.amount}${invoice.currencySymbol}")
    
    println("Items:")
    invoice.items?.forEach { item ->
        println("\t${item.name} - ${item.amount} x ${item.priceEach}${item.currencySymbol} - ${item.priceTotal}${item.currencySymbol}")
    }
}

fun initSecretKeys() {
    val properties = Properties()
    val propertiesFile = File("secrets.local.properties")
    if (propertiesFile.exists()) {
        FileInputStream(propertiesFile).use { inputStream ->
            properties.load(inputStream)
        }
        OPENAI_API_KEY = properties.getProperty("OPENAI_API_KEY")
        GROK_API_KEY = properties.getProperty("GROK_API_KEY")
        GEMINI_API_KEY = properties.getProperty("GEMINI_API_KEY")

        println("Success: got the keys.")
    } else {
        println("Error: secrets.local.properties file not found!")
    }
}
