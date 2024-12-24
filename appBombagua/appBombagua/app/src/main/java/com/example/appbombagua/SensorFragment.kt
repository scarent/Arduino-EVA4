package com.example.appbombagua

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class SensorFragment : Fragment() {

    private lateinit var valorSensorTextView: TextView
    private val apiKey = "37INGJEAUK1PTI4T" // API key de lectura de ThingSpeak
    private val channelID = "2762327" //  ID del canal en ThingSpeak
    private val graphUrl =
        "https://thingspeak.mathworks.com/channels/2762327/charts/1?bgcolor=%23ffffff&color=%23d62020&dynamic=true&results=60&type=line&update=15"

    class SensorFragment : Fragment() {

        private val graphUrl =
            "https://thingspeak.mathworks.com/channels/2762327/charts/1?bgcolor=%23ffffff&color=%23d62020&dynamic=true&results=60&type=line&update=15" //  la URL de tu gráfico

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_sensor, container, false)

            val webView = view.findViewById<WebView>(R.id.webview_grafico)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            webView.loadUrl(graphUrl)

            return view
        }
    }
}
