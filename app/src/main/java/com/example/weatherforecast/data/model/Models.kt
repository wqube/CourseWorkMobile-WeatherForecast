package com.example.weatherforecast.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Api-Ninjas: ответ на запрос города ───────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CityResponse(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    @Json(name = "is_capital") val isCapital: Boolean = false,
    val population: Int? = null
)

// ─── Open-Meteo: прогноз погоды ───────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @Json(name = "hourly") val hourly: HourlyData,
    @Json(name = "hourly_units") val hourlyUnits: HourlyUnits,
    @Json(name = "daily") val daily: DailyData? = null,
    @Json(name = "daily_units") val dailyUnits: DailyUnits? = null
)

@JsonClass(generateAdapter = true)
data class HourlyData(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int?>,
    @Json(name = "weathercode") val weatherCode: List<Int>,
    @Json(name = "windspeed_10m") val windSpeed: List<Double>,
    @Json(name = "relativehumidity_2m") val humidity: List<Int>
)

@JsonClass(generateAdapter = true)
data class HourlyUnits(
    val time: String,
    @Json(name = "temperature_2m") val temperature: String,
    @Json(name = "windspeed_10m") val windSpeed: String
)

@JsonClass(generateAdapter = true)
data class DailyData(
    val time: List<String>,
    @Json(name = "temperature_2m_max") val tempMax: List<Double>,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>,
    @Json(name = "weathercode") val weatherCode: List<Int>,
    @Json(name = "precipitation_probability_max") val precipitationMax: List<Int?>
)

@JsonClass(generateAdapter = true)
data class DailyUnits(
    @Json(name = "temperature_2m_max") val tempMax: String,
    @Json(name = "temperature_2m_min") val tempMin: String
)

// ─── Внутренние модели UI ──────────────────────────────────────────────────────

data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val windSpeed: Double,
    val humidity: Int
)

data class DailyForecast(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val weatherCode: Int,
    val precipitationProbability: Int
)

data class CityWeather(
    val city: City,
    val currentTemp: Double,
    val currentWeatherCode: Int,
    val hourlyForecasts: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>
)

// ─── WMO Weather Code → описание и эмодзи ─────────────────────────────────────

fun Int.toWeatherDescription(): String = when (this) {
    0 -> "Ясно"
    1, 2, 3 -> "Переменная облачность"
    45, 48 -> "Туман"
    51, 53, 55 -> "Морось"
    61, 63, 65 -> "Дождь"
    71, 73, 75 -> "Снег"
    80, 81, 82 -> "Ливень"
    95 -> "Гроза"
    96, 99 -> "Гроза с градом"
    else -> "Неизвестно"
}

fun Int.toWeatherEmoji(): String = when (this) {
    0 -> "☀️"
    1, 2 -> "🌤️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55 -> "🌦️"
    61, 63, 65 -> "🌧️"
    71, 73, 75 -> "❄️"
    80, 81, 82 -> "⛈️"
    95, 96, 99 -> "🌩️"
    else -> "🌡️"
}