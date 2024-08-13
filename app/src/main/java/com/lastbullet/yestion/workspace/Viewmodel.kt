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

data class OnMovingData(
    //drag용 data class
    var onMoveFromIndex : Int, //drag시작 index
    var onMoveToIndex : Int, //drag목표 index
    var movingOffset : Int, //drag의 영향을 받는 블럭들의 offset
)

class ContentViewModel : ViewModel() {
    val firebaseUrl = "https://sparta-f5aee-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val contentList = mutableStateListOf<Items>()
    val contentListState = MutableStateFlow<List<Items>>(emptyList())
    val movingState = MutableStateFlow<OnMovingData>(OnMovingData(0,0,0))

    init {
        firebaseInit()
    }

    fun addContent(item: Items) {
        contentList.add(item)
        val updatedList = contentListState.value.toMutableList()
        updatedList.add(item)
        contentListState.value =updatedList
    }

    fun removeContent(item: Items) {
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")
        contentList.remove(item)

        val updatedList = contentListState.value.toMutableList()
        updatedList.remove(item)
        contentListState.value =updatedList

        contentList.forEach {
            Log.d("remove test",it.toString())
        }
        // id 필드가 itemId와 일치하는 데이터를 검색
        workspaceDB.orderByChild("id").equalTo(item.id).addListenerForSingleValueEvent(object : ValueEventListener {
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
        if (item.sequence!=contentListState.value.size) {
            val newList = contentListState.value.toMutableList()
            for (i in item.sequence-1..<contentListState.value.size) {
                newList[i] = newList[i].copy(sequence = newList[i].sequence - 1)
                contentListState.value = newList
            }
        }
        if (item.sequence!=contentList.size) {
            for (i in item.sequence-1..<contentList.size) {
                contentList[i].sequence -= 1
            }
        }
        sortList()
        contentListState.value.forEach {
            updateFirebase(
                it.id,
                it.contents,
                it.typeFlag,
                it.sequence
            )
        }
//        contentList.forEach { it ->
//            updateFirebase(
//                it.id,
//                it.contents,
//                it.typeFlag,
//                it.sequence
//            )
//        }
    }


    fun typeChange(item: Items, toType: String) {
        item.typeFlag = toType
        updateSequence()
    }

    fun getMaxSequence(): Int {
        return contentListState.value.maxOfOrNull { it.sequence } ?: 0
//        return contentList.maxOfOrNull { it.sequence } ?: 0
    }

    private fun firebaseInit() {
        // FireBase RealTime Database 초기화
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
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
                    contentList.clear()
                    var count = 1
                    Log.d("Firebase", "Data change detected: $snapshot")

                    if (snapshot.exists()) {
                        snapshot.children.forEach { it ->
                            contentList.add(
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

                    sortList()
                    val maxSequence = getMaxSequence()
                    // 마지막 아이템의 contents가 비어있지 않은 경우, 새로운 빈 아이템 추가
                    if (contentList.isNotEmpty() && contentList.last().contents.isNotEmpty()) {
                        contentList.add(
                            Items(
                                "test${maxSequence+1}${uniqueKey}",
                                "",
                                "body",
                                maxSequence+1
                            )
                        )
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun sortList() {
        val sortedList = contentList.sortedWith(compareBy { it.sequence })
        contentList.clear()
        contentList.addAll(sortedList)
    }

    fun updateFirebase(
        id: String,
        contents: String,
        typeFlag: String,
        sequence: Int
    ) {
        val fireRealTimeDatabase =
            Firebase.database(firebaseUrl)
        val workspaceDB = fireRealTimeDatabase.getReference("workspace")
        val updates = mapOf(
            "id" to id,
            "contents" to contents,
            "typeFlag" to typeFlag,
            "sequence" to sequence
        )
        workspaceDB.child(id).updateChildren(updates).addOnSuccessListener {
            Log.d("Firebase", "Update Success")
        }.addOnFailureListener {
            Log.d("Firebase", "Update Failed: ${it.message}")
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        when {
            fromIndex==toIndex -> return
            fromIndex>toIndex -> fromIndex.let { highIndex ->
                for (i in toIndex..highIndex) {
                    contentList[i].sequence += 1
                }
            }
            fromIndex<toIndex -> fromIndex.let { lowIndex ->
                for (i in lowIndex..toIndex) {
                    contentList[i].sequence -= 1
                }
            }
        }

        val item = contentList[fromIndex]
        contentList.removeAt(fromIndex)
        contentList.add(toIndex, item)
        contentList[toIndex].sequence = toIndex+1
        sortList()
        updateSequence()
    }

    private fun updateSequence() {
        contentList.forEach { item ->
            updateFirebase(item.id, item.contents, item.typeFlag, item.sequence)
        }
    }

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