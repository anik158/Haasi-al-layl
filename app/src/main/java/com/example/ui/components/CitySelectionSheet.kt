package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CityLocation
import com.example.data.prayer.PrayerCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionSheet(
  currentCity: CityLocation,
  onCitySelected: (CityLocation) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  var searchQuery by remember { mutableStateOf("") }

  val filteredCities = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      PrayerCalculator.PRESET_CITIES
    } else {
      PrayerCalculator.PRESET_CITIES.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.country.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Text(
        text = "Select Location",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = "Calculates prayer times and weather for your area",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search city or country...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("city_search_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
      ) {
        items(filteredCities) { city ->
          val isSelected = city.name == currentCity.name && city.country == currentCity.country
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onCitySelected(city)
                onDismiss()
              }
              .testTag("city_item_${city.name.lowercase()}"),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = city.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                  text = city.country,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (isSelected) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
