package com.czy4201b.noticat.features.edit

data class EditClientViewUiState(
    val account: String = "",
    val password: String = "",
    val mode: EditMode,
    val client: String,
    val clientName: String = "",
    val clientDesc: String = "",
    val subsState: SubsState = SubsState.Idle,
    val isShowCred: Boolean = false,
    val isShowExtra: Boolean = false,
    val isApplyGlobalFilters: Boolean = true
)

sealed class EditMode {
    data object Create : EditMode()

    data object Edit : EditMode()
}

sealed class SubsState {
    data object Loading : SubsState()

    data object Idle : SubsState()

    data object Success : SubsState()

    data class Error(val msg: String) : SubsState()
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data object NavigateBack : UiEvent()
}