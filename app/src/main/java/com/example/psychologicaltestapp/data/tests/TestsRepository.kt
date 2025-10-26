package com.example.psychologicaltestapp.data.tests

import android.content.Context
import com.google.gson.Gson

object TestsRepository {
    private val gson = Gson()

    fun loadIndex(context: Context, locale: String = "es"): IndexJson {
        val rawIndex = context.assets.open("tests/index.json").use { stream ->
            gson.fromJson(stream.reader(), IndexJson::class.java)
        }

        val availableTests = rawIndex.tests.filter { test ->
            test.locales.contains(locale) && hasTestPayload(context, test.slug, test.latestVersion, locale)
        }

        val filteredGroups = rawIndex.taxonomy.groups.mapNotNull { group ->
            val filteredCategories = group.categories.mapNotNull { category ->
                val filteredSubcategories = category.subcategories.filter { subcategory ->
                    availableTests.any { it.group == group.id && it.category == category.id && it.subcategory == subcategory.id }
                }
                if (filteredSubcategories.isNotEmpty()) {
                    category.copy(subcategories = filteredSubcategories)
                } else {
                    null
                }
            }

            if (filteredCategories.isNotEmpty()) {
                group.copy(categories = filteredCategories)
            } else {
                null
            }
        }

        return rawIndex.copy(
            tests = availableTests,
            taxonomy = rawIndex.taxonomy.copy(groups = filteredGroups)
        )
    }

    fun loadTestPayload(context: Context, slug: String, version: String, locale: String): String {
        val path = "tests/$slug/$version/test.$locale.json"
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun hasTestPayload(context: Context, slug: String, version: String, locale: String): Boolean {
        val path = "tests/$slug/$version/test.$locale.json"
        return runCatching {
            context.assets.open(path).use { }
        }.isSuccess
    }
}
