package com.lastbullet.yestion.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lastbullet.yestion.ContentViewModel
import com.lastbullet.yestion.Items
import com.lastbullet.yestion.R

@Composable
fun PopupMenuBar(viewModel: ContentViewModel,
                 item: Items,
                 onDismissRequest: (Boolean) -> Unit ) {
    Popup(
    alignment = Alignment.TopEnd,
    onDismissRequest = { onDismissRequest(false) },
    properties = PopupProperties(focusable = true)
    ) {
        FloatingMenu(
            onDeleteOption = { viewModel.removeContent(item) },
            onTypeChangeOption = {
                viewModel.typeChange(
                    item,
                    if (item.typeFlag == "body") "title" else "body"
                )
            }
        )
    }
}

@Composable
fun FloatingMenu(
    onDeleteOption: () -> Unit,
    onTypeChangeOption: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(end = 45.dp)
            .background(color = Color(0xfff7f4eb), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        MenuItem(R.drawable.ic_delete, { onDeleteOption() })
        MenuItem(R.drawable.ic_type_change, { onTypeChangeOption() })
    }
}

@Composable
fun MenuItem(
    imageVector: Int,
    onTapOptions: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    onTapOptions()
                }
            }
    ) {
        Icon(imageVector = ImageVector.vectorResource(id = imageVector), contentDescription = null)
    }
}