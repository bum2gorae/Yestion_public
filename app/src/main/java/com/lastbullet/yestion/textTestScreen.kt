package com.lastbullet.yestion

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lastbullet.yestion.ui.theme.YestionTheme

class TextTestScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YestionTheme {
                TestText()
            }
        }
    }
}

@Composable
fun TestText() {
    Text(text = "이것은 텍스트 입니다. 테스트 중입니다.")
}

@Preview(showBackground = true)
@Composable
fun TextTestPreview(){
    TestText()
}
