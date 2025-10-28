package com.example.psychologicaltestapp.ui.premium

import android.os.Bundle
import android.widget.Toast
import android.view.ViewGroup
import com.example.psychologicaltestapp.BaseActivity
import com.example.psychologicaltestapp.R
import com.example.psychologicaltestapp.databinding.ActivityPremiumPlansBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class PremiumPlansActivity : BaseActivity() {

    private lateinit var binding: ActivityPremiumPlansBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_plans)
        val container = findViewById<ViewGroup>(R.id.content_frame)
        binding = ActivityPremiumPlansBinding.bind(container.getChildAt(0))

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.btnFreePlan.setOnClickListener {
            Toast.makeText(this, getString(R.string.premium_plan_toast_free), Toast.LENGTH_SHORT).show()
        }

        val paidMessage = getString(R.string.premium_plan_toast_paid)
        binding.btnProPlan.setOnClickListener {
            Toast.makeText(this, paidMessage, Toast.LENGTH_SHORT).show()
        }
        binding.btnPremiumPlan.setOnClickListener {
            Toast.makeText(this, paidMessage, Toast.LENGTH_SHORT).show()
        }

        binding.btnContactSales.setOnClickListener {
            Toast.makeText(this, getString(R.string.premium_plan_contact_message), Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }
}
