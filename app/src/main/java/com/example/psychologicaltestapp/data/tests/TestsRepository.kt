package com.example.psychologicaltestapp.data.tests

import android.content.Context
import com.google.gson.Gson

object TestsRepository {
    private val gson = Gson()

    fun loadIndex(context: Context): IndexJson {
        context.assets.open("tests/index.json").use { stream ->
            return gson.fromJson(stream.reader(), IndexJson::class.java)
        }
    }

    fun loadTestPayload(context: Context, slug: String, version: String, locale: String): String {
        val path = "tests/$slug/$version/test.$locale.json"
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
