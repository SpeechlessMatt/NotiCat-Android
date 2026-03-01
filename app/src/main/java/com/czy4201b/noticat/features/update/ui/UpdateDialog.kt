package com.czy4201b.noticat.features.update.ui

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.czy4201b.noticat.features.update.UpdateViewModel

@Composable
fun UpdateDialog(
    updateVm: UpdateViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val info by updateVm.updateInfo.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发现新版本 ${info?.version ?: ""}") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("发布时间：${info?.publishedAt ?: ""}")
                Text(text = info?.changelog ?: "")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // 暂时不开发下载功能哦
                    info?.apkUrl?.let {
                        val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                        context.startActivity(intent)
                    }
                },
            ) {
                Text("立即更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后")
            }
        }
    )
}