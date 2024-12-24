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

    private val baseUrl = "http://172.20.10.7" // ip dispositivo conectado al internet
    private val WRITE_API_URL = "https://api.thingspeak.com/update?api_key=JCDUQ3EI7FSH71ES&field1=0"
    private val READ_API_URL = "https://api.thingspeak.com/channels/2762327/fields/1.json?results=2"

    private lateinit var textViewData: TextView
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEncender = findViewById<Button>(R.id.btn_encender)
        val btnApagar = findViewById<Button>(R.id.btn_apagar)
        textViewData = findViewById(R.id.textViewData)  // el texview para mostrar el valor
        btnRefresh = findViewById(R.id.btn_refresh)  // btn refresh

        btnEncender.setOnClickListener {
            enviarComando("encender")
        }

        btnApagar.setOnClickListener {
            enviarComando("apagar")
        }

        // btn config
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
                        // datos del textview
                        textViewData.text = "Ultimo Valor: $valorSensor"
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

