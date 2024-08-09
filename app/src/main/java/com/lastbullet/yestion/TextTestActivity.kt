package com.lastbullet.yestion

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
    var fontWeight by remember { mutableStateOf<Int?>(null) }
    var fontColor by remember { mutableStateOf<Int?>(null) }
    var isUnderlined by remember { mutableStateOf<Boolean?>(null) }
    var forceUpdate by remember { mutableStateOf(false) } // 새로운 flag 추가

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
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

                fontColor?.let { color ->
                    spannableString.setSpan(
                        ForegroundColorSpan(color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                fontWeight?.let { weight ->
                    if (weight != android.graphics.Typeface.NORMAL) {
                        spannableString.setSpan(
                            StyleSpan(weight),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        val styleSpans = spannableString.getSpans(start, end, StyleSpan::class.java)
                        for (span in styleSpans) {
                            if (span.style == android.graphics.Typeface.BOLD || span.style == android.graphics.Typeface.ITALIC) {
                                spannableString.removeSpan(span)
                            }
                        }
                    }
                }

                isUnderlined?.let { underlined ->
                    if (underlined) {
                        spannableString.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        val underlineSpans = spannableString.getSpans(start, end, UnderlineSpan::class.java)
                        for (span in underlineSpans) {
                            spannableString.removeSpan(span)
                        }
                    }
                }

                view.setText(spannableString)
                view.setSelection(start, end)

                // Reset style properties after applying
                fontColor = null
                fontWeight = null
                isUnderlined = null
                forceUpdate = false // Reset the flag

//                view.invalidate()
            }
        )

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    fontColor = android.graphics.Color.RED
                    forceUpdate = !forceUpdate // Toggle flag
                },
                colors = ButtonDefaults.buttonColors(Color.Red),
                modifier = Modifier.size(48.dp)
            ) {
            }
            Button(
                onClick = {
                    fontColor = android.graphics.Color.GREEN
                    forceUpdate = !forceUpdate // Toggle flag
                },
                colors = ButtonDefaults.buttonColors(Color.Green),
                modifier = Modifier.size(48.dp)
            ) {
            }
            Button(
                onClick = {
                    fontColor = android.graphics.Color.BLUE
                    forceUpdate = !forceUpdate // Toggle flag
                },
                colors = ButtonDefaults.buttonColors(Color.Blue),
                modifier = Modifier.size(48.dp)
            ) {
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    fontWeight = if (fontWeight == android.graphics.Typeface.BOLD) {
                        android.graphics.Typeface.NORMAL
                    } else {
                        android.graphics.Typeface.BOLD
                    }
                    forceUpdate = !forceUpdate // Toggle flag
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Text(text = "Bold", color = Color.Black)
            }
            Button(
                onClick = {
                    fontWeight = if (fontWeight == android.graphics.Typeface.ITALIC) {
                        android.graphics.Typeface.NORMAL
                    } else {
                        android.graphics.Typeface.ITALIC
                    }
                    forceUpdate = !forceUpdate // Toggle flag
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier.size(48.dp)
            ) {
                Text(text = "Italic", color = Color.Black)
            }
            Button(
                onClick = {
                    isUnderlined = !(isUnderlined ?: false)
                    forceUpdate = !forceUpdate // Toggle flag
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

//@Composable
//fun OnButtonClick(onClick : () -> Unit, text : Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(Color.White),
//        modifier = Modifier.size(48.dp)
//    ) {
//        text
//    }
//}
//
