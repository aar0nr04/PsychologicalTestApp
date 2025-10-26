package com.example.psychologicaltestapp.ui.tests

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.psychologicaltestapp.TestActivity
import com.example.psychologicaltestapp.data.tests.*
import com.example.psychologicaltestapp.databinding.ActivityTestsCatalogBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputLayout

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

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

        binding.ddGroup.applyRefinedDropdown(binding.tilGroup)
        binding.ddCategory.applyRefinedDropdown(binding.tilCategory)
        binding.ddSubcategory.applyRefinedDropdown(binding.tilSubcategory)
        binding.ddTest.applyRefinedDropdown(binding.tilTest)

        try {
            index = TestsRepository.loadIndex(this, locale)
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
        val adapter = RefinedDropdownAdapter(view.context, items)
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
            binding.btnStartTest.isEnabled = false
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
            binding.btnStartTest.isEnabled = false
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
        val locale = "es"
        if (!TestsRepository.hasTestPayload(this, test.slug, test.latestVersion, locale)) {
            Toast.makeText(this, getString(R.string.test_payload_missing, test.slug), Toast.LENGTH_LONG).show()
            return
        }

        val i = Intent(this, TestActivity::class.java).apply {
            putExtra("slug", test.slug)
            putExtra("version", test.latestVersion)
            putExtra("locale", locale)
        }
        startActivity(i)
    }

    private fun AutoCompleteTextView.applyRefinedDropdown(container: TextInputLayout) {
        setDropDownBackgroundResource(R.drawable.bg_refined_dropdown_popup)

        val primaryColor = MaterialColors.getColor(
            container,
            com.google.android.material.R.attr.colorPrimary,
            0
        )
        val accentColor = MaterialColors.getColor(
            container,
            com.google.android.material.R.attr.colorSecondary,
            primaryColor
        )
        val baseStrokeColor = container.boxStrokeColor
        val baseBackgroundColor = container.boxBackgroundColor
        val elevatedBackground = MaterialColors.layer(
            container,
            baseBackgroundColor,
            accentColor,
            0.08f
        )

        val interpolator = FastOutSlowInInterpolator()
        var strokeAnimator: ValueAnimator? = null
        var backgroundAnimator: ValueAnimator? = null
        var isElevated = false

        fun animateStroke(toColor: Int) {
            strokeAnimator?.cancel()
            strokeAnimator = ValueAnimator.ofArgb(container.boxStrokeColor, toColor).apply {
                duration = 220
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener { animation ->
                    container.boxStrokeColor = animation.animatedValue as Int
                }
                start()
            }
        }

        fun animateBackground(toColor: Int) {
            backgroundAnimator?.cancel()
            backgroundAnimator = ValueAnimator.ofArgb(container.boxBackgroundColor, toColor).apply {
                duration = 220
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener { animation ->
                    container.boxBackgroundColor = animation.animatedValue as Int
                }
                start()
            }
        }

        fun elevate() {
            if (isElevated) return
            isElevated = true
            container.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(200)
                .setInterpolator(interpolator)
                .start()
            animateStroke(primaryColor)
            animateBackground(elevatedBackground)
        }

        fun reset() {
            if (!isElevated) return
            isElevated = false
            container.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(interpolator)
                .start()
            animateStroke(baseStrokeColor)
            animateBackground(baseBackgroundColor)
        }

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) elevate() else reset()
        }

        setOnDismissListener { reset() }

        setOnClickListener {
            if (!isPopupShowing) {
                post { showDropDown() }
            }
            elevate()
        }
    }

    private class RefinedDropdownAdapter(
        context: android.content.Context,
        items: Array<String>
    ) : ArrayAdapter<String>(context, R.layout.item_refined_dropdown, R.id.tvItemLabel, items) {

        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val interpolator = FastOutSlowInInterpolator()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.item_refined_dropdown, parent, false)
            val label = view.findViewById<TextView>(R.id.tvItemLabel)
            label.text = getItem(position)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = getView(position, convertView, parent)
            if (convertView == null) {
                view.alpha = 0f
                view.translationY = -16f
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(180)
                    .setInterpolator(interpolator)
                    .start()
            } else {
                view.alpha = 1f
                view.translationY = 0f
            }
            return view
        }
    }

}
