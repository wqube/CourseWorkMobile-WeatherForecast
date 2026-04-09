package com.example.weatherforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.weatherforecast.data.model.City
import com.example.weatherforecast.ui.WeatherViewModel
import com.example.weatherforecast.ui.screens.CityListScreen

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultCities = loadDefaultCities()
        viewModel.setDefaultCities(defaultCities)

        setContent {
            MaterialTheme {
                Surface {
                    CityListScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun loadDefaultCities(): List<City> {
        val rawList = resources.getStringArray(R.array.default_cities)

        return rawList.mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size != 4) return@mapNotNull null

            try {
                City(
                    name = parts[0],
                    country = parts[1],
                    latitude = parts[2].toDouble(),
                    longitude = parts[3].toDouble()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}