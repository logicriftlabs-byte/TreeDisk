package com.example

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object AIAssistant {
    
    suspend fun categorizeFiles(fileNames: List<String>): Map<String, List<String>> = withContext(Dispatchers.IO) {
        if (fileNames.isEmpty()) return@withContext emptyMap()
        
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            throw Exception("Gemini API Key is missing. Please add it to Settings / .env")
        }
        
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
        )
        
        val prompt = """
            I have a list of file names. Group them into a small number of logical, descriptive folders based on their type, project, or purpose.
            Return ONLY a valid JSON object where keys are folder names and values are arrays of file names. Do not include markdown code blocks.
            Files:
            ${fileNames.joinToString(", ")}
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        val text = response.text ?: "{}"
        
        val cleanText = text.replace("```json", "").replace("```", "").trim()
        
        val map = mutableMapOf<String, List<String>>()
        try {
            val json = JSONObject(cleanText)
            json.keys().forEach { folderName ->
                val filesArray = json.getJSONArray(folderName)
                val files = mutableListOf<String>()
                for (i in 0 until filesArray.length()) {
                    files.add(filesArray.getString(i))
                }
                map[folderName] = files
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        map
    }
}
