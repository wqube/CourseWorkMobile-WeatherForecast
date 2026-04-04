package com.example.weatherforecast.data.network

import com.example.weatherforecast.data.model.CityResponse
import com.example.weatherforecast.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,weathercode,windspeed_10m,relativehumidity_2m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode,precipitation_probability_max",
        @Query("forecast_days") forecastDays: Int = 16,
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

interface ApiNinjasApi {
    @GET("v1/city")
    suspend fun searchCity(
        @Header("X-Api-Key") apiKey: String,
        @Query("name") cityName: String,
        @Query("limit") limit: Int = 5
    ): List<CityResponse>
}