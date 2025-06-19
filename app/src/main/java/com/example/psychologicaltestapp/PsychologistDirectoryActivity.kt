import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.R

class PsychologistDirectoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PsychologistAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psychologist_directory)

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val psychologists = listOf(
            Psychologist(
                id = "1",
                name = "Dra. Ana Gómez",
                specialty = "Terapia Cognitivo-Conductual",
                location = "Ciudad de México",
                phone = "555-1234",
                email = "ana@example.com",
                description = "Especialista en terapias cognitivas",
                imageUrl = ""
            ),
            Psychologist(
                id = "2",
                name = "Dr. Luis Pérez",
                specialty = "Psicoanálisis",
                location = "Guadalajara",
                phone = "555-5678",
                email = "luis@example.com",
                description = "Experto en psicoanálisis",
                imageUrl = ""
            )
        )

        adapter = PsychologistAdapter(psychologists) { psychologist ->
            Toast.makeText(this, "Seleccionaste a ${psychologist.name}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = adapter
    }
}
