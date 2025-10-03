package com.example.psychologicaltestapp.data.tests

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class TestsCatalogViewModel(app: Application) : AndroidViewModel(app) {
    private val index by lazy { TestsRepository.loadIndex(app) }

    val groups = index.taxonomy.groups

    fun categoriesFor(groupId: String) =
        groups.first { it.id == groupId }.categories

    fun subcategoriesFor(groupId: String, categoryId: String) =
        categoriesFor(groupId).first { it.id == categoryId }.subcategories

    fun testsFor(groupId: String, categoryId: String, subcategoryId: String): List<TestItem> =
        index.tests.filter { it.group == groupId && it.category == categoryId && it.subcategory == subcategoryId }

    fun appLocale(): String = "es" // luego lo tomas de preferencias
}
