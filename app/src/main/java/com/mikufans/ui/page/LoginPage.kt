package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mikufans.api.UserApi
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.miku.entiry.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
  navController: NavController,
  baseHorizontalPadding: Dp,
  email: String
) {
  var emailAccount by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var subPassword by rememberSaveable { mutableStateOf("") }
  var verificationCode by rememberSaveable { mutableStateOf("") }
  var isRegister by rememberSaveable { mutableStateOf(false) }
  var passwordVisible by rememberSaveable { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(
        title = { Text("登录") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "返回"
            )
          }
        }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 标题区域
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "欢迎回来",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      AnimatedVisibility(visible = false) {
        Text(
          text = "我们将发送一个验证码到您的邮箱",
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
      // 输入框区域
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // 邮箱输入
          OutlinedTextField(
            readOnly = email.isNotBlank(),
            value = emailAccount,
            onValueChange = { emailAccount = it; },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("电子邮件") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "email"
              )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
          )

          AnimatedVisibility(visible = email.isBlank()) {
            // 密码输入
            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              modifier = Modifier.fillMaxWidth(),
              label = { Text("密码") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Password,
                  contentDescription = "password"
                )
              },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true
            )
          }

          // 再次输入密码确认
          AnimatedVisibility(visible = isRegister) {
            OutlinedTextField(
              value = subPassword,
              onValueChange = { subPassword = it },
              modifier = Modifier.fillMaxWidth(),
              label = { Text("确认密码") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Password,
                  contentDescription = "password"
                )
              },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true
            )
          }
        }

        // 登录按钮
        Button(
          onClick = {
            if (password.trim() != subPassword) {
              Toast.makeText(
                navController.context,
                "密码不一致",
                Toast.LENGTH_SHORT
              ).show()
            }
            if (email.isNotBlank()) {
              LocalStorage.set(navController.context, "email", "")
              LocalStorage.set(navController.context, "token", "")
              return@Button
            }
            if (isRegister) {
              coroutineScope.launch(Dispatchers.IO) {
                var success = false
                try {
                  success = UserApi.register(
                    User(
                      email = emailAccount,
                      password = password
                    )
                  )
                } catch (e: Exception) {
                  Log.e("LoginPage-register-err:", e.message, e)
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      e.message,
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
                if (success) {
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      "注册成功",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                  isRegister = false
                } else {
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      "注册失败",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              }

            } else {
              coroutineScope.launch(Dispatchers.IO) {
                var token: String
                try {
                  token = UserApi.login(
                    User(
                      email = emailAccount,
                      password = password
                    )
                  )
                } catch (e: Exception) {
                  e.printStackTrace()
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      e.message,
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                  return@launch
                }
                if (token.isNotBlank()) {
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      "登录成功",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                  LocalStorage.set(navController.context, "token", token)
                  LocalStorage.set(navController.context, "email", emailAccount)
                  navController.navigate("index")
                } else {
                  withContext(Dispatchers.Main) {
                    Toast.makeText(
                      navController.context,
                      "登录失败",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
          enabled = emailAccount.isNotBlank() && password.isNotBlank()
        ) {
          Text(
            text = if (email.isNotBlank()) "登出" else if (isRegister) "注册" else "验证",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // 底部链接
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          TextButton(onClick = {
            isRegister = !isRegister
          }) {
            Text("忘记密码？")
          }
          Spacer(modifier = Modifier.weight(1f))
          TextButton(onClick = {
            isRegister = !isRegister
          }) {
            Text("注册帐号")
          }
        }

        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}
