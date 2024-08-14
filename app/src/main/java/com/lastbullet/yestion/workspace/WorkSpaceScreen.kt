package com.lastbullet.yestion

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun WorkSpaceScreen(
    viewModel: ContentViewModel
) {
    val contentState = viewModel.contentListState.collectAsState()
    val onMovingState = viewModel.movingState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, bottom = 40.dp)
    ) {
        val maxSequence = viewModel.getMaxSequence()
        val yPositionList = mutableSetOf<Float>()
        yPositionList.clear()
        items(
            contentState.value,
            key = { "${it.id}_${it.contents}_${it.sequence}_${it.typeFlag}" }) { it ->
            viewModel.sortList()
            Log.d("content test", it.toString())
            DraggableItem(
                viewModel = viewModel,
                item = it,
                onDragEnd = { yPosition, fromIndex ->
                    var toIndex = 0
                    for ((index, value) in yPositionList.withIndex()) {
                        Log.d("yPositionList", yPositionList.toString())
                        Log.d("yPosition", "yPosition:$yPosition index:$index, value:$value")
                        if (yPosition > value) {
                            toIndex = index
                            Log.d("index change", toIndex.toString())
                        } else {
                            break
                        }
                    }
                    viewModel.movingState.value.movingOffset = 0
                    yPositionList.clear()
                    viewModel.movingState.value.yPositionList = emptySet()
                    viewModel.moveItem(fromIndex, toIndex)
                }
            )
        }
    }
}

@Composable
fun DraggableItem(
    viewModel: ContentViewModel,
    item: Items,
    onDragEnd: (Float, Int) -> Unit
) {
    val contentViewModelStateValue = viewModel.contentListState
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
    val maxSequence = viewModel.getMaxSequence()

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
                "title" -> isFocused = titleBox(input = item.contents,
                    onFocusLost = { changedText ->
                        val newItem = Items(
                            item.id,
                            changedText,
                            item.typeFlag,
                            if (item.sequence > 0) item.sequence else maxSequence + 1
                        )
                        viewModel.updateFirebase(newItem)
                    })

                "body" -> isFocused = bodyBox(input = item.contents,
                    onFocusLost = { changedText ->
                        val newItem = Items(
                            item.id,
                            changedText,
                            item.typeFlag,
                            if (item.sequence > 0) item.sequence else maxSequence + 1
                        )
                        viewModel.updateFirebase(newItem)
                    })
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
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_drag_handle),
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    isDragging = false
                                    Log.d("toindextest",movingViewModelState.onMoveToIndex.toString())
                                    viewModel.moveItem(
                                        fromIndex = movingViewModelState.onMoveFromIndex,
                                        toIndex = movingViewModelState.onMoveToIndex)
                                    viewModel.movingState.value = viewModel.movingState.value.copy(
                                        onMoveToIndex = 0,
                                        onMoveFromIndex = 0,
                                        movingOffset = 0,
                                        yPositionList = emptySet()
                                    )
                                    offsetY = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    offsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    offsetY += dragAmount.y
                                    change.consume()
                                    var tempToIndex = 0
                                    for ((index, value) in movingViewModelState.yPositionList.withIndex()) {
                                        if (yPosition > value) {
                                            tempToIndex = index + 1
                                            Log.d("tempindextest",tempToIndex.toString())
                                        } else {
                                            break
                                        }
                                    }
                                    movingViewModel.value =
                                        movingViewModel.value.copy(onMoveToIndex = tempToIndex.let { if (it>maxSequence) maxSequence-1 else it})
                                    movingViewModel.value =
                                        movingViewModel.value.copy(onMoveFromIndex = item.sequence - 1)
                                    movingViewModel.value = movingViewModel.value.copy(
                                        movingOffset = when {
                                            movingViewModelState.onMoveToIndex > movingViewModelState.onMoveFromIndex -> -height
                                            movingViewModelState.onMoveToIndex < movingViewModelState.onMoveFromIndex -> height
                                            else -> 0
                                        }
                                    )
                                }
                            )
                        }
                )
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


@Composable
fun titleBox(
    input: String,
    onFocusLost: (String) -> Unit,
    modifier: Modifier = Modifier
): Boolean {
    var title by remember { mutableStateOf(input) }
    var isFocused by remember { mutableStateOf(false) }
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
                        onFocusLost(title)
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
    onFocusLost: (String) -> Unit,
    modifier: Modifier = Modifier
): Boolean {
    var content by remember { mutableStateOf(input) }
    var isFocused by remember { mutableStateOf(false) }
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
                        onFocusLost(content)
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

//@Preview(showBackground = true)
//@Composable
//fun WorkSpacePreview() {
//    Yestion_msTheme {
//        WorkSpaceScreen()
//    }
//}