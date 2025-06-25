import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.R

class PsychologistAdapter(
    private var psychologists: List<Psychologist>,
    private val onPsychologistClick: (Psychologist) -> Unit
) : RecyclerView.Adapter<PsychologistAdapter.PsychologistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PsychologistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_psychologist, parent, false)
        return PsychologistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PsychologistViewHolder, position: Int) {
        val psychologist = psychologists[position]
        holder.bind(psychologist)
        holder.itemView.setOnClickListener { onPsychologistClick(psychologist) }
    }

    override fun getItemCount(): Int = psychologists.size

    class PsychologistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.specialtyTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(psychologist: Psychologist) {
            nameTextView.text = psychologist.name
            specialtyTextView.text = psychologist.specialty
            descriptionTextView.text = psychologist.description

            // Load image using Glide or Picasso
            Glide.with(itemView.context)
                .load(psychologist.imageUrl)
                .placeholder(R.drawable.default_profile_image) // tu vector drawable
                .error(R.drawable.default_profile_image)       // por si falla
                .into(profileImageView)
        }
    }
    fun updateList(newList: List<Psychologist>) {
        psychologists = newList
        notifyDataSetChanged()
    }


}