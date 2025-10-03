package com.example.psychologicaltestapp.data.tests

data class IndexJson(
    val version: Int,
    val updatedAt: String,
    val taxonomy: Taxonomy,
    val tests: List<TestItem>
)

data class Taxonomy(val groups: List<Group>)

data class Group(
    val id: String,
    val title: Map<String, String>,
    val categories: List<Category>
)

data class Category(
    val id: String,
    val title: Map<String, String>,
    val subcategories: List<Subcategory>
)

data class Subcategory(
    val id: String,
    val title: Map<String, String>
)

data class TestItem(
    val slug: String,
    val title: Map<String, String>,
    val summary: Map<String, String>?,
    val group: String,
    val category: String,
    val subcategory: String,
    val tags: List<String>?,
    val latestVersion: String,
    val locales: List<String>,
    val durationMin: Int?,
    val questionCount: Int?,
    val access: String,
    val license: String
)
