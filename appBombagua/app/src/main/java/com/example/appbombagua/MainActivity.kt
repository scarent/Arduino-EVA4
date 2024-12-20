package com.example.appbombagua

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val baseUrl = "http://172.20.10.7" // Cambiar si es necesario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEncender = findViewById<Button>(R.id.btn_encender)
        val btnApagar = findViewById<Button>(R.id.btn_apagar)

        btnEncender.setOnClickListener {
            enviarComando("encender")
        }

        btnApagar.setOnClickListener {
            enviarComando("apagar")
        }

        // Cargar el fragmento que muestra el valor del sensor
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.contenedor_fragment, SensorFragment())
            }
        }
    }

    private fun enviarComando(comando: String) {
        val client = OkHttpClient()
        val url = "$baseUrl/$comando"
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val respuesta = response.body?.string() ?: "Sin respuesta"
                runOnUiThread {
                    Toast.makeText(this, "Respuesta: $respuesta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
