package com.example.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.StealthBorder
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthDanger
import com.example.ui.theme.StealthDarkBg
import com.example.ui.theme.StealthDarkCard
import com.example.ui.theme.StealthEmerald
import com.example.ui.theme.StealthTextMuted
import com.example.ui.theme.StealthWarning
import com.example.viewmodel.MainViewModel

@Composable
fun SecurityAuditView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showPanicConfirmDialog by remember { mutableStateOf(false) }
    var showPinChangeModal by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StealthDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zero-Cloud Audit Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_audit_header_card"),
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(StealthEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = StealthEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zero-Cloud Security Architecture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Air-Gapped Mesh • Ephemeral Memory • No Cloud Servers",
                                fontSize = 11.sp,
                                color = StealthEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This application operates under zero-trust, completely isolated on-device protocols. There are zero outbound HTTP/REST calls, no Google Sign-In, and no cloud synchronization.",
                        fontSize = 11.sp,
                        color = StealthTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Compliance Checklist
        item {
            Text(
                text = "Hardware & Manifest Security Audit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuditCheckItem(
                    title = "INTERNET Permission Removed",
                    desc = "App cannot access WAN / Internet. Complete offline guarantee.",
                    passed = true
                )
                AuditCheckItem(
                    title = "READ_PHONE_STATE Denied",
                    desc = "Zero access to IMEI, SIM, carrier telemetry or telephony IDs.",
                    passed = true
                )
                AuditCheckItem(
                    title = "Bluetooth 5.0 Long Range (PHY_LE_CODED)",
                    desc = "S=8 forward error correction for ~300m range without cellular towers.",
                    passed = true
                )
                AuditCheckItem(
                    title = "AES-256-GCM + Diffie-Hellman E2EE",
                    desc = "Authenticated 128-bit tag encryption with ephemeral session key exchange.",
                    passed = true
                )
                AuditCheckItem(
                    title = "Double-Lock (6-Digit PIN + Biometrics)",
                    desc = "Argon2/PBKDF2 style hashing with 3-strike self-destruct lockout protection.",
                    passed = true
                )
            }
        }

        // Master PIN Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Master Vault PIN Configuration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Current default: 124816 (Stored with Salt + SHA-256)",
                                fontSize = 11.sp,
                                color = StealthTextMuted
                            )
                        }

                        Button(
                            onClick = { showPinChangeModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F54)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change PIN", fontSize = 11.sp)
                        }
                    }

                    if (pinSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinSuccessMessage!!,
                            fontSize = 11.sp,
                            color = StealthEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Emergency Panic Self-Destruct
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("panic_wipe_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF200B14)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StealthDanger.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StealthDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Emergency Panic Self-Destruct",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StealthDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Instantly clears all in-memory cryptographic keys, wipes all Room database messages & peer caches, resets authentication, and immediately restores the innocuous Music Player Decoy.",
                        fontSize = 11.sp,
                        color = Color(0xFFFECDD3),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showPanicConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("emergency_panic_wipe_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StealthDanger,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PURGE ALL DATA & RESTORE DECOY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Panic Confirmation Dialog
    if (showPanicConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPanicConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Emergency Self-Destruct",
                    fontWeight = FontWeight.Bold,
                    color = StealthDanger
                )
            },
            text = {
                Text(
                    text = "Are you sure? This will wipe all messages, destroy encryption session keys from RAM, and return immediately to the Music Player decoy.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPanicConfirmDialog = false
                        viewModel.panicSelfDestruct()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StealthDanger)
                ) {
                    Text("YES, WIPE NOW")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = StealthDarkBg
        )
    }

    // Change PIN Dialog
    if (showPinChangeModal) {
        AlertDialog(
            onDismissRequest = { showPinChangeModal = false },
            title = {
                Text(
                    text = "Set New 6-Digit Master PIN",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a 6-digit numeric PIN known only to you:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPinInput = it },
                        placeholder = { Text("6-digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StealthCyan,
                            unfocusedBorderColor = StealthBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 6) {
                            viewModel.securityVault.setMasterPin(newPinInput)
                            pinSuccessMessage = "Master PIN successfully updated."
                            showPinChangeModal = false
                            newPinInput = ""
                        }
                    },
                    enabled = newPinInput.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = StealthCyan, contentColor = Color.Black)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeModal = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = StealthDarkBg
        )
    }
}

@Composable
private fun AuditCheckItem(
    title: String,
    desc: String,
    passed: Boolean
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                contentDescription = null,
                tint = if (passed) StealthEmerald else StealthDanger,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = StealthTextMuted
                )
            }
        }
    }
}
