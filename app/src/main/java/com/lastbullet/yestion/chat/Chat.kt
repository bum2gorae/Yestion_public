package com.example.Yestion

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lastbullet.yestion.ui.theme.YestionTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val users: Map<String, Boolean> = emptyMap()
)

data class Message(
    val userName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        setContent {
            YestionTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var nickname by rememberSaveable { mutableStateOf<String?>(null) }

    if (selectedRoom == null) {
        ChatRoomListScreen(onRoomSelected = { selectedRoom = it })
    } else if (nickname == null) {
        NicknameScreen(onNicknameSubmitted = { nickname = it })
    } else {
        ChatScreen(nickname = nickname!!, chatRoom = selectedRoom!!, onBack = { selectedRoom = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(onRoomSelected: (ChatRoom) -> Unit) {
    val chatRooms = remember { mutableStateListOf<ChatRoom>() }
    val db = Firebase.database.reference.child("chatRooms")
    var newRoomName by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    // 채팅방 목록 실시간 업데이트
    DisposableEffect(Unit) {
        val roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRooms.clear()
                for (child in snapshot.children) {
                    val room = child.getValue(ChatRoom::class.java)
                    if (room != null) {
                        chatRooms.add(room)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatApp", "Failed to read chat rooms: ${error.message}")
            }
        }
        db.addValueEventListener(roomListener)

        onDispose {
            db.removeEventListener(roomListener)
        }
    }

    // UI 구성
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Rooms") },
                actions = {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Room")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            TextField(
                                value = newRoomName,
                                onValueChange = { newRoomName = it },
                                label = { Text("New Room Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newRoomName.isNotEmpty()) {
                                        val newRoomRef = db.push()
                                        val newRoom = ChatRoom(newRoomRef.key!!, newRoomName)
                                        newRoomRef.setValue(newRoom)
                                        newRoomName = ""
                                        isMenuExpanded = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create New Room")
                            }
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 채팅방 목록 표시
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatRooms) { room ->
                        ChatRoomItem(room, onRoomSelected)
                    }
                }
            }
        }
    )
}

@Composable
fun ChatRoomItem(room: ChatRoom, onRoomSelected: (ChatRoom) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoomSelected(room) }
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = room.name,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onRoomSelected(room) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Enter Room")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameScreen(onNicknameSubmitted: (String) -> Unit) {
    var nickname by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF64B5F6))
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontSize = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("이름을 적어주세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (nickname.isNotBlank()) {
                    onNicknameSubmitted(nickname)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "확인",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "확인",
                color = Color.White
            )
        }
    }
}

@Composable
fun ChatScreen(nickname: String, chatRoom: ChatRoom, onBack: () -> Unit) {
    val messages = remember { mutableStateListOf<Message>() }
    val db = Firebase.database.reference.child("chatRooms").child(chatRoom.id)
    val messagesRef = db.child("messages")
    val usersRef = db.child("users")
    val storage = Firebase.storage
    var newMessage by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSending by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 사용자가 채팅방에 들어오면 users 목록에 추가
    DisposableEffect(nickname) {
        usersRef.child(nickname).setValue(true)
        onDispose {
            usersRef.child(nickname).removeValue()
        }
    }

    // users 목록 실시간 업데이트
    var users by remember { mutableStateOf<List<String>>(emptyList()) }
    DisposableEffect(Unit) {
        val usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.children.mapNotNull { it.key }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatApp", "Failed to read users: ${error.message}")
            }
        }
        usersRef.addValueEventListener(usersListener)

        onDispose {
            usersRef.removeEventListener(usersListener)
        }
    }

    // 메시지 실시간 업데이트
    DisposableEffect(Unit) {
        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatApp", "Failed to read messages: ${error.message}")
            }
        }
        messagesRef.addValueEventListener(messageListener)

        onDispose {
            messagesRef.removeEventListener(messageListener)
        }
    }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        Log.d("ChatApp", "Image selected: $uri")
    }

    // 이미지 업로드 및 URL 가져오기
    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val storageRef = storage.reference.child("chat_images/${System.currentTimeMillis()}.jpg")
            Log.d("ChatApp", "Uploading image to ${storageRef.path}")
            val uploadTask = storageRef.putFile(uri).await()
            Log.d("ChatApp", "Image uploaded, getting download URL")
            val url = storageRef.downloadUrl.await().toString()
            Log.d("ChatApp", "Image uploaded: $url")
            return url
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ChatApp", "Image upload failed: ${e.message}")
            return null
        }
    }

    // UI 구성
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFA000))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Chat Room: ${chatRoom.name}",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Users in this chat: ${users.joinToString(", ")}",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 72.dp) // 하단 여백 추가
            ) {
                items(messages) { message ->
                    MessageCard(
                        message = message,
                        isCurrentUser = message.userName == nickname
                    )
                }
            }
        }

        // 입력 필드와 전송 버튼을 하단에 고정
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Say something...") }
            )
            Button(
                onClick = {
                    if ((newMessage.text.isNotBlank() || selectedImageUri != null) && !isSending) {
                        isSending = true
                        coroutineScope.launch {
                            val message = Message(
                                userName = nickname,
                                text = newMessage.text,
                                timestamp = System.currentTimeMillis()
                            )
                            if (selectedImageUri != null) {
                                // 이미지 업로드
                                val imageUrl = uploadImage(selectedImageUri!!)
                                if (imageUrl != null) {
                                    val messageWithImage = message.copy(imageUrl = imageUrl)
                                    messagesRef.push().setValue(messageWithImage)
                                    selectedImageUri = null
                                } else {
                                    Log.e("ChatApp", "Failed to upload image")
                                }
                            } else {
                                messagesRef.push().setValue(message)
                            }
                            newMessage = TextFieldValue("") // 입력 필드 초기화
                            isSending = false
                        }
                    }
                },
                enabled = !isSending,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Send")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (!isSending) {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Select Image"
                )
            }
        }
    }
}

@Composable
fun MessageCard(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            // 프로필 이미지 (Placeholder)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser) Color.Blue else Color.LightGray
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = if (isCurrentUser) Color.White else Color.Black
                        )
                    }
                    message.imageUrl?.let { imageUrl ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
            Text(
                text = message.userName,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(if (isCurrentUser) Alignment.End else Alignment.Start)
            )
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
}



