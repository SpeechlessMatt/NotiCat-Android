package com.czy4201b.noticat.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.czy4201b.noticat.core.common.ServerManager
import com.czy4201b.noticat.core.network.OkHttpClientProvider
import com.czy4201b.noticat.core.network.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.text.ifEmpty

class LoginCardViewModel: ViewModel() {
    private val _state = MutableStateFlow(LoginCardUiState())
    val state: StateFlow<LoginCardUiState> = _state.asStateFlow()

    private var requestJob: Job? = null

    private val _eventFlow = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        initializeData()
    }

//    暂时不用
//    fun getAccountEntity() {
//        viewModelScope.launch {
//            currentServerEntity = serverDao.getServerByStringId(stringId = serverId)
//        }
//    }

    fun updateAccount(account: String) {
        _state.update { state ->
            state.copy(account = account)
        }
    }

    fun updatePassword(password: String) {
        _state.update { state ->
            state.copy(password = password)
        }
    }

    fun updateEmail(email: String) {
        _state.update { state ->
            state.copy(email = email)
        }
    }

    fun updateCode(code: String) {
        _state.update { state ->
            state.copy(code = code)
        }
    }

    fun showLogin() {
        _state.update { state ->
            state.copy(isRegister = false)
        }
    }

    fun toggleRegister() {
        _state.update { state ->
            state.copy(isRegister = !state.isRegister)
        }
    }

    fun setCooldownTimer(seconds: Int) {
        viewModelScope.launch {
            for (i in seconds downTo 1) {
                _state.update { it.copy(sendCodeState = SendCodeState.Cooling(remainingSeconds = i)) }
                delay(1000)
            }
            _state.update { it.copy(sendCodeState = SendCodeState.Unsend) }
        }
    }

    fun sendCode() {
        if (_state.value.sendCodeState is SendCodeState.Cooling) return

        if (!Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.update { it.copy(isEmailError = true) }
            return
        }

        // double check: more security and faster
        if (ServerManager.currentUrl == null) return

        requestJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(sendCodeState = SendCodeState.Cooling(60)) }

            try {
                ServerManager.currentUrl?.let { url ->
                    val json = JSONObject()
                    json.put("email", _state.value.email)
                    val jsonString = json.toString()

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = jsonString.toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("$url/sendcode")
                        .post(body)
                        .build()

                    OkHttpClientProvider.client.newCall(request).await().use { response ->
                        val respString = response.body.string()

                        if (response.isSuccessful) {
                            setCooldownTimer(60)
                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: ${response.code}" }
                            } catch (_: Exception) {
                                "请求失败 (${response.code})"
                            }
                            _state.update { it.copy(sendCodeState = SendCodeState.Error(errorMsg)) }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. 错误处理
                _state.update { it.copy(sendCodeState = SendCodeState.Error(msg = "出错啦: ${e.message}")) }
                _eventFlow.send(UiEvent.ShowToast("出错啦: ${e.message}"))
            }
        }
    }

    fun registerAccount() {
        if (!Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.update { it.copy(isEmailError = true) }
            return
        }

        // double check: more security and faster
        if (ServerManager.currentUrl == null) return

        requestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                ServerManager.currentUrl?.let { url ->
                    val jsonString = Json.encodeToString(
                        UserInfo(
                            username = _state.value.account,
                            password = _state.value.password,
                            email = _state.value.email,
                            code = _state.value.code
                        )
                    )

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = jsonString.toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("$url/register")
                        .post(body)
                        .build()

                    OkHttpClientProvider.client.newCall(request).await().use { response ->
                        val respString = response.body.string()

                        if (response.isSuccessful) {
                            _state.update { state ->
                                state.copy(registerState = RegisterState.Success)
                            }
                            _eventFlow.send(UiEvent.ShowToast("注册成功！"))
                            delay(2000)
                            // return to login ~
                            _eventFlow.send(UiEvent.NavigateBack)
                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: ${response.code}" }
                            } catch (_: Exception) {
                                "请求失败 (${response.code})"
                            }
                            _state.update { it.copy(registerState = RegisterState.Error(errorMsg)) }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. 错误处理
                _state.update { it.copy(registerState = RegisterState.Error(msg = "出错啦: ${e.message}")) }
                _eventFlow.send(UiEvent.ShowToast("出错啦: ${e.message}"))
            }
        }
    }

    fun loginAccount() {
        // double check: more security and faster
        if (ServerManager.currentUrl == null) return

        requestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                ServerManager.currentUrl?.let { url ->
                    val jsonString = Json.encodeToString(
                        LoginInfo(
                            account = _state.value.account,
                            password = _state.value.password,
                        )
                    )

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = jsonString.toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("$url/login")
                        .post(body)
                        .build()

                    OkHttpClientProvider.client.newCall(request).await().use { response ->
                        val respString = response.body.string()

                        if (response.isSuccessful) {
                            val json = JSONObject(respString)
                            val token = json.optString("token")
                            val username = json.optString("username")

                            if (token.isNotEmpty()) {
                                ServerManager.saveAuthToken(token)
                                _state.update { state ->
                                    state.copy(loginState = LoginState.Success(username = username))
                                }
                            }
                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: ${response.code}" }
                            } catch (_: Exception) {
                                "请求失败 (${response.code})"
                            }
                            _state.update { it.copy(loginState = LoginState.Error(errorMsg)) }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. 错误处理
                _state.update { it.copy(loginState = LoginState.Error(msg = "出错啦: ${e.message}")) }
                _eventFlow.send(UiEvent.ShowToast("出错啦: ${e.message}"))
            }
        }
    }

    fun initializeData() {
        // double check: more security and faster
        if (ServerManager.currentUrl == null) return

        requestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                ServerManager.currentUrl?.let { url ->
                    val resp = ServerManager.runWithTokenValidation(
                        isUnauthorized = { response ->
                            return@runWithTokenValidation response.first == 401
                        }
                    ) { token ->
                        val request = Request.Builder()
                            .url("$url/api/ping")
                            .header("Authorization", "Bearer $token")
                            .build()
                        OkHttpClientProvider.client.newCall(request).await().use { response ->
                            return@runWithTokenValidation response.code to response.body.string()
                        }
                    }

                    resp?.let { response ->
                        val respCode = response.first
                        val respString = response.second

                        when (respCode) {
                            200 -> {
                                val json = JSONObject(respString)
                                val username = json.optString("username")
                                _state.update { it.copy(loginState = LoginState.Success(username = username)) }
                            }
                            401 -> {
                                _state.update { it.copy(loginState = LoginState.Idle) }
                            }
                            else -> {
                                _state.update { it.copy(loginState = LoginState.Error("服务器错误: $respCode")) }
                                _eventFlow.send(UiEvent.ShowToast("服务器错误: $respCode"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. 错误处理
                _state.update { it.copy(loginState = LoginState.Error(msg = "出错啦: ${e.message}")) }
                _eventFlow.send(UiEvent.ShowToast("出错啦: ${e.message}"))
            }
        }
    }
}

@Serializable
data class LoginInfo(
    val account: String,
    val password: String
)

@Serializable
data class UserInfo(
    val username: String,
    val password: String,
    val email: String,
    val code: String,
)