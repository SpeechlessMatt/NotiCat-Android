package com.czy4201b.noticat.features.edit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.components.ModernCheckBox
import com.czy4201b.noticat.core.components.ModernSwitch
import com.czy4201b.noticat.core.components.ModernSwitchDefaults
import com.czy4201b.noticat.core.components.SwipeBox

@Composable
fun EditClientView(
    onBack: () -> Unit,
    vm: EditClientViewViewModel
) {
    val context = LocalContext.current
    val uiState by vm.state.collectAsState()
    val listState = rememberLazyListState()

    // lazy to place them in vm...(caz easy logic)
    var accountVisible by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is UiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    if (uiState.subsState is SubsState.Loading) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            Surface(
                onClick = {
                    vm.addFilter()
                },
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.size(56.dp),
                tonalElevation = 6.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add"
                    )
                }
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 40.dp,
                        bottom = 10.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    uiState.clientName,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
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
                IconButton(
                    onClick = {
                        vm.createSubscription()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 5.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.save),
                        contentDescription = null,
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            state = listState
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 5.dp)
                    ) {
                        Text(uiState.clientDesc)
                    }
                }
            }

            if (uiState.isShowCred) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.credentials),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.credentials_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                }

                item {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .padding(horizontal = 20.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.account,
                                onValueChange = {
                                    vm.updateAccount(it)
                                },
                                placeholder = { Text(stringResource(R.string.account_hint)) },
                                modifier = Modifier.weight(1f),
                                visualTransformation = if (accountVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            )
                            IconButton(
                                onClick = {
                                    accountVisible = !accountVisible
                                },
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    painter = if (accountVisible)
                                        painterResource(R.drawable.visibility)
                                    else painterResource(
                                        R.drawable.visibility_off
                                    ),
                                    contentDescription = null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = {
                                    vm.updatePasswd(it)
                                },
                                placeholder = { Text(stringResource(R.string.password_hint)) },
                                modifier = Modifier.weight(1f),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            )
                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                },
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    painter = if (passwordVisible)
                                        painterResource(R.drawable.visibility)
                                    else painterResource(
                                        R.drawable.visibility_off
                                    ),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isShowExtra) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.extra),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.extra_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(
                    items = vm.extraFields,
                    key = { field -> field.id }
                ) { field ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 5.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = field.value,
                                onValueChange = {
                                    vm.updateExtra(id = field.id, value = it)
                                },
                                placeholder = {
                                    Text(
                                        stringResource(
                                            R.string.please_input,
                                            field.label
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {

                                },
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    painter = painterResource(R.drawable.help),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }

            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .weight(1f),
                        ) {
                            Text(
                                stringResource(R.string.filters),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.filters_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(
                items = vm.filterFields,
                key = { field -> field.id }
            ) { field ->
                SwipeBox(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .animateContentSize(
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        ),
                    onDelete = {
                        vm.deleteFilter(field.id)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 5.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = field.pattern,
                                onValueChange = {
                                    vm.updateFilterPattern(field.id, it)
                                },
                                isError = field.isError,
                                placeholder = { Text(stringResource(R.string.filters_hint)) },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    vm.toggleFilter(field.id)
                                },
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    painter = painterResource(R.drawable.tune),
                                    contentDescription = null
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = field.isUnfold
                        ) {
                            Card(
                                modifier = Modifier.padding(end = 48.dp, top = 5.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .padding(top = 5.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.regex),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ModernSwitch(
                                            modifier = Modifier.padding(end = 5.dp),
                                            checked = field.filterType == "regex",
                                            onCheckedChange = {
                                                vm.setFilterType(
                                                    id = field.id,
                                                    filterType = if (it) "regex" else "keyword"
                                                )
                                            },
                                            colors = ModernSwitchDefaults.colors(
                                                checkedTrack = MaterialTheme.colorScheme.secondary.copy(
                                                    alpha = 0.3f
                                                ),
                                                checkedThumb = MaterialTheme.colorScheme.secondary,
                                                uncheckedTrack = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.08f
                                                ),
                                                uncheckedThumb = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.4f
                                                ),
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .padding(bottom = 5.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.ignore_case),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ModernSwitch(
                                            modifier = Modifier.padding(end = 5.dp),
                                            checked = !field.isIgnoreCase,
                                            onCheckedChange = {
                                                // because it is "isIgnoreCase" but the switch label is "区分大小写"
                                                vm.setIgnoreCase(
                                                    id = field.id,
                                                    isIgnoreCase = !it
                                                )
                                            },
                                            colors = ModernSwitchDefaults.colors(
                                                checkedTrack = MaterialTheme.colorScheme.secondary.copy(
                                                    alpha = 0.3f
                                                ),
                                                checkedThumb = MaterialTheme.colorScheme.secondary,
                                                uncheckedTrack = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.08f
                                                ),
                                                uncheckedThumb = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.4f
                                                ),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (uiState.mode is EditMode.Create){
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "应用全局规则",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        ModernCheckBox(
                            checked = uiState.isApplyGlobalFilters,
                            onCheckedChange = {
                                vm.toggleApplyGlobalFilters(it)
                            }
                        )
                    }
                }

                items(
                    items = vm.globalFilterFields,
                    key = { field -> field.id }
                ) { field ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 5.dp)
                            .fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Transparent,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    field.pattern,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (uiState.isApplyGlobalFilters)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    vm.toggleGlobalFilter(field.id)
                                },
                                enabled = uiState.isApplyGlobalFilters
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    painter = painterResource(R.drawable.tune),
                                    contentDescription = null
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = field.isUnfold
                        ) {
                            Card(
                                modifier = Modifier.padding(end = 48.dp, top = 5.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .padding(top = 5.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.regex),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ModernSwitch(
                                            modifier = Modifier.padding(end = 5.dp),
                                            checked = field.filterType == "regex",
                                            onCheckedChange = {
                                                vm.setFilterType(
                                                    id = field.id,
                                                    filterType = if (it) "regex" else "keyword"
                                                )
                                            },
                                            colors = ModernSwitchDefaults.colors(
                                                checkedTrack = MaterialTheme.colorScheme.secondary.copy(
                                                    alpha = 0.3f
                                                ),
                                                checkedThumb = MaterialTheme.colorScheme.secondary,
                                                uncheckedTrack = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.08f
                                                ),
                                                uncheckedThumb = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.4f
                                                ),
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .padding(bottom = 5.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.ignore_case),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ModernSwitch(
                                            modifier = Modifier.padding(end = 5.dp),
                                            checked = !field.isIgnoreCase,
                                            onCheckedChange = {},
                                            colors = ModernSwitchDefaults.colors(
                                                checkedTrack = MaterialTheme.colorScheme.secondary.copy(
                                                    alpha = 0.3f
                                                ),
                                                checkedThumb = MaterialTheme.colorScheme.secondary,
                                                uncheckedTrack = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.08f
                                                ),
                                                uncheckedThumb = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.4f
                                                ),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}