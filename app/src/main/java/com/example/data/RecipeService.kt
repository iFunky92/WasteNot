package com.example.data

import com.example.BuildConfig
import com.example.api.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class RecipeSuggestion(
    val name: String,
    val usedIngredients: List<String>,
    val otherIngredients: List<String>,
    val instructions: List<String>,
    val wasteTip: String
)

data class RecipeResponse(
    val recipes: List<RecipeSuggestion>
)

object RecipeService {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(RecipeResponse::class.java)

    suspend fun getRecipeSuggestions(nearingExpiryProducts: List<Product>): List<RecipeSuggestion> {
        if (nearingExpiryProducts.isEmpty()) {
            return emptyList()
        }

        val productsListString = nearingExpiryProducts.joinToString("\n") { product ->
            "- ${product.name} (expiring in ${product.getDaysRemaining()} days, qty: ${product.quantity})"
        }

        val prompt = """
            I have the following grocery items nearing their expiration date and I want to minimize food waste:
            $productsListString
            
            Please suggest 2 or 3 creative, delicious recipes that can help me use these ingredients. For each recipe, provide:
            1. Recipe Name
            2. Ingredients (specifically highlighting which of the nearing-expiry items are used, and listing other common pantry staples needed)
            3. Simple Step-by-Step Instructions
            4. Waste Reduction Tip: A short tip on how this recipe or these ingredients help reduce food waste.
            
            Please return the response as a valid JSON object in the following format:
            {
              "recipes": [
                {
                  "name": "Recipe Name",
                  "usedIngredients": ["Milk", "Spinach"],
                  "otherIngredients": ["Eggs", "Butter", "Salt"],
                  "instructions": ["Step 1...", "Step 2..."],
                  "wasteTip": "Using spinach that is about to wilt in an omelette preserves its nutrients and keeps it out of the bin."
                }
              ]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a professional chef specializing in zero-waste cooking. Your goal is to suggest simple, practical, and delicious meals using ingredients that are about to expire. Always return valid, well-structured JSON matching the requested schema."))
            )
        )

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            throw IllegalStateException("Gemini API Key is not set. Please set it in the Secrets panel in Google AI Studio.")
        }

        val apiResponse = RetrofitClient.service.generateContent(apiKey, request)
        val jsonText = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from Gemini API.")

        return try {
            val response = adapter.fromJson(jsonText)
            response?.recipes ?: emptyList()
        } catch (e: Exception) {
            throw Exception("Failed to parse recipes JSON: ${e.message}\nRaw response was: $jsonText", e)
        }
    }
}
