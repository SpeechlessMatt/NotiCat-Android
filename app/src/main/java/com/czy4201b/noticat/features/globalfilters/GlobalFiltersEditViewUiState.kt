package com.czy4201b.noticat.features.globalfilters

data class GlobalFiltersEditViewUiState(
    val isLoading: Boolean = false
)

sealed class UiEvent {
    data class ShowToast(val message: String): UiEvent()
}
