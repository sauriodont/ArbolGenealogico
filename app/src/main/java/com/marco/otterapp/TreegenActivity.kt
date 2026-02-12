package com.marco.otterapp

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TreegenActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var buscarButton: Button
    private lateinit var volverButton: Button
    private lateinit var tableLayout: TableLayout

    private lateinit var sortNombre: TextView
    private lateinit var sortApellido: TextView
    private lateinit var sortFnac: TextView
    private lateinit var sortEdad: TextView

    private var datos: MutableList<Persona> = mutableListOf()
    private var ordenActual = Ordenamiento.NINGUNO
    private var ascendente = true

    enum class Ordenamiento {
        NOMBRE, FNAC, EDAD, NINGUNO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treegen)

        spinner = findViewById(R.id.menudown)
        buscarButton = findViewById(R.id.buscar)
        volverButton = findViewById(R.id.boton3)
        tableLayout = findViewById(R.id.tableLayout)

        sortNombre = findViewById(R.id.textViewnombre)
        sortApellido = findViewById(R.id.textViewapellido)
        sortFnac = findViewById(R.id.textViewfnac)
        sortEdad = findViewById(R.id.textViewedad)

        val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, meses)

        buscarButton.setOnClickListener {
            val mesSeleccionado = spinner.selectedItem.toString()
            consultarCumpleaños(mesSeleccionado)
        }

        volverButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        sortNombre.setOnClickListener {
            toggleOrdenamiento(Ordenamiento.NOMBRE)
            datos.sortWith(compareBy { it.nombres.lowercase() })
            if (!ascendente) datos.reverse()
            mostrarTabla()
        }

        sortFnac.setOnClickListener {
            toggleOrdenamiento(Ordenamiento.FNAC)
            datos.sortWith(compareBy {
                extraerDia(it.fnacimiento)
            })
            if (!ascendente) datos.reverse()
            mostrarTabla()
        }

        sortEdad.setOnClickListener {
            toggleOrdenamiento(Ordenamiento.EDAD)
            datos.sortWith(compareBy { calcularEdad(it.fnacimiento) })
            if (!ascendente) datos.reverse()
            mostrarTabla()
        }
    }

    private fun toggleOrdenamiento(nuevoOrden: Ordenamiento) {
        ascendente = if (ordenActual == nuevoOrden) !ascendente else true
        ordenActual = nuevoOrden
    }

    private fun consultarCumpleaños(mes: String) {
        datos.clear()
        tableLayout.removeAllViews()

        val dbFile = File(filesDir, "BBDDArbolGen.db")
        if (!dbFile.exists()) {
            Toast.makeText(this, "Base de datos no encontrada", Toast.LENGTH_LONG).show()
            return
        }

        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery(
            "SELECT codigo, nombres, nombrePadre, nombreMadre, fnacimiento FROM datatree WHERE strftime('%m', fnacimiento) = ?",
            arrayOf(mesToNumero(mes))
        )

        while (cursor.moveToNext()) {
            val codigo = cursor.getString(0) ?: ""
            val nombres = cursor.getString(1) ?: ""
            val padre = cursor.getString(2)
            val madre = cursor.getString(3)
            val fnac = cursor.getString(4) ?: ""
            datos.add(Persona(codigo, nombres, padre, madre, fnac))
        }

        cursor.close()
        db.close()

        mostrarTabla()
    }

    private fun mostrarTabla() {
        tableLayout.removeAllViews()

        for (persona in datos) {
            // Fila 1: Nombre
            val filaNombre = TableRow(this)
            val nombreText = crearCelda(persona.nombres)
            nombreText.textSize = 16f
            nombreText.setPadding(12, 12, 12, 4)
            nombreText.setTypeface(null, android.graphics.Typeface.BOLD)
            filaNombre.addView(nombreText)
            tableLayout.addView(filaNombre)

            // Fila 2: Fecha de nacimiento y edad en una fila horizontal
            val filaDetalles = TableRow(this)
            val contenedor = LinearLayout(this)
            contenedor.orientation = LinearLayout.HORIZONTAL

            val fnacText = crearCelda("Nacimiento: ${persona.fnacimiento}")
            val edadText = crearCelda("Edad: ${calcularEdad(persona.fnacimiento)}")

            fnacText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            edadText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            contenedor.addView(fnacText)
            contenedor.addView(edadText)

            filaDetalles.addView(contenedor)
            tableLayout.addView(filaDetalles)
        }
    }

    private fun crearCelda(texto: String): TextView {
        val celda = TextView(this)
        celda.text = texto
        celda.setPadding(8, 8, 8, 8)
        return celda
    }

    private fun calcularEdad(fechaNacimiento: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val fecha = sdf.parse(fechaNacimiento)
            val hoy = Calendar.getInstance()
            val nacimiento = Calendar.getInstance().apply { time = fecha!! }
            var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
            if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }
            edad
        } catch (e: Exception) {
            0
        }
    }

    private fun mesToNumero(mes: String): String {
        return when (mes.lowercase()) {
            "enero" -> "01"
            "febrero" -> "02"
            "marzo" -> "03"
            "abril" -> "04"
            "mayo" -> "05"
            "junio" -> "06"
            "julio" -> "07"
            "agosto" -> "08"
            "septiembre" -> "09"
            "octubre" -> "10"
            "noviembre" -> "11"
            "diciembre" -> "12"
            else -> "01"
        }
    }

    data class Persona(
        val codigo: String,
        val nombres: String,
        val nombrePadre: String?,
        val nombreMadre: String?,
        val fnacimiento: String
    )
    private fun extraerDia(fecha: String): Int {
        return try {
            val partes = fecha.split("-")
            if (partes.size == 3) partes[2].toIntOrNull() ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }
}