package com.marco.otterapp

import Miembro
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import java.io.File

class ArbolVisualActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val miembros = cargarMiembrosDesdeBD(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ArbolGenealogico(miembros)
                }
            }
        }
    }

    private fun cargarMiembrosDesdeBD(context: Context): List<Miembro> {
        val miembros = mutableMapOf<String, Miembro>()
        val dbFile = File(context.filesDir, "BBDDArbolGen.db")
        if (!dbFile.exists()) return emptyList()

        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery(
            "SELECT codigo, nombres || ' ' || apellidos AS nombre, idpadre, idmadre, fnacimiento FROM familia",
            null
        )

        while (cursor.moveToNext()) {
            val codigo = getStringSafe(cursor, 0)
            val nombre = getStringSafe(cursor, 1)
            val nombrePadre = getStringSafe(cursor, 2)
            val nombreMadre = getStringSafe(cursor, 3)
            val fnacimiento = getStringSafe(cursor, 4)

            miembros[codigo] = Miembro(codigo, nombre, nombrePadre, nombreMadre, fnacimiento, listOf())

        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val codigo = getStringSafe(cursor, 0)
            val idpadre = getStringSafe(cursor, 2)
            val idmadre = getStringSafe(cursor, 3)
            val fnacimiento = getStringSafe(cursor, 4)

            if (idpadre.isNotBlank()) {
                miembros[idpadre]?.let { padre ->
                    miembros[idpadre] = padre.copy(hijos = padre.hijos + codigo)
                }
            }

            if (idmadre.isNotBlank()) {
                miembros[idmadre]?.let { madre ->
                    miembros[idmadre] = madre.copy(hijos = madre.hijos + codigo)
                }
            }

            cursor.moveToNext()
        }

        cursor.close()
        db.close()
        return miembros.values.toList()
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
            Log.e("ArbolVisualActivity", "Error al leer índice $index: ${e.message}")
            ""
        }
    }
}