package com.example.weatherforecast.data.repository

import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.model.*
import com.example.weatherforecast.data.network.NetworkModule

class WeatherRepository {

    private val weatherApi = NetworkModule.openMeteoApi
    private val cityApi = NetworkModule.apiNinjasApi
    private val apiKey = BuildConfig.API_NINJAS_KEY

    // ── Поиск города по названию ──────────────────────────────────────────────
    suspend fun searchCities(query: String): Result<List<City>> {
        return try {
            val response = cityApi.searchCity(
                apiKey = apiKey,
                cityName = query,
                limit = 5
            )
            val cities = response.map { cityResponse ->
                City(
                    name = cityResponse.name,
                    country = cityResponse.country,
                    latitude = cityResponse.latitude,
                    longitude = cityResponse.longitude
                )
            }
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Получение прогноза для города ─────────────────────────────────────────
    suspend fun getWeatherForCity(city: City): Result<CityWeather> {
        return try {
            val response = weatherApi.getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )
            val cityWeather = mapResponseToCityWeather(city, response)
            Result.success(cityWeather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Маппинг сырого ответа API → UI-модель ─────────────────────────────────
    private fun mapResponseToCityWeather(city: City, response: WeatherResponse): CityWeather {
        val hourly = response.hourly

        val hourlyForecasts = hourly.time.indices.map { i ->
            HourlyForecast(
                time = hourly.time[i],
                temperature = hourly.temperature[i],
                weatherCode = hourly.weatherCode[i],
                precipitationProbability = hourly.precipitationProbability[i] ?: 0,
                windSpeed = hourly.windSpeed[i],
                humidity = hourly.humidity[i]
            )
        }

        val dailyForecasts = response.daily?.let { daily ->
            daily.time.indices.map { i ->
                DailyForecast(
                    date = daily.time[i],
                    tempMax = daily.tempMax[i],
                    tempMin = daily.tempMin[i],
                    weatherCode = daily.weatherCode[i],
                    precipitationProbability = daily.precipitationMax[i] ?: 0
                )
            }
        } ?: emptyList()

        return CityWeather(
            city = city,
            currentTemp = hourly.temperature[0],
            currentWeatherCode = hourly.weatherCode[0],
            hourlyForecasts = hourlyForecasts,
            dailyForecasts = dailyForecasts
        )
    }
}