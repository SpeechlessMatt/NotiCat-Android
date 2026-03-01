package com.czy4201b.noticat.features.update

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.czy4201b.noticat.NotiCatApplication
import com.czy4201b.noticat.core.network.OkHttpClientProvider
import com.czy4201b.noticat.core.network.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject

class UpdateViewModel : ViewModel() {
    private val _events = MutableSharedFlow<UpdateEvent>()
    val events = _events.asSharedFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo

    private val _hasUpdate = MutableStateFlow(false)

    private val _hasShowDialog = MutableStateFlow(false)

    fun checkUpdate(owner: String, repo: String) {
        if (_hasShowDialog.value) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("Update", "start check login...")
                val info = getLatest(owner, repo)
                _updateInfo.value = info
                val canUpdate = isUpdateAvailable(
                    currentVersion = getCurrentAppVersion(),
                    latestVersion = info.version
                )
                if (canUpdate){
                    Log.d("Update", "发现新版本!")
                    _hasUpdate.value = true
                    _hasShowDialog.value = true
                    _events.emit(UpdateEvent.ShowUpdateDialog(info))
                } else {
                    Log.d("Update", "未发现新版本")
                }

            } catch (e: Exception) {
                Log.d("Update", "fail: $e")
            }
        }
    }

    suspend fun getLatest(owner: String, repo: String): UpdateInfo {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .build()

        OkHttpClientProvider.client.newCall(request).await().use { resp ->
            Log.d("Update","get resp: ${resp.code}")
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
            val json = JSONObject(resp.body.string())

            val version = json.getString("tag_name")
            val publishedAt = json.getString("published_at")
            val changelog = json.optString("body") ?: ""

            val assets = json.getJSONArray("assets")
            val apkUrl = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk", true) }
                ?.getString("browser_download_url")
                ?: throw NoSuchElementException("本 Release 没有 APK")

            Log.d("Update","$version")
            Log.d("Update","$publishedAt")
            Log.d("Update", apkUrl)
            Log.d("Update", changelog)

            return UpdateInfo(version, publishedAt, apkUrl, changelog)
        }
    }

    private fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        // 清理版本号（移除 "v" 前缀等非数字字符）
        val cleanCurrent = currentVersion.replace("^[vV]".toRegex(), "").trim()
        val cleanLatest = latestVersion.replace("^[vV]".toRegex(), "").trim()

        Log.d("Update", "清理后版本: '$cleanCurrent' vs '$cleanLatest'")

        val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = cleanLatest.split(".").map { it.toIntOrNull() ?: 0 }

        // 比较每个版本号部分
        val maxLength = maxOf(currentParts.size, latestParts.size)

        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val latestPart = latestParts.getOrElse(i) { 0 }
            // 有一个部分大于就可以说要更新了
            if (currentPart < latestPart) return true
            if (currentPart > latestPart) return false
        }
        return false
    }

    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = NotiCatApplication.instance.packageManager.getPackageInfo(NotiCatApplication.instance.packageName, 0)
            packageInfo.versionName ?: "0.0.0"
        } catch (e: Exception) {
            Log.e("Update", "获取当前版本失败: $e")
            "0.0.0"
        }
    }
}