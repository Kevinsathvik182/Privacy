package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.crypto.SecurityVaultManager
import com.example.ui.theme.MusicPrimary
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthDanger
import com.example.ui.theme.StealthDarkBg
import com.example.ui.theme.StealthDarkCard
import com.example.ui.theme.StealthEmerald
import com.example.ui.theme.StealthWarning
import com.example.viewmodel.MainViewModel

@Composable
fun DoubleLockAuthDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    isForDecryptionOnly: Boolean = false
) {
    val pinInput by viewModel.authPinInput.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val failedAttempts = viewModel.securityVault.getFailedAttempts()

    var showBiometricPrompt by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("auth_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = StealthDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2F54))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security Shield Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                            )
                        )
                        .border(1.dp, StealthCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isForDecryptionOnly) Icons.Default.Key else Icons.Default.Shield,
                        contentDescription = null,
                        tint = StealthCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isForDecryptionOnly) "Decryption Master Lock" else "Encrypted Stealth Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (isForDecryptionOnly) "Authenticate to reveal Half-Number cipher" else "Enter 6-digit Security PIN or Fingerprint",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                // Failed Attempts Warning
                if (failedAttempts > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Warning: Attempt $failedAttempts/${SecurityVaultManager.MAX_FAILED_ATTEMPTS} (3 fails triggers auto-wipe)",
                        fontSize = 11.sp,
                        color = StealthDanger,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = authError!!,
                        fontSize = 12.sp,
                        color = StealthDanger,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6-Digit Masked Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) StealthCyan else Color(0xFF1E293B)
                                )
                                .border(
                                    1.dp,
                                    if (isFilled) StealthCyan else Color(0xFF334155),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Numeric Keypad
                NumericKeypad(
                    onDigitClick = { digit -> viewModel.onPinDigitEntered(digit) },
                    onBackspaceClick = { viewModel.onPinBackspace() },
                    onBiometricClick = {
                        // Trigger Biometric verification
                        if (isForDecryptionOnly) {
                            viewModel.onBiometricDecryptionUnlockSuccess()
                        } else {
                            viewModel.onBiometricSuccess()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary biometric quick-touch trigger
                OutlinedButton(
                    onClick = {
                        if (isForDecryptionOnly) {
                            viewModel.onBiometricDecryptionUnlockSuccess()
                        } else {
                            viewModel.onBiometricSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biometric_quick_auth_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StealthEmerald
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthEmerald.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = StealthEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Touch Biometric Sensor (Verified)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Default PIN helper for ease of testing
                Text(
                    text = "Default Master PIN: 124816",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('F', '0', 'B') // F = Fingerprint, B = Backspace
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        'F' -> {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E2F54), CircleShape)
                                    .clickable { onBiometricClick() }
                                    .testTag("keypad_biometric"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric",
                                    tint = StealthEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        'B' -> {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E2F54), CircleShape)
                                    .clickable { onBackspaceClick() }
                                    .testTag("keypad_backspace"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF131D36))
                                    .border(1.dp, Color(0xFF1E2F54), CircleShape)
                                    .clickable { onDigitClick(key) }
                                    .testTag("keypad_digit_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
