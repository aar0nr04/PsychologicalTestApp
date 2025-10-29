package com.example.psychologicaltestapp.location

import java.util.Locale

object LocationData {
    private val locations: Map<String, Map<String, List<String>>> = mapOf(
        "MX" to mapOf(
            "Aguascalientes" to listOf("Aguascalientes", "Jesús María", "Calvillo"),
            "Baja California" to listOf("Tijuana", "Mexicali", "Ensenada"),
            "Baja California Sur" to listOf("La Paz", "Los Cabos", "Loreto"),
            "Campeche" to listOf("Campeche", "Ciudad del Carmen", "Champotón"),
            "Chiapas" to listOf("Tuxtla Gutiérrez", "San Cristóbal de las Casas", "Tapachula"),
            "Chihuahua" to listOf("Chihuahua", "Ciudad Juárez", "Delicias"),
            "Ciudad de México" to listOf("Álvaro Obregón", "Coyoacán", "Miguel Hidalgo", "Tlalpan"),
            "Coahuila" to listOf("Saltillo", "Torreón", "Monclova"),
            "Estado de México" to listOf("Toluca", "Naucalpan", "Ecatepec"),
            "Guerrero" to listOf("Acapulco", "Chilpancingo", "Ixtapa Zihuatanejo"),
            "Jalisco" to listOf("Guadalajara", "Puerto Vallarta", "Zapopan"),
            "Nuevo León" to listOf("Monterrey", "San Nicolás de los Garza", "San Pedro Garza García"),
            "Puebla" to listOf("Puebla", "Atlixco", "San Andrés Cholula"),
            "Querétaro" to listOf("Querétaro", "San Juan del Río", "Tequisquiapan"),
            "Quintana Roo" to listOf("Cancún", "Playa del Carmen", "Tulum"),
            "Sinaloa" to listOf("Culiacán", "Mazatlán", "Los Mochis"),
            "Sonora" to listOf("Hermosillo", "Ciudad Obregón", "Nogales"),
            "Veracruz" to listOf("Veracruz", "Xalapa", "Coatzacoalcos"),
            "Yucatán" to listOf("Mérida", "Progreso", "Valladolid")
        ),
        "US" to mapOf(
            "California" to listOf("Los Ángeles", "San Francisco", "San Diego"),
            "Texas" to listOf("Austin", "Houston", "Dallas"),
            "New York" to listOf("New York", "Buffalo", "Rochester"),
            "Florida" to listOf("Miami", "Orlando", "Tampa"),
            "Illinois" to listOf("Chicago", "Springfield", "Naperville")
        ),
        "CA" to mapOf(
            "Alberta" to listOf("Calgary", "Edmonton", "Red Deer"),
            "British Columbia" to listOf("Vancouver", "Victoria", "Kelowna"),
            "Ontario" to listOf("Toronto", "Ottawa", "Mississauga"),
            "Quebec" to listOf("Montreal", "Quebec", "Laval")
        ),
        "AR" to mapOf(
            "Buenos Aires" to listOf("Buenos Aires", "La Plata", "Mar del Plata"),
            "Córdoba" to listOf("Córdoba", "Villa Carlos Paz", "Río Cuarto"),
            "Mendoza" to listOf("Mendoza", "San Rafael", "Godoy Cruz")
        ),
        "CO" to mapOf(
            "Antioquia" to listOf("Medellín", "Envigado", "Rionegro"),
            "Cundinamarca" to listOf("Bogotá", "Chía", "Zipaquirá"),
            "Valle del Cauca" to listOf("Cali", "Palmira", "Buenaventura")
        )
    )

    fun countries(): List<String> = locations.keys.sorted()

    fun statesFor(country: String): List<String> {
        val normalized = country.uppercase(Locale.US)
        return locations[normalized]?.keys?.sorted() ?: emptyList()
    }

    fun citiesFor(country: String, state: String): List<String> {
        if (state.isBlank()) return emptyList()
        val normalizedCountry = country.uppercase(Locale.US)
        val states = locations[normalizedCountry] ?: return emptyList()
        val entry = states.entries.firstOrNull { it.key.equals(state, ignoreCase = true) }
        return entry?.value?.sorted() ?: emptyList()
    }
}
