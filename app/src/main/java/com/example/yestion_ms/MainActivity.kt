package com.example.yestion_ms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.yestion_ms.ui.theme.Yestion_msTheme
import com.google.firebase.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Yestion_msTheme {
                val firebaseDB = Firebase
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(50.dp)
                ) {
                    Box(modifier = Modifier.size(200.dp)) {
                        Button(
                            onClick = { addColleague(firebaseDB) },
                            modifier = Modifier.size(
                                width = 150.dp,
                                height = 50.dp
                            )
                        ) {
                            Text(text = "addColleague")
                        }
                    }
                    Box(modifier = Modifier.size(200.dp)) {
                        Button(
                            onClick = { createGroup(firebaseDB) },
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
                            onClick = { inviteToGroup(firebaseDB) },
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

fun addColleague(firebaseDB: Firebase) {
    fireDB.collection("Company").document(userId)
        .get().addOnSuccessListener { document ->
            if (document.exists()) {
                val passwordDB = document.get("Password")
                if (password == passwordDB) {
                    val intent = Intent(
                        contextAct,
                        MainScreenActivity::class.java
                    )
                    intent.putExtra("companyCode", userId)
                    contextAct?.startActivity(intent)
                }
            }
        }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Yestion_msTheme {

    }
}