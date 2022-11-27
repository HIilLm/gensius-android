package com.example.oracle

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.oracle.ui.theme.OracleTheme
import kotlinx.coroutines.delay
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Magazine()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager!!.registerListener(
            sensorListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        var isReady: Boolean = true

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (x < 1 && x > -1) isReady = true
            if (acceleration < 12 || !isReady) return

            isReady = false

            if (x >= 10) {
                webView?.evaluateJavascript(
                    "(() => { document.querySelector('#btnPrevious').click() })()",
                    null
                )
            } else if (x <= -10) {
                webView?.evaluateJavascript(
                    "(() => { document.querySelector('#btnNext').click() })()",
                    null
                )
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(
            sensorListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun Magazine() {
        val url = "https://oracle-static.vercel.app"

        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )

                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.allowContentAccess = true
                settings.domStorageEnabled = true
                clearCache(true)

                loadUrl(url)

                webView = this
            }
        }, update = {
            it.loadUrl(url)
        })
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OracleTheme {
        Greeting("Android")
    }
}