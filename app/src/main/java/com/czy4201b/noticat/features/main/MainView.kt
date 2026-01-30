package com.czy4201b.noticat.features.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.common.ServerManager
import com.czy4201b.noticat.core.components.SwipeBox
import com.czy4201b.noticat.core.components.SwipeLazyColumn
import com.czy4201b.noticat.core.navigation.Route
import com.czy4201b.noticat.core.theme.AppTheme

@Composable
fun MainView(
    modifier: Modifier = Modifier,
    vm: MainViewViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val uiState by vm.state.collectAsState()
    val scrollState = rememberScrollState()

    var showDetailDialog by remember { mutableStateOf(false) }

    var showAboutMeDialog by remember { mutableStateOf(false) }
    var showPayCodeDialog by remember { mutableStateOf(false) }

    val serversState = rememberLazyListState()
    val serversButtonRotateDegree by animateFloatAsState(
        targetValue = if (uiState.isShowServers) 90f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "rotateDegree"
    )

    val subsState = rememberLazyListState()
    val subsButtonRotateDegree by animateFloatAsState(
        targetValue = if (uiState.isShowSubscriptions) 90f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "rotateDegree"
    )
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            if (event is UiEvent.ShowToast) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            } else if (event is UiEvent.NavigateEdit) {
                navController.navigate(
                    Route.Edit.createRoute(
                        client = event.client,
                    )
                ) {
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(
                        start = 10.dp,
                        top = 50.dp,
                        bottom = 10.dp,
                        end = 10.dp
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.padding(end = 5.dp))
                Image(
                    painter = painterResource(R.drawable.icon),
                    modifier = Modifier.size(50.dp),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(scrollState),
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = {
                    vm.refreshConnection()
                },
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (uiState.connectionState) {
                        is ConnectionState.Connected -> {
                            Icon(
                                painter = painterResource(R.drawable.check_circle),
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(10.dp),
                                contentDescription = null,
                                tint = AppTheme.customColors.success
                            )
                        }

                        is ConnectionState.Disconnected -> {
                            Icon(
                                painter = painterResource(R.drawable.cancel),
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(10.dp),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        is ConnectionState.Loading -> {
                            Icon(
                                painter = painterResource(R.drawable.loading),
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(10.dp),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        is ConnectionState.Error -> {
                            Icon(
                                painter = painterResource(R.drawable.cancel),
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(10.dp),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        when (val state = uiState.connectionState) {
                            is ConnectionState.Connected -> {
                                Text(
                                    stringResource(R.string.connect_success),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            is ConnectionState.Disconnected -> {
                                Text(
                                    stringResource(R.string.disconnect),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            is ConnectionState.Loading -> {
                                Text(
                                    stringResource(R.string.try_connect),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            is ConnectionState.Error -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.connect_fail),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.info),
                                        modifier = Modifier
                                            .size(23.dp)
                                            .clickable(
                                                onClick = {
                                                    showDetailDialog = true
                                                },
                                                indication = null,
                                                interactionSource = null
                                            ),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (showDetailDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDetailDialog = false },
                                        title = {
                                            Text(
                                                stringResource(R.string.error_info),
                                                color = AppTheme.customColors.onWarning
                                            )
                                        },
                                        text = {
                                            Text(
                                                state.msg,
                                                color = AppTheme.customColors.onWarning,
                                            )
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = { showDetailDialog = false }
                                            ) {
                                                Text(stringResource(R.string.ok_yes))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.app_version, uiState.appVersion),
                            style = MaterialTheme.typography.bodySmall
                        )
                        when (val state = uiState.connectionState) {
                            is ConnectionState.Connected -> {
                                Text(
                                    stringResource(R.string.server_version, state.serverVersion),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            is ConnectionState.Disconnected -> {
                                Text(
                                    stringResource(R.string.forget_server_version),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            is ConnectionState.Loading -> {
                                Text(
                                    stringResource(R.string.no_server_version),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            is ConnectionState.Error -> {
                                Text(
                                    stringResource(R.string.error_server_version),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                stringResource(R.string.server_settings),
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.padding(horizontal = 15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    vm.toggleServers()
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.server_source_url),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.server_source_url_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.rotate(serversButtonRotateDegree)
                        )
                    }
                    AnimatedVisibility(visible = uiState.isShowServers) {
                        SwipeLazyColumn(
                            listState = serversState,
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(bottom = 10.dp)
                                .heightIn(max = 340.dp)
                                .animateContentSize(
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                ),
                            onSwipe = {
                                vm.addServer()
                            },
                            swipeTint = "继续上拉添加服务器",
                            finishTint = "✅ 已添加成功",
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            items(
                                items = vm.serverFields,
                                key = { it.id },
                            ) { field ->
                                Column(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        fadeOutSpec = spring(stiffness = Spring.StiffnessLow)
                                    )
                                ) {
                                    Text(
                                        text = field.name,
                                        modifier = Modifier.combinedClickable(
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                vm.editingServerItem(field)
                                            },
                                            hapticFeedbackEnabled = true,
                                            onClick = {

                                            },
                                            interactionSource = null,
                                            indication = null
                                        )
                                    )
                                    SwipeBox(
                                        onDelete = {
                                            vm.deleteServer(field.id)
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = field.url,
                                                onValueChange = {
                                                    vm.updateServerURL(
                                                        id = field.id,
                                                        url = it
                                                    )
                                                },
                                                placeholder = { Text("请输入服务器地址") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                painter = if (field.isSelected)
                                                    painterResource(R.drawable.pet_ok)
                                                else
                                                    painterResource(R.drawable.pet_no),
                                                modifier = Modifier
                                                    .padding(start = 10.dp)
                                                    .clickable(
                                                        onClick = {
                                                            vm.selectServer(field.id)
                                                        },
                                                        interactionSource = null,
                                                        indication = null
                                                    ),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (vm.editingServerItem.value != null) {
                            AlertDialog(
                                onDismissRequest = { vm.editingServerItem.value = null },
                                title = { Text("修改名称") },
                                text = {
                                    OutlinedTextField(
                                        value = vm.editingServerName.value,
                                        onValueChange = {
                                            vm.updateEditingServerName(it)
                                        },
                                        placeholder = {
                                            Text(
                                                text = vm.editingServerItem.value?.name ?: "未命名"
                                            )
                                        },
                                        singleLine = true
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            vm.editingServerItem.value?.let {
                                                val success = vm.renameServer(
                                                    id = it.id,
                                                    name = vm.editingServerName.value
                                                )
                                                if (success) {
                                                    Toast.makeText(
                                                        context,
                                                        "重命名成功",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    vm.editingServerItem.value = null
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "重命名失败：名称不能为空或重复",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("确定")
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = {
                                        vm.editingServerItem.value = null
                                    }) {
                                        Text("取消")
                                    }
                                }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    ServerManager.currentUrl?.let {
                                        navController.navigate(Route.Readme.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.server_accepted_subscriptions),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.server_accepted_subscriptions_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    ServerManager.currentUrl?.let {
                                        navController.navigate(Route.Login.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                "登录服务器",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "服务器需要登录才能继续订阅客户端哟~",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                stringResource(R.string.subscriptions_and_filters),
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.padding(horizontal = 15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    vm.toggleSubscriptions()
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.subscription_clients),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.subscription_clients_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.rotate(subsButtonRotateDegree)
                        )
                    }
                    AnimatedVisibility(visible = uiState.isShowSubscriptions) {
                        if (uiState.subscribedClientsState is SubsState.Loading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 15.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "加载中...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (uiState.subscribedClientsState is SubsState.Error) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 15.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "加载失败",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (vm.subscribedClients.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 15.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = {
                                                vm.showAddSubsDialog()
                                            },
                                            interactionSource = null,
                                            indication = null
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.add),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        "点击添加订阅",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            SwipeLazyColumn(
                                listState = subsState,
                                modifier = Modifier
                                    .padding(horizontal = 15.dp)
                                    .padding(bottom = 10.dp)
                                    .heightIn(max = 340.dp)
                                    .animateContentSize(
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ),
                                onSwipe = {
                                    vm.showAddSubsDialog()
                                },
                                swipeTint = "继续上拉添加订阅",
                                finishTint = "✅ 选择订阅",
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                items(vm.subscribedClients) { sub ->
                                    SwipeBox(
                                        modifier = Modifier,
                                        onDelete = {
                                            vm.deleteSubscription(sub.subscriptionId)
                                        }
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            onClick = {
                                                ServerManager.currentUrl?.let {
                                                    navController.navigate(
                                                        Route.Edit.updateRoute(
                                                            client = sub.client,
                                                            subscriptionId = sub.subscriptionId
                                                        )
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 15.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                ) {
                                                    Text(
                                                        "bili: BiliClient",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "URL: https://www.bilibili.com",
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                                Icon(
                                                    painter = painterResource(R.drawable.chevron_right),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (uiState.isShowAddSubsDialog) {
                        ClientsDialog(onBack = { vm.closeAddSubsDialog() }, vm = vm)
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    navController.navigate(Route.GlobalFilters.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.global_filters),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.global_filters_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                "关于&客户端适配",
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.padding(horizontal = 15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    showAboutMeDialog = true
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                "关于作者",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "竟然一个人完成了Android和NotiCat Server的初期开发吗？有点意思喵。竟然是大学生吗？怪不得这么闲喵。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.rotate(subsButtonRotateDegree)
                        )
                    }
                    if (showAboutMeDialog) {
                        AboutMeDialog(onBack = { showAboutMeDialog = false })
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    showPayCodeDialog = true
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                "支持开发",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "如果 NotiCat 帮助到你，向开发者捐赠表示支持喵",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.rotate(subsButtonRotateDegree)
                        )
                    }
                    if (showPayCodeDialog) {
                        Dialog(
                            onDismissRequest = { showPayCodeDialog = false }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.paycode),
                                contentDescription = null
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                "客户端适配",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "在 Github 提出 Issue 或者尝试自己搭建 NotiCat Server 以适配新的客户端",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}