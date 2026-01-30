package com.czy4201b.noticat.features.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.czy4201b.noticat.R
import com.czy4201b.noticat.core.theme.AppTheme

@Composable
internal fun LoginCard(
    onClose: () -> Unit,
    vm: LoginCardViewModel
) {
    val context = LocalContext.current
    val uiState by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is UiEvent.NavigateBack -> {
                    vm.showLogin()
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {

            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        when (val state = uiState.loginState) {
            is LoginState.Success -> {
                Box(
                    modifier = Modifier
                        .width(350.dp)
                        .height(300.dp)
                        .padding(20.dp),
                ) {
                    Text(
                        text = "您好，${
                            state.username.ifBlank { "Unknown" }.replaceFirstChar { it.uppercase() }
                        }",
                        modifier = Modifier.align(Alignment.TopStart),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.done_all),
                            tint = AppTheme.customColors.success,
                            modifier = Modifier
                                .padding(15.dp)
                                .size(50.dp),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier
                                .padding(end = 15.dp),
                            text = "登录成功",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (uiState.isRegister) "注册" else "登录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.isRegister) {
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = {
                                vm.updateEmail(it)
                            },
                            label = { Text("邮箱") },
                            isError = uiState.isEmailError,
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = uiState.account,
                        onValueChange = {
                            vm.updateAccount(it)
                        },
                        label = { Text(if (uiState.isRegister) "用户名" else "用户名或邮箱") },
                        isError = uiState.isAccountError,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = {
                            vm.updatePassword(it)
                        },
                        label = { Text("密码") },
                        singleLine = true,
                        isError = uiState.isAccountError,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (uiState.isRegister) {
                        OutlinedTextField(
                            value = uiState.code,
                            onValueChange = {
                                vm.updateCode(it)
                            },
                            label = { Text("验证码") },
                            isError = uiState.isCodeError,
                            singleLine = true
                        )

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,

                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.12f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f
                                )
                            ),
                            onClick = {
                                vm.sendCode()
                            },
                            enabled = uiState.sendCodeState !is SendCodeState.Cooling
                        ) {
                            when (val state = uiState.sendCodeState) {
                                SendCodeState.Unsend -> Text("发送验证码")
                                is SendCodeState.Cooling -> Text("等待${state.remainingSeconds}秒后重试")
                                else -> Text("重新发送验证码")
                            }
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (uiState.isRegister)
                                vm.registerAccount()
                            else
                                vm.loginAccount()
                        }
                    ) {
                        Text(
                            text = if (uiState.isRegister) "注册" else "登录"
                        )
                    }

                    Row {
                        TextButton(
                            onClick = {
                                vm.toggleRegister()
                            }
                        ) {
                            Text(
                                text = if (!uiState.isRegister) "注册" else "登录"
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = onClose
                        ) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }
}