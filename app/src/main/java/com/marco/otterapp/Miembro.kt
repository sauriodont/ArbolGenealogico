data class Miembro(
    val codigo: String,
    val nombre: String,
    val nombrePadre: String,
    val nombreMadre: String,
    val fnacimiento: String,
    val hijos: List<String> = listOf()
)