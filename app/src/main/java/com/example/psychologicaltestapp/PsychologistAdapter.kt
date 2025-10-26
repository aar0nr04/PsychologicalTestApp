package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.databinding.ItemPsychologistBinding

class PsychologistAdapter(
    private var psychologists: List<Psychologist>,
    private val onPsychologistClick: (Psychologist) -> Unit
) : RecyclerView.Adapter<PsychologistAdapter.PsychologistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PsychologistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPsychologistBinding.inflate(inflater, parent, false)
        return PsychologistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PsychologistViewHolder, position: Int) {
        val psychologist = psychologists[position]
        holder.bind(psychologist)
        holder.itemView.setOnClickListener { onPsychologistClick(psychologist) }
    }

    override fun getItemCount(): Int = psychologists.size

    class PsychologistViewHolder(
        private val binding: ItemPsychologistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(psychologist: Psychologist) {
            binding.nameTextView.text = psychologist.name
            binding.specialtyTextView.text = psychologist.specialty
            binding.descriptionTextView.text = psychologist.description

            Glide.with(binding.root.context)
                .load(psychologist.imageUrl)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(binding.profileImageView)
        }
    }
    fun updateList(newList: List<Psychologist>) {
        psychologists = newList
        notifyDataSetChanged()
    }


}
