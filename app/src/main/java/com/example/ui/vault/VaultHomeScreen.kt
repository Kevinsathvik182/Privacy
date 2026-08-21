package com.example.ui.vault

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultNavTab
import com.example.ui.auth.DoubleLockAuthDialog
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
fun VaultHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vaultTab by viewModel.vaultTab.collectAsState()
    val isDecryptionMode by viewModel.isDecryptionMode.collectAsState()
    val showDecryptionModal by viewModel.showDecryptionAuthModal.collectAsState()
    val peers by viewModel.peers.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StealthDarkBg,
        topBar = {
            VaultTopAppBar(
                viewModel = viewModel,
                isDecryptionMode = isDecryptionMode
            )
        },
        bottomBar = {
            VaultBottomNavBar(
                currentTab = vaultTab,
                onTabSelect = { viewModel.setVaultTab(it) },
                peerCount = peers.size,
                messageCount = messages.size
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (vaultTab) {
                VaultNavTab.RADAR -> MeshRadarView(viewModel = viewModel)
                VaultNavTab.CHAT -> EncryptedChatView(viewModel = viewModel)
                VaultNavTab.CIPHER_LAB -> HalfNumberCipherLab(viewModel = viewModel)
                VaultNavTab.SECURITY_AUDIT -> SecurityAuditView(viewModel = viewModel)
            }
        }
    }

    // Modal when user clicks Decryption Mode to unlock raw Half-Numbers with Master PIN / Biometric
    if (showDecryptionModal) {
        DoubleLockAuthDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissDecryptionAuthModal() },
            isForDecryptionOnly = true
        )
    }
}

@Composable
private fun VaultTopAppBar(
    viewModel: MainViewModel,
    isDecryptionMode: Boolean
) {
    Surface(
        color = StealthDarkCard,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StealthBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF00F0FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "STEALTH MESH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(StealthEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "300m CODED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = StealthEmerald
                            )
                        }
                    }
                    Text(
                        text = "Zero-Cloud • Air-Gapped E2EE",
                        fontSize = 10.sp,
                        color = StealthCyan
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Decryption Lock Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDecryptionMode) StealthEmerald.copy(alpha = 0.25f)
                            else Color(0xFF1E293B)
                        )
                        .border(
                            1.dp,
                            if (isDecryptionMode) StealthEmerald else Color(0xFF334155),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.requestToggleDecryptionMode() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("appbar_decrypt_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDecryptionMode) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Decrypt Mode",
                        tint = if (isDecryptionMode) StealthEmerald else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Panic Wipe / Decoy Exit Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StealthDanger.copy(alpha = 0.2f))
                        .border(1.dp, StealthDanger.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.panicSelfDestruct() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("panic_exit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Panic Wipe",
                            tint = StealthDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "DECOY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StealthDanger
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultBottomNavBar(
    currentTab: VaultNavTab,
    onTabSelect: (VaultNavTab) -> Unit,
    peerCount: Int,
    messageCount: Int
) {
    NavigationBar(
        containerColor = StealthDarkCard,
        contentColor = Color.White,
        modifier = Modifier.border(1.dp, StealthBorder)
    ) {
        NavigationBarItem(
            selected = currentTab == VaultNavTab.RADAR,
            onClick = { onTabSelect(VaultNavTab.RADAR) },
            icon = {
                BadgedBox(
                    badge = {
                        if (peerCount > 0) {
                            Badge(containerColor = StealthEmerald) { Text(peerCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.Radar, contentDescription = "Radar")
                }
            },
            label = { Text("Mesh Radar", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StealthCyan,
                selectedTextColor = StealthCyan,
                indicatorColor = Color(0xFF1E2F54),
                unselectedIconColor = StealthTextMuted,
                unselectedTextColor = StealthTextMuted
            ),
            modifier = Modifier.testTag("nav_radar")
        )

        NavigationBarItem(
            selected = currentTab == VaultNavTab.CHAT,
            onClick = { onTabSelect(VaultNavTab.CHAT) },
            icon = {
                BadgedBox(
                    badge = {
                        if (messageCount > 0) {
                            Badge(containerColor = StealthCyan) { Text(messageCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat")
                }
            },
            label = { Text("Cipher Chat", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StealthCyan,
                selectedTextColor = StealthCyan,
                indicatorColor = Color(0xFF1E2F54),
                unselectedIconColor = StealthTextMuted,
                unselectedTextColor = StealthTextMuted
            ),
            modifier = Modifier.testTag("nav_chat")
        )

        NavigationBarItem(
            selected = currentTab == VaultNavTab.CIPHER_LAB,
            onClick = { onTabSelect(VaultNavTab.CIPHER_LAB) },
            icon = {
                Icon(Icons.Default.Calculate, contentDescription = "Cipher Lab")
            },
            label = { Text("Half-Number", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StealthCyan,
                selectedTextColor = StealthCyan,
                indicatorColor = Color(0xFF1E2F54),
                unselectedIconColor = StealthTextMuted,
                unselectedTextColor = StealthTextMuted
            ),
            modifier = Modifier.testTag("nav_cipher_lab")
        )

        NavigationBarItem(
            selected = currentTab == VaultNavTab.SECURITY_AUDIT,
            onClick = { onTabSelect(VaultNavTab.SECURITY_AUDIT) },
            icon = {
                Icon(Icons.Default.Security, contentDescription = "Security Audit")
            },
            label = { Text("Security", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StealthCyan,
                selectedTextColor = StealthCyan,
                indicatorColor = Color(0xFF1E2F54),
                unselectedIconColor = StealthTextMuted,
                unselectedTextColor = StealthTextMuted
            ),
            modifier = Modifier.testTag("nav_security")
        )
    }
}
