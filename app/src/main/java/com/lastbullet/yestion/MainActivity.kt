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
        Button(onClick = {
            LoginFunction().signUpOnClick(
                UserInfo(
                    uid = user,
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