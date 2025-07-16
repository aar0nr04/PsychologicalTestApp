package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import android.widget.Toast
import com.example.psychologicaltestapp.databinding.ActivityTestListBinding

class TestListActivity : BaseActivity() {

    private lateinit var binding: ActivityTestListBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        loadInterstitialAd()

        try {
            val categories = loadTestsFromJson(this)

            binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
            categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
                showTestsInCategory(selectedCategory)
            }
            binding.categoriesRecyclerView.adapter = categoryAdapter

            binding.backButton.setOnClickListener {
                finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar las categorías", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // ID de prueba
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun showTestsInCategory(category: Category) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    navigateToTestDetails(category)
                    loadInterstitialAd() // recarga el ad para la próxima vez
                }
                // Eliminamos onAdFailedToShowFullScreenContent para evitar error
            }
            interstitialAd?.show(this)
        } else {
            navigateToTestDetails(category)
        }
    }

    private fun navigateToTestDetails(category: Category) {
        val intent = Intent(this, TestDetailsActivity::class.java)
        intent.putExtra("CATEGORY", Gson().toJson(category))
        startActivity(intent)
    }
}
