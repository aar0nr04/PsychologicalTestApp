package com.example.psychologicaltestapp.ui.tests

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.ComponentActivity
import com.example.psychologicaltestapp.R
import com.example.psychologicaltestapp.data.tests.*
import com.example.psychologicaltestapp.databinding.ActivityTestsCatalogBinding

class TestsCatalogActivity : ComponentActivity() {

    private lateinit var binding: ActivityTestsCatalogBinding
    private lateinit var index: IndexJson
    private var selectedGroup: Group? = null
    private var selectedCategory: Category? = null
    private var selectedSubcategory: Subcategory? = null
    private var selectedTest: TestItem? = null
    private val locale = "es" // luego: SharedPreferences o config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestsCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        index = TestsRepository.loadIndex(this)

        setupGroupDropdown()
        binding.btnStartTest.setOnClickListener { startSelectedTest() }
    }

    private fun setupGroupDropdown() {
        val groups = index.taxonomy.groups
        val labels = groups.map { it.title[locale] ?: it.title["en"] ?: it.id }
        binding.ddGroup.setAdapter(labels.toAdapter(binding.ddGroup))
        binding.ddGroup.setOnItemClickListener { _, _, pos, _ ->
            selectedGroup = groups[pos]
            selectedCategory = null; selectedSubcategory = null; selectedTest = null
            binding.ddCategory.setText(""); binding.ddSubcategory.setText(""); binding.ddTest.setText("")
            setupCategoryDropdown()
            updateButtonState()
        }
    }

    private fun setupCategoryDropdown() {
        val g = selectedGroup ?: return
        val cats = g.categories
        val labels = cats.map { it.title[locale] ?: it.title["en"] ?: it.id }
        binding.ddCategory.setAdapter(labels.toAdapter(binding.ddCategory))
        binding.ddCategory.setOnItemClickListener { _, _, pos, _ ->
            selectedCategory = cats[pos]
            selectedSubcategory = null; selectedTest = null
            binding.ddSubcategory.setText(""); binding.ddTest.setText("")
            setupSubcategoryDropdown()
            updateButtonState()
        }
    }

    private fun setupSubcategoryDropdown() {
        val c = selectedCategory ?: return
        val subs = c.subcategories
        val labels = subs.map { it.title[locale] ?: it.title["en"] ?: it.id }
        binding.ddSubcategory.setAdapter(labels.toAdapter(binding.ddSubcategory))
        binding.ddSubcategory.setOnItemClickListener { _, _, pos, _ ->
            selectedSubcategory = subs[pos]
            selectedTest = null
            binding.ddTest.setText("")
            setupTestsDropdown()
            updateButtonState()
        }
    }

    private fun setupTestsDropdown() {
        val g = selectedGroup?.id ?: return
        val c = selectedCategory?.id ?: return
        val s = selectedSubcategory?.id ?: return
        val tests = index.tests.filter { it.group == g && it.category == c && it.subcategory == s }
        val labels = tests.map { it.title[locale] ?: it.title["en"] ?: it.slug }
        binding.ddTest.setAdapter(labels.toAdapter(binding.ddTest))
        binding.ddTest.setOnItemClickListener { _, _, pos, _ ->
            selectedTest = tests[pos]
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        binding.btnStartTest.isEnabled = selectedTest != null
    }

    private fun startSelectedTest() {
        val test = selectedTest ?: return
        // Aquí puedes mostrar intersticial #2 si lo tienes precargado.
        val i = Intent(this, TestRunnerActivity::class.java).apply {
            putExtra("slug", test.slug)
            putExtra("version", test.latestVersion)
            putExtra("locale", locale)
        }
        startActivity(i)
    }

    // Extension para crear adapters rápido
    private fun List<String>.toAdapter(view: AutoCompleteTextView): ArrayAdapter<String> =
        ArrayAdapter(view.context, android.R.layout.simple_list_item_1, this)
}
