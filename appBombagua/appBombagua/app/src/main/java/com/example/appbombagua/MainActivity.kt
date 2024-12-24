package com.example.appbombagua

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val baseUrl = "http://172.20.10.7" // IP del dispositivo conectado al Internet
    private val WRITE_API_URL = "https://api.thingspeak.com/update?api_key=JCDUQ3EI7FSH71ES"
    private val READ_API_URL = "https://api.thingspeak.com/channels/2762327/fields/1.json?results=2"

    private lateinit var textViewData: TextView
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEncender = findViewById<Button>(R.id.btn_encender)
        val btnApagar = findViewById<Button>(R.id.btn_apagar)
        textViewData = findViewById(R.id.textViewData) // TextView para mostrar el valor
        btnRefresh = findViewById(R.id.btn_refresh)   // Botón de actualizar

        btnEncender.setOnClickListener {
            enviarComando("encender")
        }

        btnApagar.setOnClickListener {
            enviarComando("apagar")
        }

        btnRefresh.setOnClickListener {
            obtenerDatosDeThingSpeak()
        }

        // Cargar el fragmento que muestra el valor del sensor
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.contenedor_fragment, SensorFragment())
            }
        }

        // Obtener los datos de ThingSpeak al iniciar
        obtenerDatosDeThingSpeak()
    }

    private fun enviarComando(comando: String) {
        val client = OkHttpClient()
        val urlDispositivo = "$baseUrl/$comando"
        val valorField2 = if (comando == "encender") 1 else 0
        val urlThingSpeak = "$WRITE_API_URL&field2=$valorField2"

        Thread {
            try {
                // Enviar comando al dispositivo
                val response = client.newCall(Request.Builder().url(urlDispositivo).build()).execute()
                val respuesta = response.body?.string() ?: "Sin respuesta"

                runOnUiThread {
                    Toast.makeText(this, "Respuesta: $respuesta", Toast.LENGTH_SHORT).show()
                }

                // Enviar valor a ThingSpeak
                val responseThingSpeak = client.newCall(Request.Builder().url(urlThingSpeak).build()).execute()
                val respuestaThingSpeak = responseThingSpeak.body?.string() ?: "Sin respuesta de ThingSpeak"

                runOnUiThread {
                    Toast.makeText(this, "ThingSpeak: $respuestaThingSpeak", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun obtenerDatosDeThingSpeak() {
        val client = OkHttpClient()
        val request = Request.Builder().url(READ_API_URL).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    val feeds = jsonResponse.getJSONArray("feeds")
                    val latestFeed = feeds.getJSONObject(0)
                    val valorSensor = latestFeed.getString("field1")

                    runOnUiThread {
                        // Mostrar datos en el TextView
                        textViewData.text = "Último Valor: $valorSensor"
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}


