package com.example.psychologicaltestapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.databinding.ActivityPsychologistDetailBinding

class PsychologistDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsychologistDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPsychologistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val psychologist = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PSYCHOLOGIST", Psychologist::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Psychologist>("PSYCHOLOGIST")
        }
        android.util.Log.d("TestApp", "Recibido en DetailActivity: $psychologist")

        psychologist?.let {
            binding.nameTextView.text = it.name
            binding.specialtyTextView.text = it.specialty
            binding.descriptionTextView.text = it.description

            if (it.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.profileImageView)
            } else {
                binding.profileImageView.setImageResource(R.drawable.placeholder_image)
            }
        } ?: run {
            finish()
        }
    }
}
