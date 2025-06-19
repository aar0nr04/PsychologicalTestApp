package com.example.psychologicaltestapp.data

import android.content.Context
import com.example.psychologicaltestapp.CategorySummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryRepository(private val context: Context) {

    fun getCategories(language: String): List<CategorySummary> {
        val fileName = "tests/categories_${language}.json"
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }

        val listType = object : TypeToken<List<CategorySummary>>() {}.type
        return Gson().fromJson(json, listType)
    }
}
