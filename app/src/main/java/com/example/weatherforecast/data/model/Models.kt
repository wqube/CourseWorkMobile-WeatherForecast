package com.example.weatherforecast.data.model

import androidx.annotation.StringRes
import com.example.weatherforecast.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityResponse(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    @Json(name = "is_capital") val isCapital: Boolean = false,
    val population: Int? = null
)

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

@StringRes
fun Int.toWeatherDescriptionRes(): Int = when (this) {
    0 -> R.string.weather_clear
    1, 2 -> R.string.weather_partly_cloudy
    3 -> R.string.weather_cloudy
    45, 48 -> R.string.weather_fog
    51, 53, 55 -> R.string.weather_drizzle
    61, 63, 65 -> R.string.weather_rain
    71, 73, 75 -> R.string.weather_snow
    80, 81, 82 -> R.string.weather_shower
    95 -> R.string.weather_thunderstorm
    96, 99 -> R.string.weather_thunderstorm_hail
    else -> R.string.weather_unknown
}

@StringRes
fun Int.toWeatherIconRes(): Int = when (this) {
    0 -> R.string.weather_icon_clear
    1, 2 -> R.string.weather_icon_partly_cloudy
    3 -> R.string.weather_icon_cloudy
    45, 48 -> R.string.weather_icon_fog
    51, 53, 55 -> R.string.weather_icon_drizzle
    61, 63, 65 -> R.string.weather_icon_rain
    71, 73, 75 -> R.string.weather_icon_snow
    80, 81, 82 -> R.string.weather_icon_shower
    95, 96, 99 -> R.string.weather_icon_thunderstorm
    else -> R.string.weather_icon_unknown
}
