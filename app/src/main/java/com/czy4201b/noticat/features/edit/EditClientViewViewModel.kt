package com.czy4201b.noticat.features.edit

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.czy4201b.noticat.core.common.GlobalFilterManager
import com.czy4201b.noticat.core.common.ServerManager
import com.czy4201b.noticat.core.network.OkHttpClientProvider
import com.czy4201b.noticat.core.network.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import kotlin.text.ifEmpty

class EditClientViewViewModel(
    private val client: String,
    private val subscriptionId: Int,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            EditClientViewUiState(
                mode = if (subscriptionId == -1) EditMode.Create else EditMode.Edit,
                client = client,
            )
        )
    val state: StateFlow<EditClientViewUiState> = _state.asStateFlow()

    var extraFields = mutableStateListOf<ExtraFieldState>()
        private set
    var filterFields = mutableStateListOf<FilterFieldState>()
        private set

    var globalFilterFields = mutableStateListOf<FilterFieldState>()
        private set

    private val _eventFlow = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        initialize()
    }

    fun initialize(){
        val map = ServerManager.supportClientMap.value?.get(client)
        val clientName = map?.name ?: "加载错误"
        val clientDesc =
            if (map?.description == null || map.description == "")
                "这里是介绍：如果你看到这行字，那就是这个客户端没写介绍或者软件错误喵"
            else map.description
        _state.update { state ->
            state.copy(clientName = clientName, clientDesc = clientDesc)
        }
        analyzeCred()
        analyzeExtra()
        if (subscriptionId != -1){
            fetchSubscriptionInfo(subscriptionId)
        }

        viewModelScope.launch {
            val globalFilters = GlobalFilterManager.getGlobalFilters().map { filter ->
                FilterFieldState(
                    pattern = filter.pattern,
                    filterType = filter.type,
                    isIgnoreCase = filter.isIgnoreCase
                )
            }
            globalFilterFields.addAll(globalFilters)
        }
    }

    fun updateAccount(account: String) {
        _state.update { state ->
            state.copy(account = account)
        }
    }

    fun updatePasswd(password: String) {
        _state.update { state ->
            state.copy(password = password)
        }
    }

    fun updateExtra(id: String, value: String) {
        val index = extraFields.indexOfFirst { it.id == id }
        if (index != -1) {
            extraFields[index] = extraFields[index].copy(value = value)
        }
    }

    fun addFilter() {
        filterFields.add(
            FilterFieldState(
                pattern = "",
                filterType = "keyword",
                isIgnoreCase = true,
                isUnfold = false
            )
        )
    }

    fun deleteFilter(id: String) {
        filterFields.removeAll { it.id == id }
    }

    fun updateFilterPattern(id: String, pattern: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(pattern = pattern)
        }
    }

    fun setFilterType(id: String, filterType: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(filterType = filterType)
        }
    }

    fun setIgnoreCase(id: String, isIgnoreCase: Boolean) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(isIgnoreCase = isIgnoreCase)
        }
    }

    fun toggleFilter(id: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(isUnfold = !filterFields[index].isUnfold)
        }
    }

    fun toggleGlobalFilter(id: String) {
        val index = globalFilterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            globalFilterFields[index] = globalFilterFields[index].copy(isUnfold = !globalFilterFields[index].isUnfold)
        }
    }

    fun toggleApplyGlobalFilters(checked: Boolean){
        _state.update { it.copy(isApplyGlobalFilters = checked) }
    }

    private fun analyzeCred() {
        ServerManager.supportClientMap.value?.get(client).let { clientInfo ->
            clientInfo?.credentials.let { credParams ->
                credParams?.let { cred ->
                    if (!cred.isEmpty()) {
                        _state.update { state ->
                            state.copy(isShowCred = true)
                        }
                    }
                }
            }
        }
    }

    private fun analyzeExtra() {
        ServerManager.supportClientMap.value?.get(client).let { clientInfo ->
            clientInfo?.extra.let { extraParams ->
                extraParams?.forEach { extra ->
                    extraFields.add(
                        ExtraFieldState(
                            label = extra.label,
                            apiKey = extra.apiKey
                        )
                    )
                }
            }
        }
        if (!extraFields.isEmpty()) {
            _state.update { state ->
                state.copy(isShowExtra = true)
            }
        }
    }

    private fun validateFilters(): Boolean {
        var hasError = false

        // 2026/1/30 Gemini generate
        // 1. 统计每个 pattern 出现的次数 (排除掉空白，空白由 isBlank 处理)
        val patternCounts = filterFields.asSequence()
            .map { it.pattern.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()

        // 2. 遍历并更新状态
        for (i in filterFields.indices) {
            val current = filterFields[i]
            val pattern = current.pattern.trim()

            val isEmpty = pattern.isBlank()
            val isDuplicate = (patternCounts[pattern] ?: 0) > 1

            val shouldShowError = isEmpty || isDuplicate

            if (shouldShowError) hasError = true

            if (current.isError != shouldShowError) {
                filterFields[i] = current.copy(isError = shouldShowError)
            }
        }

        return !hasError
    }

    fun createSubscription() {
        if (!validateFilters()) return
        if (ServerManager.currentUrl == null) return

        ServerManager.currentUrl?.let { url ->
            viewModelScope.launch(Dispatchers.IO) {
                _state.update { it.copy(subsState = SubsState.Loading) }

                try {
                    val resp = ServerManager.runWithTokenValidation(
                        isUnauthorized = { response ->
                            return@runWithTokenValidation response.first == 401
                        }
                    ) { token ->
                        val extraMap = extraFields.associate { it.apiKey to it.value }
                        var filtersList = filterFields.map { state ->
                            Filter(
                                type = state.filterType,
                                pattern = state.pattern,
                                ignoreCase = state.isIgnoreCase
                            )
                        }

                        if (subscriptionId == -1 && _state.value.isApplyGlobalFilters){
                            val globalFiltersList = globalFilterFields.map { state ->
                                Filter(
                                    type = state.filterType,
                                    pattern = state.pattern,
                                    ignoreCase = state.isIgnoreCase
                                )
                            }
                            filtersList = filtersList + globalFiltersList
                        }

                        val json = Json {
                            encodeDefaults = true
                        }
                        val jsonString = json.encodeToString(
                            PostSubscription(
                                client = client,
                                subscriptionId = subscriptionId,
                                credentials = Credentials(
                                    username = _state.value.account,
                                    password = _state.value.password
                                ),
                                extra = extraMap,
                                filters = filtersList
                            )
                        )

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = jsonString.toRequestBody(mediaType)

                        val request = Request.Builder()
                            .url("$url/api/subscription")
                            .header("Authorization", "Bearer $token")
                            .post(body)
                            .build()

                        OkHttpClientProvider.client.newCall(request).await().use { response ->
                            return@runWithTokenValidation response.code to response.body.string()
                        }
                    }

                    resp?.let { response ->
                        val respCode = response.first
                        val respString = response.second

                        if (respCode == 200) {
                            _state.update {
                                it.copy(
                                    subsState = SubsState.Success,
                                )
                            }
                            _eventFlow.send(UiEvent.ShowToast("订阅成功"))
                            delay(5000)
                            _state.update {
                                it.copy(
                                    subsState = SubsState.Idle,
                                )
                            }
                            _eventFlow.send(UiEvent.NavigateBack)

                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: $respCode" }
                            } catch (_: Exception) {
                                "请求失败 ($respCode)"
                            }
                            _state.update { it.copy(subsState = SubsState.Error(errorMsg)) }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(subsState = SubsState.Error(msg = "出错啦: ${e.message}")) }
                }
            }
        }
    }

    fun fetchSubscriptionInfo(id: Int) {
        if (ServerManager.currentUrl == null) return

        ServerManager.currentUrl?.let { url ->
            viewModelScope.launch(Dispatchers.IO) {
                delay(200)
                _state.update { it.copy(subsState = SubsState.Loading) }

                try {
                    val resp = ServerManager.runWithTokenValidation(
                        isUnauthorized = { response ->
                            return@runWithTokenValidation response.first == 401
                        }
                    ) { token ->
                        val request = Request.Builder()
                            .url("$url/api/subscription/$id")
                            .header("Authorization", "Bearer $token")
                            .build()

                        OkHttpClientProvider.client.newCall(request).await().use { response ->
                            return@runWithTokenValidation response.code to response.body.string()
                        }
                    }

                    resp?.let { response ->
                        val respCode = response.first
                        val respString = response.second

                        if (respCode == 200) {
                            val json = Json {
                                ignoreUnknownKeys = true
                                coerceInputValues = true
                            }
                            val subscriptionInfo =
                                json.decodeFromString<GetSubscription>(respString)

                            // extra fill
                            extraFields.forEachIndexed { index, field ->
                                subscriptionInfo.extra[field.apiKey]?.let { newValue ->
                                    extraFields[index] = field.copy(value = newValue)
                                }
                            }

                            // account fill
                            _state.update { state ->
                                state.copy(
                                    account = subscriptionInfo.credentials.username,
                                    password = subscriptionInfo.credentials.password
                                )
                            }

                            // filters fill
                            filterFields.clear()
                            val newField = subscriptionInfo.filters.map { filter ->
                                FilterFieldState(
                                    pattern = filter.pattern,
                                    filterType = filter.type,
                                    isIgnoreCase = filter.ignoreCase
                                )
                            }
                            filterFields.addAll(newField)

                            _state.update {
                                it.copy(
                                    subsState = SubsState.Idle,
                                )
                            }

                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: $respCode" }
                            } catch (_: Exception) {
                                "请求失败 ($respCode)"
                            }
                            _state.update { it.copy(subsState = SubsState.Error(errorMsg)) }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                            _eventFlow.send(UiEvent.NavigateBack)
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(subsState = SubsState.Error(msg = "出错啦: ${e.message}")) }
                    _eventFlow.send(UiEvent.NavigateBack)
                }
            }
        }
    }
}

data class FilterFieldState(
    val id: String = UUID.randomUUID().toString(),
    val pattern: String,
    val filterType: String,
    val isIgnoreCase: Boolean = false,
    val isUnfold: Boolean = false,
    val isError: Boolean = false
)

data class ExtraFieldState(
    val id: String = UUID.randomUUID().toString(),
    // label is sth to show
    val label: String,
    // apiKey is sth to send
    // for example: URL is show to user, but we send to server: {"url": "xxx"}, the url is the apiKey
    val apiKey: String,
    val value: String = ""
)

@Serializable
data class PostSubscription(
    val client: String,
    @SerialName("subscription_id")
    val subscriptionId: Int,
    val credentials: Credentials,
    val extra: Map<String, String>,
    val filters: List<Filter> = emptyList()
)

@Serializable
data class GetSubscription(
    val client: String,
    val credentials: Credentials,
    val extra: Map<String, String>,
    val filters: List<Filter>
)

@Serializable
data class Credentials(
    val username: String = "",
    val password: String = ""
)

@Serializable
data class Filter(
    val type: String,
    val pattern: String,
    @SerialName("ignore_case")
    val ignoreCase: Boolean
)

class EditClientViewViewModelFactory(
    private val client: String,
    private val subscriptionId: Int,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditClientViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditClientViewViewModel(
                client = client,
                subscriptionId = subscriptionId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}