package com.example.ui.vault

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeshPeer
import com.example.data.model.VaultNavTab
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthDarkBg
import com.example.ui.theme.StealthDarkCard
import com.example.ui.theme.StealthEmerald
import com.example.ui.theme.StealthTextMuted
import com.example.ui.theme.StealthWarning
import com.example.viewmodel.MainViewModel

@Composable
fun MeshRadarView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val peers by viewModel.peers.collectAsState()
    val isScanning by viewModel.meshManager.isScanning.collectAsState()
    val isCodedPhy by viewModel.meshManager.isCodedPhyEnabled.collectAsState()
    val selectedPeer by viewModel.selectedPeer.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Radar Animation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radar_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StealthDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2F54))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Long-Range Coded PHY Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Bluetooth 5.0 LE • Max Range ~300m (Open Field)",
                                style = MaterialTheme.typography.bodySmall,
                                color = StealthCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isScanning) viewModel.meshManager.stopScan()
                                else viewModel.meshManager.startScan()
                            },
                            modifier = Modifier.testTag("scan_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = StealthCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Radar Canvas Drawing with sweep line
                    RadarVisualizer(peerCount = peers.size)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Coded PHY Toggle Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B14), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsInputAntenna,
                                contentDescription = null,
                                tint = StealthEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PHY_LE_CODED (S=8 Encoding)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Enhances sensitivity by +12dB for 300m reach",
                                    fontSize = 10.sp,
                                    color = StealthTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isCodedPhy,
                            onCheckedChange = { viewModel.meshManager.setCodedPhyEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StealthCyan,
                                checkedTrackColor = Color(0xFF1E2F54)
                            )
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Mesh Nodes (${peers.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Zero Cloud • Local P2P",
                    fontSize = 11.sp,
                    color = StealthEmerald
                )
            }
        }

        items(peers) { peer ->
            val isSelected = selectedPeer?.peerId == peer.peerId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectPeer(peer)
                    }
                    .testTag("peer_card_${peer.peerId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF162342) else StealthDarkCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) StealthCyan else Color(0xFF1E2F54)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (peer.isConnected) StealthEmerald.copy(alpha = 0.2f)
                                        else Color(0xFF1E293B)
                                    )
                                    .border(
                                        1.dp,
                                        if (peer.isConnected) StealthEmerald else Color(0xFF334155),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (peer.isConnected) StealthEmerald else StealthCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${peer.deviceAddress} • Key: [${peer.publicKeyFingerprint}]",
                                    fontSize = 11.sp,
                                    color = StealthTextMuted
                                )
                            }
                        }

                        if (peer.isConnected) {
                            Box(
                                modifier = Modifier
                                    .background(StealthEmerald.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "CONNECTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StealthEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Row (Distance, RSSI, Coded PHY)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "📍 Distance: ~${peer.estimatedDistanceMeters}m",
                                fontSize = 11.sp,
                                color = StealthCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "📶 RSSI: ${peer.rssi} dBm",
                                fontSize = 11.sp,
                                color = StealthTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.selectPeer(peer)
                                viewModel.setVaultTab(VaultNavTab.CHAT)
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("chat_with_peer_${peer.peerId}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) StealthCyan else Color(0xFF1E2F54),
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Open Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarVisualizer(peerCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(Color(0xFF070B14))
            .border(1.dp, Color(0xFF1E2F54), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2

            // Range rings: 75m, 150m, 225m, 300m
            drawCircle(color = Color(0xFF1E2F54), radius = maxRadius * 0.25f, center = center, style = Stroke(width = 1f))
            drawCircle(color = Color(0xFF1E2F54), radius = maxRadius * 0.50f, center = center, style = Stroke(width = 1f))
            drawCircle(color = Color(0xFF1E2F54), radius = maxRadius * 0.75f, center = center, style = Stroke(width = 1f))
            drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.4f), radius = maxRadius * 0.98f, center = center, style = Stroke(width = 1.5f))

            // Cross hairs
            drawLine(color = Color(0xFF1E2F54), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1f)
            drawLine(color = Color(0xFF1E2F54), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1f)

            // Sweep line
            val rad = Math.toRadians(sweepAngle.toDouble())
            val endX = (center.x + maxRadius * Math.cos(rad)).toFloat()
            val endY = (center.y + maxRadius * Math.sin(rad)).toFloat()
            drawLine(color = Color(0xFF00F0FF), start = center, end = Offset(endX, endY), strokeWidth = 2f)

            // Center host node
            drawCircle(color = Color(0xFF00F0FF), radius = 5f, center = center)

            // Blip 1 (~85m)
            drawCircle(color = Color(0xFF10B981), radius = 6f, center = Offset(center.x + 35f, center.y - 25f))

            // Blip 2 (~240m)
            drawCircle(color = Color(0xFF38BDF8), radius = 5f, center = Offset(center.x - 55f, center.y + 45f))

            // Blip 3 (~18m)
            drawCircle(color = Color(0xFFF59E0B), radius = 5f, center = Offset(center.x + 10f, center.y + 15f))
        }

        Text(
            text = "300m MESH",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00F0FF).copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
        )
    }
}
