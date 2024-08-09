package com.lastbullet.yestion

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


data class Message(
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: String
)

data class UserInfo(
    val userId: String,
    val password: String
)
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val users: Map<String, Boolean> = emptyMap()
)

//data class Message(
//    val userName: String = "",
//    val text: String = "",
//    val imageUrl: String? = null,
//    val timestamp: Long = 0
//)
class LoginFunction {
    private var database: DatabaseReference = Firebase.database.reference


    fun sendMessage(message: Message) {
        database.child("messages").push().setValue(message)
    }

    fun signUpOnClick(userInfo: UserInfo) {
        database.child("userinfo").push().setValue(userInfo)
    }
}