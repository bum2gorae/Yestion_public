package com.lastbullet.yestion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter


class MainScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreenComponent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenComponent() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Yestion")
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: 메뉴 아이콘 클릭 시 동작 */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 프로필 아이콘 클릭 시 동작 */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEDE7F6)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: 추가 버튼 클릭 시 동작 */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFFEDE7F6)
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* TODO: 홈 버튼 클릭 시 동작 */ }) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
                IconButton(onClick = { /* TODO: 검색 버튼 클릭 시 동작 */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                Spacer(Modifier.weight(1f))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1C4E9))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                ){
                    Text(
                        text = "안드로이드",
                        fontSize = 30.sp,
                        modifier = Modifier.
                        align(Alignment.CenterStart)
                            .padding(start = 20.dp)

                    )
                }
            }
            // 아바타 섹션


            Spacer(modifier = Modifier.height(16.dp))

            // 가로 카드 섹션
            FeatureCard(title = "노트", description = "")
            FeatureCard(title = "채팅방", description = "Subhead")
        }
    }
}

@Composable
fun FeatureCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEDE7F6)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7E57C2))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(title, fontSize = 18.sp)
                if (description.isNotEmpty()) {
                    Text(description, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 추가 아이콘 버튼 섹션
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD1C4E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewFeatureCard() {
    FeatureCard(title = "노트", description = "This is a subhead")
}


