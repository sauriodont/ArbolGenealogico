package com.marco.otterapp

import Miembro
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ArbolActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arbol)

        val volverButton = findViewById<Button>(R.id.volver2)
        volverButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val miembros = loadData()
        if (miembros.isEmpty()) {
            Toast.makeText(this, "No se pudo cargar el árbol genealógico", Toast.LENGTH_LONG).show()
        }

        recyclerView.adapter = MiembroAdapter(miembros)
    }

    private fun loadData(): List<Miembro> {
        val miembros = mutableListOf<Miembro>()
        val dbFile = File(filesDir, "BBDDArbolGen.db")

        if (!dbFile.exists()) {
            Log.e("ArbolActivity", "Base de datos no encontrada: ${dbFile.absolutePath}")
            return miembros
        }

        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery(
            "SELECT codigo, nombres, nombrePadre, nombreMadre FROM datatree",
            null
        )

        try {
            while (cursor.moveToNext()) {
                val codigo = getStringSafe(cursor, 0)
                val nombres = getStringSafe(cursor, 1)
                val nombrePadre = getStringSafe(cursor, 2)
                val nombreMadre = getStringSafe(cursor, 3)
                val fnacimiento = getStringSafe(cursor, 4)

                val miembro = Miembro(
                    codigo = codigo,
                    nombre = nombres,
                    nombrePadre = nombrePadre,
                    nombreMadre = nombreMadre,
                    fnacimiento = fnacimiento,
                    hijos = listOf()
                )
                miembros.add(miembro)
            }
        } catch (e: Exception) {
            Log.e("ArbolActivity", "Error al leer la base de datos: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return miembros
    }

    private fun getStringSafe(cursor: Cursor, index: Int): String {
        return try {
            when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(index).toString()
                Cursor.FIELD_TYPE_FLOAT -> cursor.getFloat(index).toString()
                Cursor.FIELD_TYPE_BLOB -> {
                    val blob = cursor.getBlob(index)
                    try {
                        String(blob, Charsets.UTF_8)
                    } catch (e: Exception) {
                        ""
                    }
                }
                else -> ""
            }
        } catch (e: Exception) {
            Log.e("ArbolActivity", "Error al leer índice $index: ${e.message}")
            ""
        }
    }
}