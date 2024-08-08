package com.lastbullet.yestion

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    var fontWeight by remember { mutableIntStateOf(android.graphics.Typeface.NORMAL) }
    var fontcolor by remember { mutableIntStateOf(android.graphics.Color.WHITE) }
    var isUnderlined by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(), // Occupy the max size in the Compose UI tree
            factory = { context ->
                EditText(context).apply {

                    setAutofillHints("")
                    setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            },
            update = { view ->
                val start = view.selectionStart
                val end = view.selectionEnd

                val spannableString = SpannableString(view.text)
                spannableString.setSpan(
                    ForegroundColorSpan(fontcolor),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                spannableString.setSpan(
                    StyleSpan(fontWeight),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                if (isUnderlined) {
                    spannableString.setSpan(
                        UnderlineSpan(),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
                view.setText(spannableString)
                view.setSelection(start, end)
            }
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    fontcolor = android.graphics.Color.RED
                },
                colors = ButtonDefaults.buttonColors(Color.Red),
                modifier = Modifier.size(48.dp)
            ) {
            }
            Button(
                onClick = {
                    fontcolor = android.graphics.Color.GREEN
                },
                colors = ButtonDefaults.buttonColors(Color.Green),
                modifier = Modifier.size(48.dp)
            ) {
            }
            Button(
                onClick = {
                    fontcolor = android.graphics.Color.BLUE
                },
                colors = ButtonDefaults.buttonColors(Color.Blue),
                modifier = Modifier.size(48.dp),

                ) {
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    fontWeight = android.graphics.Typeface.BOLD
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Text(text = "Bold", color = Color.Black)
            }
            Button(
                onClick = {
                    fontWeight = android.graphics.Typeface.ITALIC
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Text(text = "Italic", color = Color.Black)
            }
            Button(
                onClick = {
                    isUnderlined = !isUnderlined
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Text(text = "Upright")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewTextView() {
    TextView()
}
