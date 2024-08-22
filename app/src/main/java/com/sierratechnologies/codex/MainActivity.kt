package com.sierratechnologies.codex

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var jioSpeedTextView: TextView
    private lateinit var airtelSpeedTextView: TextView
    private lateinit var vodafoneSpeedTextView: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TEST_URL = "https://example.com/largefile" // Replace with a URL to a large file for testing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jioSpeedTextView = findViewById(R.id.jioSpeedTextView)
        airtelSpeedTextView = findViewById(R.id.airtelSpeedTextView)
        vodafoneSpeedTextView = findViewById(R.id.vodafoneSpeedTextView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                // Handle permission denial
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Use location to perform network speed test
                performNetworkSpeedTest { downloadSpeed, uploadSpeed ->
                    displayNetworkSpeeds(downloadSpeed, uploadSpeed)
                }
            } ?: run {
                // Handle case when location is null
            }
        }
    }

    private fun performNetworkSpeedTest(onSpeedMeasured: (downloadSpeedMbps: Int, uploadSpeedMbps: Int) -> Unit) {
        thread {
            val downloadSpeed = measureDownloadSpeed()
            // Simulate upload speed; replace with actual upload logic if needed
            val uploadSpeed = measureUploadSpeed()

            Handler(Looper.getMainLooper()).post {
                onSpeedMeasured(downloadSpeed, uploadSpeed)
            }
        }
    }

    private fun measureDownloadSpeed(): Int {
        var downloadSpeedMbps = 0
        try {
            val urlConnection = URL(TEST_URL).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            val inputStream = BufferedInputStream(urlConnection.inputStream)
            val startTime = System.currentTimeMillis()
            var totalBytes = 0

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytes += bytesRead
            }
            inputStream.close()

            val elapsedTime = System.currentTimeMillis() - startTime
            downloadSpeedMbps = ((totalBytes / (elapsedTime / 1000.0)) / (1024 * 1024)).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return downloadSpeedMbps
    }

    private fun measureUploadSpeed(): Int {
        // Implement upload speed measurement similarly to download speed
        // Placeholder implementation
        return 0 // Placeholder
    }

    private fun displayNetworkSpeeds(downloadSpeedMbps: Int, uploadSpeedMbps: Int) {
        val maxSpeedMbps = 100 // Hypothetical max speed for calculation

        val jioSpeedPercentage = (downloadSpeedMbps * 100) / maxSpeedMbps
        val airtelSpeedPercentage = (uploadSpeedMbps * 100) / maxSpeedMbps // Example, replace with actual data
        val vodafoneSpeedPercentage = (downloadSpeedMbps * 100) / maxSpeedMbps // Example, replace with actual data

        jioSpeedTextView.text = "Jio Speed: $jioSpeedPercentage%"
        airtelSpeedTextView.text = "Airtel Speed: $airtelSpeedPercentage%"
        vodafoneSpeedTextView.text = "Vodafone Speed: $vodafoneSpeedPercentage%"
    }
}
