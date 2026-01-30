package com.czy4201b.noticat.features.login

data class LoginCardUiState(
    val account: String = "",
    val password: String = "",
    val email: String = "",
    val code: String = "",
    val loginState: LoginState = LoginState.Idle,
    val registerState: RegisterState = RegisterState.Idle,
    val sendCodeState: SendCodeState = SendCodeState.Unsend,
    val isAccountError: Boolean = false,
    val isEmailError: Boolean = false,
    val isCodeError: Boolean = false,
    val isRegister: Boolean = false
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}

sealed class SendCodeState {
    data class Cooling(val remainingSeconds: Int) : SendCodeState()
    data object Unsend : SendCodeState()
    data class Error(val msg: String) : SendCodeState()
}

sealed class LoginState {
    data object Idle : LoginState()
    data class Success(val username: String) : LoginState()
    data class Error(val msg: String) : LoginState()
}

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Success : RegisterState()
    data class Error(val msg: String) : RegisterState()
}