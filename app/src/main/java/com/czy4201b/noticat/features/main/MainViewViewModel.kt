package com.czy4201b.noticat.features.main

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.czy4201b.noticat.NotiCatApplication
import com.czy4201b.noticat.core.common.ServerManager
import com.czy4201b.noticat.core.common.model.ServerInfo
import com.czy4201b.noticat.core.database.ServerDao
import com.czy4201b.noticat.core.database.entity.ServerEntity
import com.czy4201b.noticat.core.network.OkHttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID

@OptIn(FlowPreview::class)
class MainViewViewModel(
    private val serverDao: ServerDao,
) : ViewModel() {
    private val _state = MutableStateFlow(MainViewUiState())
    val state: StateFlow<MainViewUiState> = _state.asStateFlow()

    var serverFields = mutableStateListOf<ServerFieldState>()
        private set
    var editingServerItem = mutableStateOf<ServerFieldState?>(null)
        private set
    var editingServerName = mutableStateOf("")
        private set

    var subscribedClients = mutableStateListOf<SubscriptionInfo>()
        private set

    // Subscription: ClientsDialog
    private val _eventFlow = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()
    var searchClientText = mutableStateOf("")
        private set

    // MainView connection (single)
    private var connectionJob: Job? = null

    // Init
    private var isInitializing = true

    init {
        _state.update { state ->
            state.copy(
                appVersion = getCurrentAppVersion()
            )
        }
        if (serverFields.isEmpty()) {
            addServer()
        }
        loadServersFromDb()
        observeServersChanges()
    }

    private fun loadServersFromDb() {
        viewModelScope.launch {
            val entities = serverDao.getAll().first()

            val states = entities.map { entity ->
                ServerFieldState(
                    id = entity.stringId,
                    url = entity.url,
                    name = entity.name
                )
            }

            serverFields.clear()
            if (states.isEmpty()) {
                addServer()
            } else {
                serverFields.addAll(states)
                selectServer(serverFields.first().id)
            }

            isInitializing = false
        }
    }

    private fun observeServersChanges() {
        viewModelScope.launch {
            snapshotFlow { serverFields.toList() }
                .debounce(500)
                .collect { currentList ->
                    if (!isInitializing) {
                        saveServerFields(currentList)
                    }
                }
        }
    }

    /**
     * refresh connection of server ('ping' server)
     */
    fun refreshConnection() {
        if (state.value.isRefreshCooling) return
        if (state.value.connectionState is ConnectionState.Disconnected) return

        serverFields.firstOrNull { it.isSelected }?.let { selectedItem ->
            ServerManager.updateCurrent(url = selectedItem.url, serverId = selectedItem.id)
        } ?: run {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isRefreshCooling = true) }
            fetchServerInfo()
            delay(5000)
            _state.update { it.copy(isRefreshCooling = false) }
        }
    }

    fun selectServer(id: String) {
        connectionJob?.cancel()
        ServerManager.clearAll()

        serverFields.forEachIndexed { index, item ->
            val shouldBeSelected = item.id == id
            if (item.isSelected != shouldBeSelected) {
                val updatedItem = item.copy(isSelected = shouldBeSelected)
                serverFields[index] = updatedItem

                if (shouldBeSelected) {
                    ServerManager.updateCurrent(url = item.url, serverId = item.id)
                    fetchServerInfo()
                }
            }
        }
    }

    fun addServer() {
        serverFields.add(ServerFieldState(name = "新服务器"))
    }

    fun deleteServer(id: String) {
        val isDeletingSelected = serverFields.any { it.id == id && it.isSelected }
        if (isDeletingSelected) {
            connectionJob?.cancel() // 停止正在进行的网络请求
            ServerManager.clearSupportClients()
            _state.update { it.copy(connectionState = ConnectionState.Disconnected) }
        }

        serverFields.removeAll { it.id == id }
        if (serverFields.isEmpty()) {
            addServer()
        }

        viewModelScope.launch {
            ServerManager.cleanAuth(serverId = id)
            serverDao.deleteByStringId(stringId = id)
        }
    }

    fun editingServerItem(serverFieldState: ServerFieldState) {
        editingServerItem.value = serverFieldState
        editingServerName.value = ""
    }

    // editing -> EditingDialog
    fun updateEditingServerName(name: String) {
        editingServerName.value = name
    }

    fun renameServer(id: String, name: String): Boolean {
        if (name.isBlank()) return false
// allow Duplicate
//        val isDuplicate = serverFields.any { it.id != id && it.name == name }
//        if (isDuplicate) return false

        val index = serverFields.indexOfFirst { it.id == id }
        if (index != -1) {
            serverFields[index] = serverFields[index].copy(name = name)
            return true
        }
        return false
    }

    fun updateServerURL(id: String, url: String) {
        val index = serverFields.indexOfFirst { it.id == id }
        if (index != -1) {
            serverFields[index] = serverFields[index].copy(url = url)
        }
    }

    suspend fun saveServerFields(list: List<ServerFieldState>) {
        val entities = list.map { state ->
            val existing = serverDao.getServerByStringId(state.id)
            ServerEntity(
                id = existing?.id ?: 0,
                stringId = state.id,
                url = state.url,
                name = state.name
            )
        }
        serverDao.insertAll(entities)
    }

    fun toggleServers() {
        _state.update { state ->
            state.copy(
                isShowServers = !state.isShowServers
            )
        }
    }

    fun toggleSubscriptions() {
        _state.update { state ->
            state.copy(
                isShowSubscriptions = !state.isShowSubscriptions
            )
        }
        if (_state.value.isShowSubscriptions) {
            getSubscribedClients()
        }
    }

    fun showAddSubsDialog() {
        if (_state.value.connectionState !is ConnectionState.Connected) {
            return
        }
        ServerManager.supportClientMap.value?.let {
            _state.update { state ->
                state.copy(
                    isShowAddSubsDialog = true
                )
            }
        } ?: refreshConnection()
    }

    fun updateSearchClientText(search: String) {
        searchClientText.value = search
    }

    fun closeAddSubsDialog() {
        _state.update { state ->
            state.copy(
                isShowAddSubsDialog = false
            )
        }
    }

    fun chooseSubscriptionClient(client: String) {
        if (ServerManager.currentUrl != null && ServerManager.currentServerId != null) {
            viewModelScope.launch {
                _state.update { state ->
                    state.copy(
                        isShowAddSubsDialog = false
                    )
                }
                _eventFlow.send(UiEvent.NavigateEdit(client = client))
            }
        }
    }

    private fun getCurrentAppVersion(): String {
        return try {
            val application = NotiCatApplication.instance
            val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
            packageInfo.versionName ?: "-"
        } catch (e: Exception) {
            Log.e("Update", "获取当前版本失败: $e")
            "-"
        }
    }

    private fun fetchServerInfo() {
        if (_state.value.connectionState is ConnectionState.Loading) return

        connectionJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(connectionState = ConnectionState.Loading) }

            try {
                ServerManager.currentUrl?.let { url ->
                    val request = Request.Builder().url("$url/info").build()
                    OkHttpClientProvider.client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {

                            val jsonString = response.body.string()
                            val serverInfo = Json.decodeFromString<ServerInfo>(jsonString)
                            val serverVersion = serverInfo.version

                            val map = serverInfo.supportClients.associateBy { it.client }
                            ServerManager.updateSupportClients(map)

                            _state.update {
                                it.copy(
                                    connectionState = ConnectionState.Connected(serverVersion = serverVersion)
                                )
                            }
                        } else {
                            _state.update { it.copy(connectionState = ConnectionState.Error(msg = "服务器返回错误: ${response.code}")) }
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. 错误处理
                _state.update { it.copy(connectionState = ConnectionState.Error(msg = "出错啦: ${e.message}")) }
            }
        }
    }

    fun getSubscribedClients() {
        if (_state.value.connectionState is ConnectionState.Loading || _state.value.connectionState is ConnectionState.Disconnected) {
            _state.update { it.copy(subscribedClientsState = SubsState.Error("未连接")) }
            return
        }

        if (_state.value.subscribedClientsState is SubsState.Loading) return
        // cooling 20 seconds
        if (_state.value.subscribedClientsState is SubsState.Success) return

        ServerManager.currentUrl?.let { url ->
            connectionJob = viewModelScope.launch(Dispatchers.IO) {
                _state.update { it.copy(subscribedClientsState = SubsState.Loading) }

                try {
                    val resp = ServerManager.runWithTokenValidation(
                        isUnauthorized = { response ->
                            return@runWithTokenValidation response.first == 401
                        }
                    ) { token ->
                        val request = Request.Builder()
                            .url("$url/api/subscriptions")
                            .header("Authorization", "Bearer $token")
                            .build()

                        OkHttpClientProvider.client.newCall(request).execute().use { response ->
                            return@runWithTokenValidation response.code to response.body.string()
                        }
                    }

                    resp?.let { response ->
                        val respCode = response.first
                        val respString = response.second

                        if (respCode == 200) {
                            val subscribedClientsInfo =
                                Json.decodeFromString<List<SubscriptionInfo>>(respString)
                            subscribedClients.clear()
                            subscribedClients.addAll(subscribedClientsInfo)

                            _state.update {
                                it.copy(
                                    subscribedClientsState = SubsState.Success,
                                )
                            }
                            // delay 20s (cooling 20 seconds)
                            delay(20000)
                            _state.update {
                                it.copy(
                                    subscribedClientsState = SubsState.Idle,
                                )
                            }
                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: $respCode" }
                            } catch (_: Exception) {
                                "请求失败 ($respCode)"
                            }
                            _state.update {
                                it.copy(
                                    subscribedClientsState = SubsState.Error(
                                        errorMsg
                                    )
                                )
                            }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(subscribedClientsState = SubsState.Error(msg = "出错啦: ${e.message}")) }
                }
            }
        }
    }

    fun deleteSubscription(id: Int) {
        if (ServerManager.currentUrl == null) return

        ServerManager.currentUrl?.let { url ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val resp = ServerManager.runWithTokenValidation(
                        isUnauthorized = { response ->
                            return@runWithTokenValidation response.first == 401
                        }
                    ) { token ->
                        val request = Request.Builder()
                            .url("$url/api/subscription/$id")
                            .header("Authorization", "Bearer $token")
                            .delete()
                            .build()

                        OkHttpClientProvider.client.newCall(request).execute().use { response ->
                            return@runWithTokenValidation response.code to response.body.string()
                        }
                    }

                    resp?.let { response ->
                        val respCode = response.first
                        val respString = response.second
                        if (respCode == 200) {
                            _eventFlow.send(UiEvent.ShowToast("删除成功"))
                            subscribedClients.removeIf { it.subscriptionId == id }
                        } else {
                            val errorMsg = try {
                                val json = JSONObject(respString)
                                json.optString("error").ifEmpty { "服务器错误: $respCode" }
                            } catch (_: Exception) {
                                "请求失败 ($respCode)"
                            }
                            _eventFlow.send(UiEvent.ShowToast(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    _eventFlow.send(UiEvent.ShowToast(message = "${e.message}"))
                }
            }
        }
    }
}

data class ServerFieldState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val name: String = "",
    val isSelected: Boolean = false
)

@Serializable
data class SubscriptionInfo(
    val client: String,
    @SerialName("subscription_id")
    val subscriptionId: Int
)

class MainViewViewModelFactory(
    private val serverDao: ServerDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewViewModel(serverDao = serverDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}