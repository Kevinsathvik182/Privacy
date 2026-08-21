package com.example.ui.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.HalfNumberCipher
import com.example.data.model.ChatMessage
import com.example.data.model.DeliveryStatus
import com.example.ui.theme.StealthBorder
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthDanger
import com.example.ui.theme.StealthDarkBg
import com.example.ui.theme.StealthDarkCard
import com.example.ui.theme.StealthEmerald
import com.example.ui.theme.StealthTextMuted
import com.example.ui.theme.StealthWarning
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EncryptedChatView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isDecryptionMode by viewModel.isDecryptionMode.collectAsState()
    val selectedPeer by viewModel.selectedPeer.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StealthDarkBg)
    ) {
        // Active Channel Sub-Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StealthEmerald)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = selectedPeer?.name ?: "DeltaMesh-300m (Coded PHY)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "E2EE AES-256-GCM • Distance: ~${selectedPeer?.estimatedDistanceMeters ?: 45}m",
                            fontSize = 10.sp,
                            color = StealthCyan
                        )
                    }
                }

                // Decryption Mode Badge / Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDecryptionMode) StealthEmerald.copy(alpha = 0.2f)
                            else Color(0xFF1E293B)
                        )
                        .border(
                            1.dp,
                            if (isDecryptionMode) StealthEmerald else Color(0xFF334155),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.requestToggleDecryptionMode() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_decryption_mode_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDecryptionMode) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isDecryptionMode) StealthEmerald else StealthTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isDecryptionMode) "DECRYPTED (ON)" else "CIPHER ONLY (OFF)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDecryptionMode) StealthEmerald else StealthTextMuted
                        )
                    }
                }
            }
        }

        // Live Encryption Status Banner
        if (!isDecryptionMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = StealthCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Stealth Half-Number Mode: Messages rendered as numerical cipher tokens (A=0.5, H=4.0, I=4.5...)",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Messages Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(
                    message = message,
                    isDecrypted = isDecryptionMode
                )
            }
        }

        // Quick Preset Message Buttons
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf("HI", "LOCATION DELTA", "MESH IS SECURE", "STATUS REPORT", "AFFIRMATIVE")
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131D36))
                        .border(1.dp, Color(0xFF1E2F54), RoundedCornerShape(16.dp))
                        .clickable { inputText = preset }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("preset_button_$preset")
                ) {
                    Text(
                        text = "$preset → ${HalfNumberCipher.encrypt(preset)}",
                        fontSize = 10.sp,
                        color = StealthCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Real-Time Half-Number Live Preview Bar
        if (inputText.isNotBlank()) {
            val previewCipher = HalfNumberCipher.encrypt(inputText)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A101D)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthCyan.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIPHER STREAM: ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StealthEmerald
                    )
                    Text(
                        text = previewCipher,
                        fontSize = 11.sp,
                        color = StealthCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        text = "Type text (e.g. 'HI' sends '4.0 4.5')...",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthCyan,
                    unfocusedBorderColor = Color(0xFF1E2F54),
                    focusedContainerColor = StealthDarkCard,
                    unfocusedContainerColor = StealthDarkCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF00F0FF))
                        )
                    )
                    .clickable {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    }
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isDecrypted: Boolean
) {
    val isFromMe = message.isFromMe
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Sender info & Range tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isFromMe) "OWNER (ME)" else message.senderName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFromMe) StealthCyan else StealthEmerald
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "• ~${message.distanceEstimateMeters}m Coded PHY • $formattedTime",
                fontSize = 9.sp,
                color = StealthTextMuted
            )
        }

        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .testTag("chat_bubble_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromMe) Color(0xFF132244) else Color(0xFF0E1A2E)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isFromMe) StealthCyan.copy(alpha = 0.5f) else Color(0xFF1E2F54)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isDecrypted) {
                    // DECRYPTED MODE: Show Clear Plaintext + Half-Number Stream
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.rawText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .background(StealthEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DECRYPTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StealthEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Half-Number: ${message.cipherStream}",
                        fontSize = 11.sp,
                        color = StealthCyan,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    // STEALTH / CIPHER ONLY MODE: Show ONLY Half-Numbers ("4.0 4.5")
                    Text(
                        text = message.cipherStream,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StealthCyan,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "[STEALTH STREAM • UNLOCK TO DECRYPT]",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Delivery Status & AES-256 verification indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AES-GCM: ${message.encryptedBlob.take(8)}...",
                        fontSize = 8.sp,
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = StealthEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "CODED_PHY",
                            fontSize = 8.sp,
                            color = StealthEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
