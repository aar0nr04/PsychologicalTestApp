import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.Question
import com.example.psychologicaltestapp.R

class QuestionAdapter(
    private val context: Context,
    private val questions: List<Question>,
    private val onOptionSelected: (String) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(context, question, onOptionSelected)
    }

    override fun getItemCount(): Int = questions.size

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val imageQuestion: ImageView = itemView.findViewById(R.id.imageQuestion)
        private val imageOptionsContainer: LinearLayout = itemView.findViewById(R.id.imageOptionsContainer)

        fun bind(context: Context, question: Question, onOptionSelected: (String) -> Unit) {
            questionText.text = question.questionText

            // Mostrar imagen principal si existe
            if (question.imageQuestion != null) {
                val imageResId = context.resources.getIdentifier(question.imageQuestion, "drawable", context.packageName)
                if (imageResId != 0) {
                    imageQuestion.setImageResource(imageResId)
                    imageQuestion.visibility = View.VISIBLE
                } else {
                    imageQuestion.visibility = View.GONE
                }
            } else {
                imageQuestion.visibility = View.GONE
            }

            // Mostrar opciones de imagen si existen
            imageOptionsContainer.removeAllViews()
            if (!question.optionImages.isNullOrEmpty()) {
                imageOptionsContainer.visibility = View.VISIBLE
                question.optionImages.forEach { imageName ->
                    val optionImage = ImageView(context)
                    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                    if (resId != 0) {
                        optionImage.setImageResource(resId)
                        optionImage.layoutParams = LinearLayout.LayoutParams(0, 200).apply {
                            weight = 1f
                            marginEnd = 16
                        }
                        optionImage.setPadding(8, 8, 8, 8)
                        optionImage.scaleType = ImageView.ScaleType.FIT_CENTER
                        optionImage.setOnClickListener {
                            onOptionSelected(imageName)
                        }
                        imageOptionsContainer.addView(optionImage)
                    }
                }
            } else {
                imageOptionsContainer.visibility = View.GONE
            }
        }
    }
}
