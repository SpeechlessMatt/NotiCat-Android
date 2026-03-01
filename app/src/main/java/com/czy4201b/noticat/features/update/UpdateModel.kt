package com.czy4201b.noticat.features.update

sealed class UpdateEvent {
    data class ShowUpdateDialog(val info: UpdateInfo) : UpdateEvent()
    data class ShowError(val message: String) : UpdateEvent()
}

data class UpdateInfo(
    val version: String,
    val publishedAt: String,
    val apkUrl: String,
    val changelog: String
)