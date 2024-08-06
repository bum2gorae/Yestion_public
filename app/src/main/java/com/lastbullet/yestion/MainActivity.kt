package com.lastbullet.yestion

import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.rememberCompositionContext
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
                Greeting(this)
            }
        }
    }
}

@Composable
fun Greeting(context : Context) {
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
            Toast.makeText(context,"메시지 전달 완료",Toast.LENGTH_SHORT).show()
            //firebase에 연동되는 것을 확인. 임시로 막아두었습니다.
//            LoginFunction().sendMessage(
//                Message(
//                    userId = user,
//                    text = text,
//                    timestamp = LocalDateTime.now()
//                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm:ss"))
//                )
//            )
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
            // firebase에 연동되는 것을 확인. 임시로 막아두었습니다.
//            LoginFunction().signUpOnClick(
//                UserInfo(
//                    userId = user,
//                    password = text
//                )
//            )
            Toast.makeText(context,"가입 정보 전달 완료",Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "Sign up")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YestionTheme {
        Greeting(MainActivity())
    }
}