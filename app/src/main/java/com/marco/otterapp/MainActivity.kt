package com.marco.otterapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        private var intentoDeDescargaRealizado = false
    }

    private val databaseUrl =
        "https://drive.google.com/uc?export=download&id=1s9vD9AbwKb9P7CQGXhgEBDf47yHf2KLh"
    private val databaseFileName = "BBDDArbolGen.db"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.ident).setOnClickListener {
            startActivity(Intent(this, IdentidadActivity::class.java))
        }

        findViewById<Button>(R.id.arbol).setOnClickListener {
            startActivity(Intent(this, ArbolActivity::class.java))
        }
        findViewById<Button>(R.id.cumples).setOnClickListener {
            startActivity(Intent(this, TreegenActivity::class.java))
        }
        findViewById<Button>(R.id.arbolVisual).setOnClickListener {
            startActivity(Intent(this, ArbolVisualActivity::class.java))
        }


        if (!intentoDeDescargaRealizado) {
            intentoDeDescargaRealizado = true
            descargarBaseDeDatos()
        }
    }

    private fun descargarBaseDeDatos() {
        val file = File(filesDir, databaseFileName)

        if (isInternetAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL(databaseUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        val outputStream = FileOutputStream(file)

                        inputStream.copyTo(outputStream)

                        inputStream.close()
                        outputStream.close()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Base de datos actualizada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        usarBaseLocal(file)
                    }
                } catch (e: Exception) {
                    Log.e("DB_DOWNLOAD", "Error al descargar: ${e.message}")
                    withContext(Dispatchers.Main) {
                        usarBaseLocal(file)
                    }
                }
            }
        } else {
            usarBaseLocal(file)
        }
    }

    private fun usarBaseLocal(file: File) {
        if (file.exists()) {
            Toast.makeText(this, "Usando base de datos local", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se pudo obtener la base de datos", Toast.LENGTH_LONG).show()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}