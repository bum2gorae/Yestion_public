package com.lastbullet.yestion.workspace

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lastbullet.yestion.ContentViewModel
import com.lastbullet.yestion.Items
import com.lastbullet.yestion.R

@Composable
fun DragIcon(
    viewModel: ContentViewModel,
    item: Items,
    yPositionInit: Float,
    height: Int,
    onDragStateChange: (Boolean) -> Unit,
    onOffsetReset: () -> Unit,
    onOffsetChange: (Float) -> Unit) {
    val movingViewModelState by viewModel.movingState.collectAsState()
    val movingViewModel = viewModel.movingState
    val maxSequence = viewModel.getMaxSequence()
    var yPosition by remember { mutableFloatStateOf(yPositionInit) }
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_drag_handle),
        contentDescription = null,
        modifier = Modifier
            .padding(5.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStateChange(true)
                    },
                    onDragEnd = {
                        onDragStateChange(false)
                        Log.d(
                            "toindextest",
                            movingViewModelState.onMoveToIndex.toString()
                        )
                        viewModel.moveItem(
                            fromIndex = movingViewModelState.onMoveFromIndex,
                            toIndex = movingViewModelState.onMoveToIndex
                        )
                        viewModel.movingState.value = viewModel.movingState.value.copy(
                            onMoveToIndex = 0,
                            onMoveFromIndex = 0,
                            movingOffset = 0,
                            yPositionList = emptySet()
                        )
                        onOffsetReset()
                    },
                    onDragCancel = {
                        onDragStateChange(false)
                        onOffsetReset()
                    },
                    onDrag = { change, dragAmount ->
                        yPosition += dragAmount.y
                        onOffsetChange(dragAmount.y)
                        change.consume()
                        var tempToIndex = 0
                        for ((index, value) in movingViewModelState.yPositionList.withIndex()) {
                            if (yPosition > value) {
                                tempToIndex = index + 1
                                Log.d("tempindextest", tempToIndex.toString())
                            } else {
                                break
                            }
                        }
                        movingViewModel.value =
                            movingViewModel.value.copy(onMoveToIndex = tempToIndex.let { if (it > maxSequence) maxSequence - 1 else it })
                        movingViewModel.value =
                            movingViewModel.value.copy(onMoveFromIndex = item.sequence - 1)
                        movingViewModel.value = movingViewModel.value.copy(
                            movingOffset = when {
                                movingViewModelState.onMoveToIndex > movingViewModelState.onMoveFromIndex -> -height
                                movingViewModelState.onMoveToIndex < movingViewModelState.onMoveFromIndex -> height
                                else -> 0
                            }
                        )
                        Log.d("heightCheck", viewModel.movingState.value.movingOffset.toString())
                    }
                )
            }
    )

}