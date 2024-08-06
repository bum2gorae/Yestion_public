package com.lastbullet.yestion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.lastbullet.yestion.ui.theme.YestionTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YestionTheme {
                Greeting()
            }
        }
    }
}

@Composable
fun Greeting() {
    Column {
        var user by remember { mutableStateOf("") }
        var text by remember { mutableStateOf("") }
        TextField(
            value = user,
            onValueChange = { user = it },
            label = { Text(text = "username") }
        )
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(text = "text") }
        )
"""
    username에서 받은 user, text에서 받은 text, 현재 시각을 담은 timestamp를 database에 보냅니다.
    보내진 데이터는 database의 "messages"에 저장됩니다.
    구조 :
 database
    ├messages
    │   ├userId
    │   ├text
    │   └timestamp
    └userinfo
""".trimIndent()
        Button(onClick = {
            LoginFunction().sendMessage(
                Message(
                    userId = user,
                    text = text,
                    timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm:ss"))
                )
            )
//            Toast.makeText(MainActivity(), "보내기 완료", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "send message")
        }
"""
     username에서 받은 user, text에서 받은 text를 database에 보냅니다.
     보내진 데이터는 database의 "userinfo"에 저장됩니다.
     TODO: 추후 로그인 화면에서 구현할 예정입니다.
    구조 :
 database
    ├messages
    └userinfo
        ├userId
        └password
""".trimIndent()
        Button(onClick = {
            LoginFunction().signUpOnClick(
                UserInfo(
                    userId = user,
                    password = text
                )
            )
        }) {
            Text(text = "Sign up")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YestionTheme {
        Greeting()
    }
}