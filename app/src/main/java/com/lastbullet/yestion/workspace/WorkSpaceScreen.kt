package com.lastbullet.yestion

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.lastbullet.yestion.workspace.DragIcon
import com.lastbullet.yestion.workspace.bodyBox
import com.lastbullet.yestion.workspace.titleBox
import kotlin.math.roundToInt


@Composable
fun WorkSpaceScreen(
    viewModel: ContentViewModel
) {
    val contentState = viewModel.contentListState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, bottom = 40.dp)
    ) {
        val yPositionList = mutableSetOf<Float>()
        yPositionList.clear()
        items(
            contentState.value,
            key = { "${it.id}_${it.contents}_${it.sequence}_${it.typeFlag}" }) { it ->
            viewModel.sortList()
            Log.d("content test", it.toString())
            DraggableItem(
                viewModel = viewModel,
                item = it
            )
        }
    }
}

@Composable
fun DraggableItem(
    viewModel: ContentViewModel,
    item: Items
) {
    val movingViewModelState by viewModel.movingState.collectAsState()
    val movingViewModel = viewModel.movingState

    var offsetY by remember { mutableFloatStateOf(0f) }
    val zIndex = if (offsetY != 0f) 1f else 0f
    var isDragging by remember { mutableStateOf(false) }
    var yPosition by remember { mutableFloatStateOf(0f) }
    var height by remember { mutableIntStateOf(0) }
    var isFocused by remember { mutableStateOf(false) }

    val highIndex =
        if (movingViewModelState.onMoveToIndex > movingViewModelState.onMoveFromIndex) {
            movingViewModelState.onMoveToIndex + 1
        } else movingViewModelState.onMoveFromIndex
    val lowIndex =
        if (movingViewModelState.onMoveToIndex > movingViewModelState.onMoveFromIndex) {
            movingViewModelState.onMoveFromIndex + 1
        } else movingViewModelState.onMoveToIndex

    val modifier = Modifier
        .background(color = Color.Transparent)
        .fillMaxWidth()
        .offset {
            IntOffset(
                0, offsetY.roundToInt() +
                        if (item.sequence - 1 in lowIndex until highIndex) {
                            movingViewModelState.movingOffset
                        } else 0
            )
        }
        .zIndex(zIndex)
        .border(width = 2.dp, color = if (isDragging) Color.Cyan else Color.Transparent)
        .onGloballyPositioned { layoutCoordinates ->
            yPosition = layoutCoordinates.positionInWindow().y
            Log.d("yPositionCheck", yPosition.toString())
            val size: IntSize = layoutCoordinates.size
            height = size.height
            val centerPosition = yPosition + (height / 2)
            if (!isDragging) movingViewModel.value = movingViewModel.value.copy(
                yPositionList = movingViewModel.value.yPositionList + centerPosition
            )
            Log.d("yPositionListCheck1", movingViewModelState.yPositionList.toString())
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent),
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            var isMenuToggled by remember { mutableStateOf(false) }
            when (item.typeFlag) {
                "title" -> isFocused = titleBox(input = item.contents, viewModel, item)
                "body" -> isFocused = bodyBox(input = item.contents, viewModel, item)
            }
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                        .background(color = Color.Transparent)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu),
                        contentDescription = null,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    isMenuToggled = !isMenuToggled
                                }
                            }
                    )
                }
                if (isMenuToggled) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        onDismissRequest = { isMenuToggled = false },
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
                DragIcon(viewModel,
                    item,
                    yPosition,
                    height,
                    onDragStateChange = { isDragging = it },
                    onOffsetReset = {offsetY = 0f},
                    onOffsetChange = {offsetY = it})
            }
        }
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

//@Preview(showBackground = true)
//@Composable
//fun WorkSpacePreview() {
//    Yestion_msTheme {
//        WorkSpaceScreen()
//    }
//}