package com.czy4201b.noticat.features.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.common.ServerManager
import kotlinx.coroutines.launch

@Composable
fun ClientsDialog(
    onBack: () -> Unit,
    vm: MainViewViewModel
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val supportMap by ServerManager.supportClientMap.collectAsState()
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    Dialog(
        onDismissRequest = {
            onBack()
        }
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.width(400.dp)
            ) {
                Text(
                    text = "订阅客户端",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 10.dp)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    value = vm.searchClientText.value,
                    onValueChange = {
                        vm.updateSearchClientText(it)
                    },
                    placeholder = {
                        Text("搜索支持的Client")
                    }
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 5.dp, top = 10.dp),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Box {
                        supportMap?.let { map ->
                            // search text
                            val filteredList =
                                remember(map, vm.searchClientText.value) {
                                    val allValues = map.values.toList()
                                    if (vm.searchClientText.value.isBlank()) {
                                        allValues
                                    } else {
                                        allValues.filter {
                                            it.name.contains(
                                                vm.searchClientText.value,
                                                ignoreCase = true
                                            ) || it.client.contains(
                                                vm.searchClientText.value,
                                                ignoreCase = true
                                            ) || it.url.contains(
                                                vm.searchClientText.value,
                                                ignoreCase = true
                                            )
                                        }
                                    }
                                }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .heightIn(max = 350.dp, min = 100.dp)
                                    .fillMaxWidth()
                            ) {
                                itemsIndexed(
                                    items = filteredList,
                                    key = { _, item -> item.client }
                                ) { index, client ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                onClick = {
                                                    vm.chooseSubscriptionClient(client.client)
                                                }
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 15.dp, vertical = 5.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                "${client.client}: ${client.name}",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "URL: ${client.url}",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    if (index < filteredList.lastIndex) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 15.dp))
                                    }
                                }

                            }
                        } ?: run {
                            Text(
                                "加载失败",
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            AnimatedVisibility(showButton) {
                                IconButton(
                                    onClick = {
                                        scope.launch { listState.animateScrollToItem(0) }
                                    },
                                    modifier = Modifier
                                        .padding(10.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(
                                            alpha = 0.4f
                                        ),
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_up),
                                        modifier = Modifier.size(80.dp),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 15.dp),
                    onClick = {
                        onBack()
                    }
                ) {
                    Text(
                        text = "取消"
                    )
                }
            }
        }
    }
}