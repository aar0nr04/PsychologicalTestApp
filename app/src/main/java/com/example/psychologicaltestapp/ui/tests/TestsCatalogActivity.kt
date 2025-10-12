package com.example.psychologicaltestapp.ui.tests

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.psychologicaltestapp.TestActivity
import com.example.psychologicaltestapp.data.tests.*
import com.example.psychologicaltestapp.databinding.ActivityTestsCatalogBinding

class TestsCatalogActivity : ComponentActivity() {

    private lateinit var binding: ActivityTestsCatalogBinding
    private lateinit var index: IndexJson

    private var selectedGroup: Group? = null
    private var selectedCategory: Category? = null
    private var selectedSubcategory: Subcategory? = null
    private var selectedTest: TestItem? = null

    // Usa "es" si tu index.json tiene títulos en español. Si no, cambia a "en".
    private val locale = "es"

    private fun String.safeLocale(map: Map<String, String>?, fallbackKey: String = "en"): String {
        if (map == null) return this
        return map[this] ?: map[fallbackKey] ?: map.values.firstOrNull() ?: this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestsCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            index = TestsRepository.loadIndex(this)
            Toast.makeText(
                this,
                "Grupos: ${index.taxonomy.groups.size} | Tests: ${index.tests.size}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error leyendo index.json: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupGroupDropdown()
        binding.btnStartTest.setOnClickListener { startSelectedTest() }

        // Abre el primer dropdown para que el usuario vea opciones
        binding.ddGroup.postDelayed({ binding.ddGroup.showDropDown() }, 300)
    }

    // ---------- Helpers ----------
    private fun setAdapterFor(view: AutoCompleteTextView, items: Array<String>) {
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, items)
        view.setAdapter(adapter)
    }

    private fun updateButtonState() {
        binding.btnStartTest.isEnabled = selectedTest != null
    }

    // ---------- Cascada ----------
    private fun setupGroupDropdown() {
        val groups = index.taxonomy.groups
        if (groups.isEmpty()) {
            Toast.makeText(this, "taxonomy.groups vacío en index.json", Toast.LENGTH_LONG).show()
            return
        }
        val labels = groups.map { locale.safeLocale(it.title, "en") }.toTypedArray()
        setAdapterFor(binding.ddGroup, labels)

        binding.ddGroup.setOnItemClickListener { _, _, pos, _ ->
            selectedGroup = groups[pos]
            selectedCategory = null; selectedSubcategory = null; selectedTest = null
            binding.ddCategory.setText(""); binding.ddSubcategory.setText(""); binding.ddTest.setText("")
            setupCategoryDropdown()
            updateButtonState()
            binding.ddCategory.post { binding.ddCategory.showDropDown() }
        }
    }

    private fun setupCategoryDropdown() {
        val g = selectedGroup ?: return
        val cats = g.categories
        val labels = cats.map { locale.safeLocale(it.title, "en") }.toTypedArray()
        setAdapterFor(binding.ddCategory, labels)

        binding.ddCategory.setOnItemClickListener { _, _, pos, _ ->
            selectedCategory = cats[pos]
            selectedSubcategory = null; selectedTest = null
            binding.ddSubcategory.setText(""); binding.ddTest.setText("")
            setupSubcategoryDropdown()
            updateButtonState()
            binding.ddSubcategory.post { binding.ddSubcategory.showDropDown() }
        }
    }

    private fun setupSubcategoryDropdown() {
        val c = selectedCategory ?: return
        val subs = c.subcategories
        val labels = subs.map { locale.safeLocale(it.title, "en") }.toTypedArray()
        setAdapterFor(binding.ddSubcategory, labels)

        binding.ddSubcategory.setOnItemClickListener { _, _, pos, _ ->
            selectedSubcategory = subs[pos]
            selectedTest = null
            binding.ddTest.setText("")
            setupTestsDropdown()
            updateButtonState()
            binding.ddTest.post { binding.ddTest.showDropDown() }
        }
    }

    private fun setupTestsDropdown() {
        val g = selectedGroup?.id ?: return
        val c = selectedCategory?.id ?: return
        val s = selectedSubcategory?.id ?: return

        val tests = index.tests.filter { it.group == g && it.category == c && it.subcategory == s }
        if (tests.isEmpty()) {
            Toast.makeText(this, "No hay tests para $g/$c/$s", Toast.LENGTH_SHORT).show()
        }

        val labels = tests.map { locale.safeLocale(it.title, "en") }.toTypedArray()
        setAdapterFor(binding.ddTest, labels)

        binding.ddTest.setOnItemClickListener { _, _, pos, _ ->
            selectedTest = tests[pos]
            updateButtonState()
        }
    }

    // ---------- Navegación ----------
    private fun startSelectedTest() {
        val test = selectedTest ?: return
        val i = Intent(this, TestActivity::class.java).apply {
            putExtra("slug", test.slug)
            putExtra("version", test.latestVersion)
            putExtra("locale", "es") // o el que uses
        }
        startActivity(i)
    }

}
