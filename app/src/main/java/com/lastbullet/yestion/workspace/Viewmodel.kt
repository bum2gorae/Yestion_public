package com.lastbullet.yestion

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Items(
    val id: String,
    val contents: String,
    var typeFlag: String,
    var sequence: Int
)

data class dragData(
    //drag용 data class
    var onMoveFromIndex: Int, //drag시작 index
    var onMoveToIndex: Int, //drag목표 index
    var movingOffset: Int, //drag의 영향을 받는 블럭들의 offset
    var yPositionList: Set<Float>
)

class ContentViewModel : ViewModel() {
    val firebaseUrl = "https://sparta-f5aee-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val fireRealTimeDatabase =
        Firebase.database(firebaseUrl)
    val contentListState = MutableStateFlow<List<Items>>(emptyList())
    val movingState = MutableStateFlow<dragData>(dragData(0, 0, 0, emptySet()))

    init {
        firebaseInit()
    }

    private fun firebaseInit() {
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")

        // 데이터베이스 리스너 등록 (UI 스레드에서 실행)
        workspaceDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    val formattedDateTime = currentDateTime.format(formatter)
                    val decimalValue = formattedDateTime.toLong()
                    val uniqueKey = get62Code(decimalValue)
                    var count = 1
                    Log.d("Firebase", "Data change detected: $snapshot")

                    val newList = mutableListOf<Items>()

                    if (snapshot.exists()) {
                        snapshot.children.forEach { it ->
                            newList.add(
                                Items(
                                    it.child("id").value.toString(),
                                    it.child("contents").value.toString(),
                                    it.child("typeFlag").value.toString(),
                                    it.child("sequence").getValue(Int::class.java) ?: 0
                                )
                            )
                            count++
                        }
                    } else {
                        addContent(Items("test", "", "title", 1))
                    }
                    newList.forEach {
                        Log.d("DataListenerCheck", it.toString())
                    }
                    contentListState.value = newList

                    sortList()
                    val maxSequence = getMaxSequence()
                    // 마지막 아이템의 contents가 비어있지 않은 경우, 새로운 빈 아이템 추가
                    if (contentListState.value.isNotEmpty() && contentListState.value.last().contents.isNotEmpty()) {
                        newList.add(
                            Items(
                                "test${maxSequence + 1}${uniqueKey}",
                                "",
                                "body",
                                maxSequence + 1
                            )
                        )
                        contentListState.value = newList
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        sortList()
    }

    fun addContent(item: Items) {
        val updatedList = contentListState.value.toMutableList()
        updatedList.add(item)
        contentListState.value = updatedList
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")
        workspaceDB.push().setValue(item).addOnSuccessListener {
            Log.d("Firebase", "Item added successfully")
        }.addOnFailureListener { exception ->
            Log.d("Firebase", "Failed to add item: ${exception.message}")
        }
        sortList()
    }

    fun removeContent(item: Items) {
        val updatedList = contentListState.value.toMutableList()
        updatedList.remove(item)
        contentListState.value = updatedList

        // id 필드가 itemId와 일치하는 데이터를 검색
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")
        workspaceDB.orderByChild("id").equalTo(item.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        // 검색된 데이터 삭제
                        snapshot.ref.removeValue().addOnSuccessListener {
                            Log.d("Firebase", "Item with id ${item.id} removed successfully")
                        }.addOnFailureListener { exception ->
                            Log.d("Firebase", "Failed to remove item: ${exception.message}")
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
                }
            })

        if (item.sequence != contentListState.value.size) {
            val newList = contentListState.value.toMutableList()
            for (i in item.sequence - 1..<contentListState.value.size) {
                newList[i] = newList[i].copy(sequence = newList[i].sequence - 1)
                contentListState.value = newList
            }
        }
        sortList()
        contentListState.value.forEach {
            updateFirebase(it)
        }
    }

    fun typeChange(item: Items, toType: String) {
        Log.d("typeChangeTest", item.toString())
        val updatedList = contentListState.value.toMutableList()
        val index = updatedList.indexOf(item)
        val updateItem = item.copy(typeFlag = toType)
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")

        updatedList[index] = updateItem
        contentListState.value = updatedList

        workspaceDB.orderByChild("id").equalTo(item.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        // 검색된 데이터 업데이트
                        val updates = mapOf("typeFlag" to toType)
                        snapshot.ref.updateChildren(updates).addOnSuccessListener {
                            Log.d("Firebase", "Item with id ${item.id} updated successfully")
                        }.addOnFailureListener { exception ->
                            Log.d("Firebase", "Failed to remove item: ${exception.message}")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }

    fun sequenceChange(item: Items, toSequence: Int) {
        val updatedList = contentListState.value.toMutableList()
        val index = updatedList.indexOf(item)
        val updateItem = item.copy(sequence = toSequence)
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")

        updatedList[index] = updateItem
        contentListState.value = updatedList

        workspaceDB.orderByChild("id").equalTo(item.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        // 검색된 데이터 업데이트
                        val updates = mapOf("sequence" to toSequence)
                        snapshot.ref.updateChildren(updates).addOnSuccessListener {
                            Log.d("Firebase", "Item with id ${item.id} updated successfully")
                        }.addOnFailureListener { exception ->
                            Log.d("Firebase", "Failed to remove item: ${exception.message}")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }

    fun getMaxSequence(): Int {
        return contentListState.value.maxOfOrNull { it.sequence } ?: 0
    }

    fun sortList() {
        contentListState.value = contentListState.value.sortedWith(compareBy { it.sequence })
    }

    private fun updateSequence(updateList: MutableList<Items>) {
        updateList.forEach { item ->
            updateFirebase(item)
        }
    }

    fun updateFirebase(item: Items) {
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")
        val updates = mapOf(
            "id" to item.id,
            "contents" to item.contents,
            "typeFlag" to item.typeFlag,
            "sequence" to item.sequence
        )
        workspaceDB.child(item.id).updateChildren(updates).addOnSuccessListener {
            Log.d("Firebase", "Update Success")
        }.addOnFailureListener {
            Log.d("Firebase", "Update Failed: ${it.message}")
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val newList = contentListState.value.toMutableList()
        val itemState = contentListState.value[fromIndex]

        // drag에 영향 받는 요소들의 sequence 변경
        fromIndex.apply {
            when {
                this == toIndex -> return
                this > toIndex -> {
                    for (i in toIndex until this) {
                        newList[i] = newList[i].copy(sequence = newList[i].sequence + 1)
                        contentListState.value = newList
                        Log.d("movingTest1",newList.toString())
                    }
                }
                this < toIndex -> {
                    for (i in this..toIndex) {
                        newList[i] = newList[i].copy(sequence = newList[i].sequence - 1)
                        contentListState.value = newList
                        Log.d("movingTest2",newList.toString())
                    }
                }
            }
        }

        newList.removeAt(fromIndex)
        newList.add(toIndex, itemState)
        Log.d("movingTest3",newList.toString())
        newList[toIndex].sequence = (toIndex + 1)
        Log.d("movingTest4",newList.toString())
        contentListState.value = newList
        updateSequence(newList)
        sequenceChange(itemState, toIndex+1)
        movingState.value.yPositionList = emptySet()
    }

    //고유 key생성 로직
    fun get62Code(value: Long): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var result = ""
        var number = value
        while (number > 0) {
            result = chars[(number % 62).toInt()] + result
            number /= 62
        }
        return result
    }
}