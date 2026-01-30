package com.czy4201b.noticat.features.main

data class MainViewUiState(
    val appVersion: String = "-",
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val subscribedClientsState: SubsState = SubsState.Idle,
    val isRefreshCooling: Boolean = false,
    val isShowServers: Boolean = false,
    val isShowSubscriptions: Boolean = false,
    val isShowAddSubsDialog: Boolean = false,
    val isShowFilterRules: Boolean = false
)

sealed class ConnectionState {
    data object Loading : ConnectionState()
    data object Disconnected : ConnectionState()
    data class Connected(val serverVersion: String = "-") : ConnectionState()
    data class Error(val msg: String) : ConnectionState()
}

sealed class SubsState {
    data object Loading : SubsState()
    data object Idle : SubsState()
    data object Success : SubsState()
    data class Error(val msg: String) : SubsState()
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class NavigateEdit(val client: String) : UiEvent()
}