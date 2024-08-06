package com.lastbullet.yestion

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


data class Message(
    val userId: String,
    val text: String,
    val timestamp: String
)

data class UserInfo(
    val uid: String,
    val password: String
)

class LoginFunction {
    private var database: DatabaseReference = Firebase.database.reference


    fun sendMessage(message: Message) {
        database.child("messages").push().setValue(message)
    }

    fun signUpOnClick(userInfo: UserInfo) {
        database.child("userinfo").push().setValue(userInfo)
    }

}