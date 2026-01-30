package com.czy4201b.noticat.core.common

import com.czy4201b.noticat.core.common.model.ClientInfo
import com.czy4201b.noticat.core.database.AccountDao
import com.czy4201b.noticat.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ServerManager {
    private lateinit var accountDao: AccountDao

    fun init(dao: AccountDao) {
        this.accountDao = dao
    }

    private val _supportClientMap = MutableStateFlow<Map<String, ClientInfo>?>(null)
    val supportClientMap = _supportClientMap.asStateFlow()

    var currentUrl: String? = null
        private set
    var currentServerId: String? = null
        private set

    // stringId(serverId) match authToken?
    private val tokenCache = mutableMapOf<String, String>()

    fun updateSupportClients(map: Map<String, ClientInfo>) {
        _supportClientMap.value = map
    }

    fun clearSupportClients() {
        _supportClientMap.value = null
    }

    fun clearAll() {
        currentUrl = null
        currentServerId = null
        clearSupportClients()
    }

    fun updateCurrent(url: String?, serverId: String?) {
        currentUrl = url
        currentServerId = serverId
    }

    suspend fun saveAuthToken(token: String){
        currentServerId?.let { serverId ->
            tokenCache[serverId] = token
            accountDao.insert(
                account = AccountEntity(
                    loginAuth = token,
                    serverId = serverId
                )
            )
        }
    }

    /**
     * get auth token with cache in map
     */
    private suspend fun getAuthToken(): String {
        val id = currentServerId ?: return ""
        val auth = accountDao.getAccountByServerId(id)?.loginAuth ?: ""
        tokenCache[id] = auth
        return auth
    }

    /**
     * This function is to wrap action in token validation check.
     * We can place (suspend) action like okhttp request and make a check function to check authorization
     * if it return null: something wrong happened..(Caz' id is not set)
     * @param action action is function that you want to do sth with authToken
     * @param isUnauthorized a check function to analyze the result from action which check if it is the token error
     */
    suspend fun <T> runWithTokenValidation(
        isUnauthorized: (T) -> Boolean,
        action: suspend (String) -> T,
    ): T? {
        currentServerId?.let { id ->
            val cachedToken = tokenCache[id] ?: getAuthToken()
            if (cachedToken.isEmpty()) {
                return action(cachedToken)
            }

            val result = action(cachedToken)
            // if is unauthorized: get token from db
            if (isUnauthorized(result)) {
                val freshToken = getAuthToken()
                if (freshToken == cachedToken)
                    return result

                return action(freshToken)
            } else {
                return result
            }
        }
        return null
    }
}
