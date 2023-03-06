package com.bahria.driverapp.ui.map.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bahria.driverapp.ui.map.repository.MapRepository

class MapViewModelFactory(private val repository: MapRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(repository) as T
    }
}