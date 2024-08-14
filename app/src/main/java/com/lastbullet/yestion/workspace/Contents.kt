package com.lastbullet.yestion.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lastbullet.yestion.ContentViewModel
import com.lastbullet.yestion.Items

@Composable
fun titleBox(
    input: String,
    viewModel: ContentViewModel,
    item: Items,
    modifier: Modifier = Modifier
): Boolean {
    var title by remember { mutableStateOf(input) }
    var isFocused by remember { mutableStateOf(false) }
    val maxSequence = viewModel.getMaxSequence()
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
            .background(color = Color.Transparent),
        contentAlignment = Alignment.Center
    )
    {
        TextField(
            value = title,
            onValueChange = {
                title = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused && title != "") {
                        val newItem = Items(
                            item.id,
                            title,
                            item.typeFlag,
                            if (item.sequence > 0) item.sequence else maxSequence + 1
                        )
                        viewModel.updateFirebase(newItem)
                    }
                    isFocused = focusState.isFocused
                },
            placeholder = {
                Text(
                    text = "제목 없음",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
    return isFocused
}

@Composable
fun bodyBox(
    input: String,
    viewModel: ContentViewModel,
    item: Items,
    modifier: Modifier = Modifier
): Boolean {
    var content by remember { mutableStateOf(input) }
    var isFocused by remember { mutableStateOf(false) }
    val maxSequence = viewModel.getMaxSequence()
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
            .background(color = Color.Transparent),
        contentAlignment = Alignment.Center
    )
    {
        TextField(
            value = content,
            onValueChange = {
                content = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused && content != "") {
                        val newItem = Items(
                            item.id,
                            content,
                            item.typeFlag,
                            if (item.sequence > 0) item.sequence else maxSequence + 1
                        )
                        viewModel.updateFirebase(newItem)
                    }
                    isFocused = focusState.isFocused
                },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                fontSize = 15.sp
            )
        )
    }
    return isFocused
}