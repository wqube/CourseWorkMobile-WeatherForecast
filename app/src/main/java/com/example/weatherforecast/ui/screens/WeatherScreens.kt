package com.example.weatherforecast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import com.example.weatherforecast.data.model.*
import com.example.weatherforecast.ui.SearchUiState
import com.example.weatherforecast.ui.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    if (selectedCity != null) {
        CityDetailScreen(
            cityWeather = selectedCity!!,
            onBack = { viewModel.clearSelectedCity() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchCity(it)
                            },
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }
                    }) {
                        Icon(
                            if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_hint)
                        )
                    }
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isSearchActive) {
                SearchResultsList(
                    searchState = searchState,
                    onCitySelected = { city ->
                        viewModel.addCity(city)
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                if (uiState.savedCities.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.savedCities) { city ->
                            CityWeatherCard(
                                city = city,
                                cityWeather = uiState.cityWeathers[city.name],
                                isLoading = city.name in uiState.loadingCities,
                                error = uiState.errors[city.name],
                                onClick = {
                                    uiState.cityWeathers[city.name]?.let {
                                        viewModel.selectCity(it)
                                    }
                                },
                                onDelete = { viewModel.removeCity(city) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityWeatherCard(
    city: City,
    cityWeather: CityWeather?,
    isLoading: Boolean,
    error: Int?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val weatherCode = cityWeather?.currentWeatherCode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = cityWeather != null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        isLoading -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        error != null -> Text(
                            text = stringResource(id = error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        weatherCode != null -> Text(
                            text = stringResource(id = weatherCode.toWeatherDescriptionRes()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (weatherCode != null) {
                        Text(text = stringResource(weatherCode.toWeatherIconRes()), fontSize = 40.sp)
                        Text(
                            text = "${cityWeather.currentTemp.toInt()}°C",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove_city),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailScreen(cityWeather: CityWeather, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityWeather.city.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CurrentWeatherCard(cityWeather) }

            item {
                Text(
                    stringResource(R.string.hourly_forecast),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                HourlyForecastRow(cityWeather.hourlyForecasts.take(24))
            }

            item {
                Text(
                    stringResource(R.string.daily_forecast),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(cityWeather.dailyForecasts) { day ->
                DailyForecastRow(day)
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(cityWeather: CityWeather) {
    val weatherCode = cityWeather.currentWeatherCode

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(weatherCode.toWeatherIconRes()), fontSize = 72.sp)
            Text(
                text = "${cityWeather.currentTemp.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = weatherCode.toWeatherDescriptionRes()),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(16.dp))

            val current = cityWeather.hourlyForecasts.firstOrNull()
            if (current != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherStat(
                        iconRes = R.string.icon_humidity,
                        value = "${current.humidity}%",
                        label = stringResource(R.string.humidity)
                    )
                    WeatherStat(
                        iconRes = R.string.icon_wind,
                        value = "${current.windSpeed.toInt()} км/ч",
                        label = stringResource(R.string.wind)
                    )
                    WeatherStat(
                        iconRes = R.string.icon_precipitation,
                        value = "${current.precipitationProbability}%",
                        label = stringResource(R.string.precipitation)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherStat(@StringRes iconRes: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(iconRes), fontSize = 20.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
fun HourlyForecastRow(hours: List<HourlyForecast>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(hours) { hour -> HourlyCard(hour) }
    }
}

@Composable
fun HourlyCard(hour: HourlyForecast) {
    val time = hour.time.substringAfter("T")
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(70.dp)) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = time, style = MaterialTheme.typography.bodySmall)
            Text(text = stringResource(hour.weatherCode.toWeatherIconRes()), fontSize = 24.sp)
            Text(text = "${hour.temperature.toInt()}°", fontWeight = FontWeight.Bold)
            Text(
                text = "${hour.precipitationProbability}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DailyForecastRow(day: DailyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = day.date, modifier = Modifier.weight(1f))
        Text(text = stringResource(day.weatherCode.toWeatherIconRes()), fontSize = 24.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${day.tempMax.toInt()}° / ${day.tempMin.toInt()}°",
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider(thickness = 0.5.dp)
}

@Composable
fun SearchResultsList(searchState: SearchUiState, onCitySelected: (City) -> Unit) {
    when (searchState) {
        is SearchUiState.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is SearchUiState.Results -> LazyColumn {
            items(searchState.cities) { city ->
                ListItem(
                    headlineContent = { Text(city.name) },
                    supportingContent = { Text(city.country) },
                    leadingContent = { Text(stringResource(R.string.icon_location)) },
                    modifier = Modifier.clickable { onCitySelected(city) }
                )
                HorizontalDivider()
            }
        }

        is SearchUiState.Error -> Text(
            text = stringResource(id = searchState.messageRes),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )

        else -> {}
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.icon_empty_cities), fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.no_results), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.add_city), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
