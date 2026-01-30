@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.czy4201b.noticat.features.main

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.common.ServerManager

@Composable
fun ReadmeWeb(
    onBack: () -> Unit,
){
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 40.dp,
                        bottom = 15.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onBack()
                            },
                            indication = null,
                            interactionSource = null
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.padding(end = 5.dp))
                    Icon(
                        painter = painterResource(R.drawable.return_back),
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = stringResource(R.string.return_back),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    stringResource(R.string.supported_subscriptions),
                    modifier = Modifier
                        .padding(start = 7.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        ServerManager.currentUrl?.let { url ->
            AndroidView(
                factory = { webView },
                update = {
                    it.settings.javaScriptEnabled = true
                    it.loadUrl("$url/readme")
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}