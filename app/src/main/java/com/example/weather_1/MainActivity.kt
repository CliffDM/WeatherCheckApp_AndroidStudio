package com.example.weather_1

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double
)

data class Weather(
    val description: String,
    val icon: String
)

class MainActivity : AppCompatActivity() {

    private val apiKey = "545b8d928ffde592ff164cdc587ec935"
    private val defaultCity = "Manado"

    private lateinit var cityEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var cityNameTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var weatherIconImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityEditText = findViewById(R.id.cityEditText)
        searchButton = findViewById(R.id.searchButton)
        cityNameTextView = findViewById(R.id.cityNameTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)

        searchButton.setOnClickListener {
            val city = cityEditText.text.toString()
            if (city.isNotEmpty()) {
                FetchWeatherTask().execute(city)
            } else {
                Toast.makeText(this, "Masukkan Nama Kota dengan tepat", Toast.LENGTH_SHORT).show()
            }
        }

        // Load weather data for the default city
        FetchWeatherTask().execute(defaultCity)
    }

    private inner class FetchWeatherTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val city = params[0]
            val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val result = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    result.append(line)
                }
                return result.toString()
            } finally {
                connection.disconnect()
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val weatherData = parseWeatherData(result)
            if (weatherData != null) {
                updateUI(weatherData)
            } else {
                Toast.makeText(this@MainActivity, "Weather Data fetch failed", Toast.LENGTH_SHORT).show()
            }
        }

        private fun parseWeatherData(jsonString: String): WeatherData? {
            try {
                val jsonObject = JSONObject(jsonString)
                val cityName = jsonObject.getString("name")
                val main = jsonObject.getJSONObject("main")
                val temp = main.getDouble("temp")
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val description = weatherObject.getString("description")
                val icon = weatherObject.getString("icon")

                return WeatherData(cityName, Main(temp), listOf(Weather(description, icon)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun updateUI(weatherData: WeatherData) {
            cityNameTextView.text = weatherData.name
            temperatureTextView.text = "${weatherData.main.temp}°C"
            descriptionTextView.text = weatherData.weather[0].description

            val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"
            Picasso.get().load(iconUrl).into(weatherIconImageView)
        }
    }
}
