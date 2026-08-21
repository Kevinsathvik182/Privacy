package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.auth.DoubleLockAuthDialog
import com.example.ui.decoy.DecoyMusicPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vault.VaultHomeScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            MyApplicationTheme {
                val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
                val showAuthDialog by viewModel.showAuthDialog.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(
                            targetState = isVaultUnlocked,
                            label = "stealth_screen_transition"
                        ) { unlocked ->
                            if (unlocked) {
                                VaultHomeScreen(viewModel = viewModel)
                            } else {
                                DecoyMusicPlayerScreen(viewModel = viewModel)
                            }
                        }

                        if (showAuthDialog) {
                            DoubleLockAuthDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.dismissAuthDialog() },
                                isForDecryptionOnly = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onStop() {
        super.onStop()
        // Ephemeral Privacy: Auto-restore decoy mode when app is minimized or backgrounded
        viewModel.panicSelfDestruct()
    }
}
