package com.lastbullet.yestion

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lastbullet.yestion.ui.theme.YestionTheme

class TextTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YestionTheme {
                TextView()
            }
        }
    }
}


@Composable
fun TextView() {
    // TextFieldValue로 지정하지 않으면 단순 String으로 되어 selection이 먹히지 않음
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedText by remember { mutableStateOf("") }
    Column {
        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                // 선택된 텍스트가 있으면 업데이트
                if (newValue.selection.length > 0) {
                    selectedText = newValue.text.substring(
                        newValue.selection.start,
                        newValue.selection.end
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun View() {
    TextView()
}