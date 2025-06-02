package com.example.psychologicaltestapp

import Psychologist
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

        val psychologist = intent.getParcelableExtra<Psychologist>("PSYCHOLOGIST")

        psychologist?.let {
            binding.nameTextView.text = it.name
            binding.specialtyTextView.text = it.specialty
            binding.descriptionTextView.text = it.description

            Glide.with(this)
                .load(it.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.profileImageView)
        }
    }
}