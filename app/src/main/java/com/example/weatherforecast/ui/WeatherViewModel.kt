package com.example.weatherforecast.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.model.City
import com.example.weatherforecast.data.model.CityWeather
import com.example.weatherforecast.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── UI-состояния поиска ──────────────────────────────────────────────────────

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Results(val cities: List<City>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

// ─── Глобальное состояние экрана ──────────────────────────────────────────────

data class MainUiState(
    val savedCities: List<City> = emptyList(),
    val cityWeathers: Map<String, CityWeather> = emptyMap(),
    val loadingCities: Set<String> = emptySet(),
    val errors: Map<String, String> = emptyMap()
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _selectedCity = MutableStateFlow<CityWeather?>(null)
    val selectedCity: StateFlow<CityWeather?> = _selectedCity.asStateFlow()

    // Стартовые города
    init {
        val defaultCities = listOf(
            City("Moscow", "RU", 55.7558, 37.6173),
            City("Warsaw", "PL", 52.2297, 21.0122),
            City("London", "GB", 51.5074, -0.1278)
        )
        _uiState.update { it.copy(savedCities = defaultCities) }
        defaultCities.forEach { loadWeatherForCity(it) }
    }

    // ── Поиск ─────────────────────────────────────────────────────────────────
    fun searchCity(query: String) {
        if (query.length < 2) {
            _searchState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            repository.searchCities(query).fold(
                onSuccess = { cities ->
                    _searchState.value = if (cities.isEmpty())
                        SearchUiState.Error("Города не найдены")
                    else
                        SearchUiState.Results(cities)
                },
                onFailure = { error ->
                    _searchState.value = SearchUiState.Error(error.message ?: "Ошибка поиска")
                }
            )
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState.Idle
    }

    // ── Добавление / удаление города ──────────────────────────────────────────
    fun addCity(city: City) {
        if (_uiState.value.savedCities.any { it.name == city.name }) return
        _uiState.update { it.copy(savedCities = it.savedCities + city) }
        loadWeatherForCity(city)
        clearSearch()
    }

    fun removeCity(city: City) {
        _uiState.update { state ->
            state.copy(
                savedCities = state.savedCities.filter { it.name != city.name },
                cityWeathers = state.cityWeathers - city.name
            )
        }
    }

    // ── Загрузка погоды ───────────────────────────────────────────────────────
    fun loadWeatherForCity(city: City) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingCities = it.loadingCities + city.name) }
            repository.getWeatherForCity(city).fold(
                onSuccess = { cityWeather ->
                    _uiState.update { state ->
                        state.copy(
                            cityWeathers = state.cityWeathers + (city.name to cityWeather),
                            loadingCities = state.loadingCities - city.name,
                            errors = state.errors - city.name
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            loadingCities = state.loadingCities - city.name,
                            errors = state.errors + (city.name to (error.message ?: "Ошибка загрузки"))
                        )
                    }
                }
            )
        }
    }

    fun refreshAll() {
        _uiState.value.savedCities.forEach { loadWeatherForCity(it) }
    }

    fun selectCity(cityWeather: CityWeather) {
        _selectedCity.value = cityWeather
    }

    fun clearSelectedCity() {
        _selectedCity.value = null
    }
}