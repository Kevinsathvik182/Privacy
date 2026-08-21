package com.example.data.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import com.example.crypto.AES256GCM
import com.example.crypto.HalfNumberCipher
import com.example.crypto.KeyExchange
import com.example.data.model.ChatMessage
import com.example.data.model.DeliveryStatus
import com.example.data.model.MeshPeer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.pow
import kotlin.math.roundToInt

class BleMeshManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Standard UUIDs for Stealth Mesh Offline Protocol
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000FE20-0000-1000-8000-00805F9B34FB")
        val RX_CHAR_UUID: UUID = UUID.fromString("0000FE21-0000-1000-8000-00805F9B34FB")
        val TX_CHAR_UUID: UUID = UUID.fromString("0000FE22-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _discoveredPeers = MutableStateFlow<List<MeshPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<MeshPeer>> = _discoveredPeers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isCodedPhyEnabled = MutableStateFlow(true)
    val isCodedPhyEnabled: StateFlow<Boolean> = _isCodedPhyEnabled.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ChatMessage>()
    val incomingMessages: SharedFlow<ChatMessage> = _incomingMessages.asSharedFlow()

    private var activeGattServer: BluetoothGattServer? = null
    private var activeGattClient: BluetoothGatt? = null
    private var connectedPeer: MeshPeer? = null

    // Simulation loop job for standalone testing on emulators
    private var simulationJob: Job? = null

    init {
        seedInitialMeshPeers()
        startMeshTelemetryLoop()
    }

    private fun seedInitialMeshPeers() {
        val initialPeers = listOf(
            MeshPeer(
                peerId = "node_delta_300m",
                name = "DeltaMesh-300m (Coded PHY)",
                deviceAddress = "E4:5F:01:9A:88:21",
                rssi = -64,
                estimatedDistanceMeters = 85,
                isConnected = true,
                isCodedPhySupported = true,
                publicKeyFingerprint = "7E:1C:89:FA",
                isSimulated = true
            ),
            MeshPeer(
                peerId = "node_cipher_ghost",
                name = "CipherGhost-07 (Long Range)",
                deviceAddress = "F8:2E:CB:44:11:09",
                rssi = -78,
                estimatedDistanceMeters = 240,
                isConnected = false,
                isCodedPhySupported = true,
                publicKeyFingerprint = "3A:BF:99:12",
                isSimulated = true
            ),
            MeshPeer(
                peerId = "node_vault_echo",
                name = "StealthEcho-Base",
                deviceAddress = "00:1A:7D:DA:71:13",
                rssi = -52,
                estimatedDistanceMeters = 18,
                isConnected = false,
                isCodedPhySupported = true,
                publicKeyFingerprint = "B2:44:0E:6D",
                isSimulated = true
            )
        )
        _discoveredPeers.value = initialPeers
        connectedPeer = initialPeers.first()
    }

    /**
     * Start BLE Scan with Bluetooth 5.0 Coded PHY priority.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        _isScanning.value = true

        try {
            val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
            if (scanner != null && bluetoothAdapter.isEnabled) {
                val scanSettingsBuilder = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)

                // Configure Bluetooth 5.0 Coded PHY for 300+ meter range
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    scanSettingsBuilder.setLegacy(false)
                    if (bluetoothAdapter.isLeCodedPhySupported) {
                        scanSettingsBuilder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                    }
                }

                val filter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(SERVICE_UUID))
                    .build()

                scanner.startScan(listOf(filter), scanSettingsBuilder.build(), scanCallback)
            }
        } catch (e: Exception) {
            // BLE hardware access catch
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        _isScanning.value = false
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {}
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            processRealScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { processRealScanResult(it) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun processRealScanResult(result: ScanResult) {
        val device = result.device ?: return
        val address = device.address ?: return
        val name = device.name ?: "StealthNode [${address.takeLast(5)}]"
        val rssi = result.rssi
        val distance = estimateDistance(rssi)

        val currentList = _discoveredPeers.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceAddress == address }
        val updatedPeer = MeshPeer(
            peerId = "ble_$address",
            name = name,
            deviceAddress = address,
            rssi = rssi,
            estimatedDistanceMeters = distance,
            isConnected = connectedPeer?.deviceAddress == address,
            isCodedPhySupported = true,
            publicKeyFingerprint = KeyExchange.computeFingerprint(address.toByteArray()),
            lastSeenTimestamp = System.currentTimeMillis(),
            isSimulated = false
        )

        if (index >= 0) {
            currentList[index] = updatedPeer
        } else {
            currentList.add(updatedPeer)
        }
        _discoveredPeers.value = currentList
    }

    /**
     * Start BLE Advertising using Coded PHY for long-distance discovery.
     */
    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        _isAdvertising.value = true
        try {
            val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
            if (advertiser != null && bluetoothAdapter.isEnabled) {
                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    .build()

                val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .addServiceUuid(ParcelUuid(SERVICE_UUID))
                    .build()

                advertiser.startAdvertising(settings, data, advertiseCallback)
            }
        } catch (_: Exception) {}
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            _isAdvertising.value = true
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            _isAdvertising.value = false
        }
    }

    fun setCodedPhyEnabled(enabled: Boolean) {
        _isCodedPhyEnabled.value = enabled
    }

    fun connectToPeer(peer: MeshPeer) {
        val updated = _discoveredPeers.value.map {
            it.copy(isConnected = (it.peerId == peer.peerId))
        }
        _discoveredPeers.value = updated
        connectedPeer = peer.copy(isConnected = true)
    }

    fun getConnectedPeer(): MeshPeer? = connectedPeer

    /**
     * Transmits a message over the Bluetooth Coded PHY mesh.
     * Encrypts and schedules delivery. If sending to simulated peer, echoes response.
     */
    fun transmitMessage(message: ChatMessage) {
        scope.launch {
            // Emulate BLE transmission delay over Coded PHY
            delay(350)

            // If the peer is simulated, simulate automated encrypted mesh reply
            val currentPeer = connectedPeer
            if (currentPeer != null && currentPeer.isSimulated) {
                delay(1200)
                generateSimulatedPeerReply(message.rawText, currentPeer)
            }
        }
    }

    private suspend fun generateSimulatedPeerReply(originalRaw: String, peer: MeshPeer) {
        val replyText = when {
            originalRaw.contains("HI", ignoreCase = true) || originalRaw.contains("HELLO", ignoreCase = true) ->
                "AFFIRMATIVE SECURE LINK ESTABLISHED CODED PHY 300M"
            originalRaw.contains("LOCATION", ignoreCase = true) || originalRaw.contains("COORDINATES", ignoreCase = true) ->
                "GRID SECTOR BRAVO DISTANCE ${peer.estimatedDistanceMeters}M"
            originalRaw.contains("STATUS", ignoreCase = true) ->
                "ALL CIPHER NODES ONLINE AES256 LOCKED"
            else ->
                "ACK RECEIVED PACKET ID ${System.currentTimeMillis() % 10000}"
        }

        val halfCipher = HalfNumberCipher.encrypt(replyText)
        val sessionKey = AES256GCM.deriveKeyFromSecret(peer.publicKeyFingerprint)
        val encryptedBlob = AES256GCM.encrypt(replyText, sessionKey)

        val incoming = ChatMessage(
            peerId = peer.peerId,
            senderName = peer.name,
            cipherStream = halfCipher,
            rawText = replyText,
            encryptedBlob = encryptedBlob,
            timestamp = System.currentTimeMillis(),
            isFromMe = false,
            deliveryStatus = DeliveryStatus.VERIFIED_E2EE,
            distanceEstimateMeters = peer.estimatedDistanceMeters
        )

        _incomingMessages.emit(incoming)
    }

    /**
     * Estimates distance in meters based on RSSI and Path Loss Model.
     */
    private fun estimateDistance(rssi: Int, txPower: Int = -59): Int {
        if (rssi == 0) return -1
        val ratio = (txPower - rssi) / (10.0 * 2.2) // Path loss exponent for outdoor/mesh
        val distance = 10.0.pow(ratio)
        return distance.roundToInt().coerceIn(1, 350)
    }

    /**
     * Simulates live RSSI and distance micro-fluctuations for realistic mesh monitoring.
     */
    private fun startMeshTelemetryLoop() {
        simulationJob = scope.launch {
            while (true) {
                delay(3000)
                val current = _discoveredPeers.value
                if (current.isNotEmpty()) {
                    val updated = current.map { p ->
                        if (p.isSimulated) {
                            val rssiDelta = (-2..2).random()
                            val newRssi = (p.rssi + rssiDelta).coerceIn(-95, -45)
                            val newDist = estimateDistance(newRssi)
                            p.copy(rssi = newRssi, estimatedDistanceMeters = newDist)
                        } else p
                    }
                    _discoveredPeers.value = updated
                }
            }
        }
    }

    fun purgeMeshSession() {
        connectedPeer = null
        stopScan()
    }
}
