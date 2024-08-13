package com.example.yestion_ms

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
import kotlin.math.roundToInt


@Composable
fun WorkSpaceScreen(
    viewModel: ContentViewModel
) {
    var movingOffset by remember { mutableIntStateOf(0) }
    var onMoveToIndex by remember { mutableIntStateOf(0) }
    var onMoveFromIndex by remember { mutableIntStateOf(0) }
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
            Log.d("content test", it.toString())
            DraggableItem(
                viewModel = viewModel,
                item = it,
                onMove = { yPosition, fromIndex, height ->
                    Log.d("fromIndex",fromIndex.toString())
                    var toIndex = 0
                    for ((index, value) in yPositionList.withIndex()) {
                        if (yPosition > value) {
                            toIndex = index
                            Log.d("index change", toIndex.toString())
                        } else {
                            break
                        }
                    }
                    movingOffset = when {
                        onMoveToIndex > onMoveFromIndex -> -height
                        onMoveToIndex < onMoveFromIndex -> height
                        else -> 0
                    }
                    onMoveToIndex = toIndex
                    onMoveFromIndex = fromIndex
                },
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
                    movingOffset = 0
                    yPositionList.clear()
                    viewModel.moveItem(fromIndex, toIndex)
                },
                onFocusLost = { changedText ->
                    viewModel.updateFirebase(
                        it.id,
                        changedText,
                        it.typeFlag,
                        if (it.sequence > 0) it.sequence else maxSequence + 1
                    )
                },
                onPositionSave = { yPositionCenter ->
                    Log.d("yPosition check",yPositionList.toString())
                    if (maxSequence > yPositionList.size) yPositionList.add(yPositionCenter)
                },
                movingOffset,
                onMoveToIndex,
                onMoveFromIndex
            )
        }
    }
}

@Composable
fun DraggableItem(
    viewModel: ContentViewModel,
    item: Items,
    onMove: (Float, Int, Int) -> Unit,
    onDragEnd: (Float, Int) -> Unit,
    onFocusLost: (String) -> Unit,
    onPositionSave: (Float) -> Unit,
    movingOffset: Int,
    onMoveToIndex: Int,
    onMoveFromIndex: Int
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val zIndex = if (offsetY != 0f) 1f else 0f
    var isDragging by remember { mutableStateOf(false) }
    var yPosition by remember { mutableFloatStateOf(0f) }
    var height by remember { mutableIntStateOf(0) }
    var isFocused by remember { mutableStateOf(false) }

    var highIndex = onMoveToIndex
    var lowIndex = onMoveToIndex
    if (onMoveToIndex > onMoveFromIndex) {
        lowIndex = onMoveFromIndex + 1
        highIndex += 1
    } else highIndex = onMoveFromIndex

    val modifier = Modifier
        .background(color = Color.Transparent)
        .fillMaxWidth()
        .offset {
            IntOffset(
                0, offsetY.roundToInt() +
                        if (item.sequence in (lowIndex + 1)..highIndex) movingOffset else 0
            )
        }
        .zIndex(zIndex)
        .border(width = 2.dp, color = if (isDragging) Color.Cyan else Color.Transparent)
        .onGloballyPositioned { layoutCoordinates ->
            yPosition = layoutCoordinates.positionInWindow().y
            val size: IntSize = layoutCoordinates.size
            height = size.height
            if (!isDragging) onPositionSave(yPosition - (height / 2))
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
                    onFocusLost = { changedText -> onFocusLost(changedText) })

                "body" -> isFocused = bodyBox(input = item.contents,
                    onFocusLost = { changedText -> onFocusLost(changedText) })
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
                            onTypeChangeOption = { viewModel.typeChange(item, if (item.typeFlag=="body") "title" else "body") }
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
                                    onDragEnd(yPosition, item.sequence - 1)
                                    offsetY = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    offsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    offsetY += dragAmount.y
                                    change.consume()
                                    Log.d("onDrag ID", item.id)
                                    // 드래그 하는 동안 visualize하기 위한 onMove함수
                                    onMove(yPosition, item.sequence - 1, height)
                                }
                            )
                        }
                )
            }
        }
    }
}

@Composable
fun FloatingMenu(onDeleteOption: () -> Unit,
                 onTypeChangeOption: () -> Unit,) {
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
fun MenuItem(imageVector: Int,
             onTapOptions: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    onTapOptions()
                }
            }
    ) {
        Icon(imageVector = ImageVector.vectorResource(id = imageVector) , contentDescription = null)
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