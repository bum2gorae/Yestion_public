package com.example.yestion_ms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.yestion_ms.ui.theme.Yestion_msTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SocialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Yestion_msTheme {
                val firebaseDB = Firebase.firestore
                var colleagueSearch by remember {
                    mutableStateOf("")
                }
                var colleagueName by remember {
                    mutableStateOf("")
                }
                var colleagueKey by remember {
                    mutableStateOf("")
                }

                val userKey = "testUserKey"
                val groupName = "testGroup"
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            TextField(value = colleagueSearch, onValueChange = { it ->
                                colleagueSearch = it
                            })
                            Box(modifier = Modifier.size(200.dp)) {
                                Button(
                                    onClick = {
                                        addColleague(
                                            firebaseDB,
                                            userKey,
                                            colleagueKey,
                                            colleagueName
                                        )
                                    },
                                    modifier = Modifier.size(
                                        width = 150.dp,
                                        height = 50.dp
                                    )
                                ) {
                                    Text(text = "addColleague")
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            firebaseDB.collection("Users")
                                .whereEqualTo("Name", colleagueSearch)
                                .get()
                                .addOnSuccessListener { result ->
                                    result.forEach {

                                    }
                                }
                        }
                    }
                    Box(modifier = Modifier.size(200.dp)) {
                        Button(
                            onClick = {
//                                createGroup(firebaseDB)
                                      },
                            modifier = Modifier.size(
                                width = 150.dp,
                                height = 50.dp
                            )
                        ) {
                            Text(text = "createGroup")
                        }
                    }
                    Box(modifier = Modifier.size(200.dp)) {
                        Button(
                            onClick = {
//                                inviteToGroup(irebaseDB)
                                      },
                            modifier = Modifier.size(
                                width = 150.dp,
                                height = 50.dp
                            )
                        ) {
                            Text(text = "inviteToGroup")
                        }
                    }
                }
            }
        }
    }
}

fun addColleague(
    firebaseDB: FirebaseFirestore,
    userKey: String,
    colleagueKey: String,
    colleagueName: String
) {
    val colleagueMap = mapOf("Name" to colleagueName)
    firebaseDB.collection("Users").document(userKey)
        .collection("Colleague").document(colleagueKey)
        .set(colleagueMap)
        .addOnSuccessListener {
            /*TODO*/
        }
}

fun createGroup(
    firebaseDB: FirebaseFirestore,
    userKey: String,
    groupName: String
) {
    //62진법코드를 userKey에 붙여서 그룹고유번호 부여
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val formattedDateTime = currentDateTime.format(formatter)
    val decimalValue = formattedDateTime.toLong()
    val uniqueGroupKey = get62Code(decimalValue) + userKey
    val groupMap = mapOf("Name" to groupName)
    val groupLeaderMap = mapOf("Leader" to userKey)
    //Firestore Users컬렉션에 추가
    firebaseDB.collection("Users").document(userKey)
        .collection("Group").document(uniqueGroupKey)
        .set(groupMap)
        .addOnSuccessListener {
            /*TODO*/
        }
    //Firestore Groups컬렉션에 추가
    firebaseDB.collection("Groups").document(uniqueGroupKey)
        .set(groupMap)
    firebaseDB.collection("Groups").document(uniqueGroupKey)
        .set(groupLeaderMap)
        .addOnSuccessListener {
            /*TODO*/
        }
}

fun inviteToGroup(
    firebaseDB: FirebaseFirestore,
    groupKey: String,
    memberKey: String,
    memberName: String
) {
    val memberMap = mapOf("Name" to memberName)
    firebaseDB.collection("Groups").document(groupKey)
        .collection("Members").document(memberKey)
        .set(memberMap)
        .addOnSuccessListener {
            /*TODO*/
        }
}

//yyyymmddhhmmss를 62진법으로 바꿈a
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Yestion_msTheme {

    }
}