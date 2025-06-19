
// TestRepository.kt
package com.example.psychologicaltestapp
import android.content.Context
import com.google.gson.Gson

class TestRepository(private val context: Context) {

    private val cache = mutableMapOf<String, Test>()

    fun getTest(testType: String, language: String): Test {
        val key = "$testType-$language"
        cache[key]?.let { return it } // si ya está en caché

        val fileName = "tests/${testType}_${language}.json"
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val category = Gson().fromJson(json, TestCategory::class.java)
        val test = category.tests.firstOrNull()
            ?: throw IllegalArgumentException("No se encontró test en $fileName")

        cache[key] = test
        return test
    }
}
