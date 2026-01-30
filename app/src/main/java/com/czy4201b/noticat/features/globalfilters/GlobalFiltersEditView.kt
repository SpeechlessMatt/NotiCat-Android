package com.czy4201b.noticat.features.globalfilters

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.components.ModernSwitch
import com.czy4201b.noticat.core.components.ModernSwitchDefaults
import com.czy4201b.noticat.core.components.SwipeBox
import androidx.compose.ui.window.Dialog

@Composable
fun GlobalFiltersEditView(
    onBack: () -> Unit,
    vm: GlobalFiltersEditViewViewModel
) {
    val context = LocalContext.current
    val uiState by vm.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event){
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.isLoading) {
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
                    stringResource(R.string.global_filters),
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
                        vm.saveFilters()
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
            modifier = Modifier.padding(innerPadding),
            state = listState
        ) {
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
                                stringResource(R.string.global_filters),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.global_filters_desc),
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
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onDelete = {
                        vm.deleteFilter(field.id)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = spring(stiffness = Spring.StiffnessLow)
                            ),
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

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}