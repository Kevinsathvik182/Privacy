package com.example.ui.vault

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.AES256GCM
import com.example.crypto.CipherStep
import com.example.crypto.HalfNumberCipher
import com.example.ui.theme.StealthBorder
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthDarkBg
import com.example.ui.theme.StealthDarkCard
import com.example.ui.theme.StealthEmerald
import com.example.ui.theme.StealthTextMuted
import com.example.ui.theme.StealthWarning
import com.example.viewmodel.MainViewModel

@Composable
fun HalfNumberCipherLab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.cipherLabInput.collectAsState()
    val steps by viewModel.cipherLabSteps.collectAsState()

    var testCipherInput by remember { mutableStateOf("4.0 4.5") }
    val testDecrypted = remember(testCipherInput) {
        HalfNumberCipher.decrypt(testCipherInput)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StealthDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cipher_lab_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StealthCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = StealthCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Half-Number Cipher Mathematics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "f(x) = (Alphabet Index / 2.0) • Invert: f⁻¹(y) = y × 2.0",
                                fontSize = 11.sp,
                                color = StealthCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Every alphabet A..Z (1..26) is halved into decimal numbers. For instance, 'HI' translates to H (8/2 = 4.0) and I (9/2 = 4.5), yielding '4.0 4.5'. To bystanders or sniffers, the broadcast payload looks identical to GPS coordinates or telemetry data.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Live Interactive Encoder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "1. Live Text-to-Cipher Encoder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = input,
                        onValueChange = { viewModel.updateCipherLab(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cipher_lab_input"),
                        label = { Text("Input Text (e.g. 'HI', 'MISSION DELTA')") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StealthCyan,
                            unfocusedBorderColor = StealthBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Cipher Box
                    val currentCipher = HalfNumberCipher.encrypt(input)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B14), RoundedCornerShape(10.dp))
                            .border(1.dp, StealthCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "ENCRYPTED HALF-NUMBER STREAM:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StealthEmerald
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (currentCipher.isNotEmpty()) currentCipher else "[EMPTY]",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StealthCyan,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Mathematical Breakdown Cards
        item {
            Text(
                text = "Character Transformation Breakdown (${steps.size} Tokens)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(steps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1629)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2F54))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (step.originalChar == ' ') "␣" else step.originalChar.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            if (step.isAlpha) {
                                Text(
                                    text = "Alphabet Pos: #${step.charIndex}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Math: ${step.charIndex} ÷ 2 = ${step.halfValue}",
                                    fontSize = 11.sp,
                                    color = StealthTextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = "Special Symbol / Space",
                                    fontSize = 12.sp,
                                    color = StealthTextMuted
                                )
                            }
                        }
                    }

                    // Token badge
                    Box(
                        modifier = Modifier
                            .background(StealthCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, StealthCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = step.token,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StealthCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Reverse Decoder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "2. Reverse Cipher Decoder (Half-Numbers → Text)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testCipherInput,
                        onValueChange = { testCipherInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cipher_lab_reverse_input"),
                        label = { Text("Enter Half-Numbers (e.g. '4.0 4.5')") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StealthEmerald,
                            unfocusedBorderColor = StealthBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B14), RoundedCornerShape(10.dp))
                            .border(1.dp, StealthEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "RECOVERED DECRYPTED PLAINTEXT:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StealthEmerald
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (testDecrypted.isNotEmpty()) testDecrypted else "[NO RESULT]",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Reference Matrix Table A-Z
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "3. Complete Cipher Table Reference (A..Z)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val alphabet = ('A'..'Z').toList()
                    val chunked = alphabet.chunked(6)

                    chunked.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row.forEach { ch ->
                                val idx = ch - 'A' + 1
                                val half = idx / 2.0
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(48.dp)
                                ) {
                                    Text(
                                        text = ch.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "%.1f".format(java.util.Locale.US, half),
                                        fontSize = 10.sp,
                                        color = StealthCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
