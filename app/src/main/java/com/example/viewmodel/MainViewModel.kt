package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.AES256GCM
import com.example.crypto.CipherStep
import com.example.crypto.HalfNumberCipher
import com.example.crypto.KeyExchange
import com.example.crypto.SecurityVaultManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.MessageEntity
import com.example.data.mesh.BleMeshManager
import com.example.data.model.ChatMessage
import com.example.data.model.DecoyTab
import com.example.data.model.DecoyTrack
import com.example.data.model.DeliveryStatus
import com.example.data.model.MeshPeer
import com.example.data.model.StudyDocument
import com.example.data.model.VaultNavTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val messageDao = db.messageDao()
    private val peerDao = db.peerDao()

    val securityVault = SecurityVaultManager(application)
    val meshManager = BleMeshManager(application)

    // Decoy Music Player State
    private val _decoyTab = MutableStateFlow(DecoyTab.MUSIC)
    val decoyTab: StateFlow<DecoyTab> = _decoyTab.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.35f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _activeStudyDoc = MutableStateFlow<StudyDocument?>(null)
    val activeStudyDoc: StateFlow<StudyDocument?> = _activeStudyDoc.asStateFlow()

    val trackList = listOf(
        DecoyTrack("1", "Midnight Lofi Study", "Aura Beats", 184, "Focus Chill"),
        DecoyTrack("2", "Quantum Waveforms", "Synthetica", 215, "Ambient Synth"),
        DecoyTrack("3", "Cyber Odyssey Pt. 2", "Neon Pulse", 240, "Deep Electronic"),
        DecoyTrack("4", "Acoustic Forest Echoes", "Elysium Sound", 195, "Acoustic Nature"),
        DecoyTrack("5", "Neural Chillout Station", "Mindwave Lab", 228, "Lofi Hip Hop")
    )

    val studyDocuments = listOf(
        StudyDocument(
            id = "doc_1",
            title = "CS408: Distributed Systems & Asynchronous Protocols",
            subtitle = "Prof. R. Vance • Semester VIII • Final Lecture Notes",
            category = "Computer Science",
            pageCount = 38,
            contentPreview = "Section 4.2: Fault-Tolerant Consensus in Partitioned Mesh Topologies.\n\nWhen nodes experience intermittent link drops, Byzantine fault tolerance requires a minimum quorum of 2f + 1 nodes. In peer-to-peer topologies using Bluetooth LE Coded PHY, maximum path loss margins increase link reliability across outdoor propagation environments up to 300 meters.\n\nKey Concepts:\n1. Ad-hoc routing tables\n2. Asymmetric key distribution over out-of-band channels\n3. Ephemeral packet lifespans and garbage collection."
        ),
        StudyDocument(
            id = "doc_2",
            title = "PHY302: Quantum Mechanics & Wavepacket Evolution",
            subtitle = "Department of Physics • Chapter 6 Notes",
            category = "Quantum Physics",
            pageCount = 24,
            contentPreview = "Schrödinger Wave Equation for Free Particles in One Dimension.\n\nΨ(x,t) = A exp(i(kx - ωt))\n\nThe probability density function P(x) = |Ψ(x,t)|² remains invariant under global gauge transformations. When examining cryptographic random bit generation from quantum vacuum fluctuations, the entropy rate approaches unity."
        ),
        StudyDocument(
            id = "doc_3",
            title = "MATH240: Linear Algebra & Matrix Decomposition",
            subtitle = "Exam Review • Eigenvalues, SVD & PCA",
            category = "Mathematics",
            pageCount = 42,
            contentPreview = "Singular Value Decomposition (SVD) theorem states that any real m×n matrix M can be factorized as U Σ Vᵀ, where U is an m×m orthogonal matrix, Σ is an m×n rectangular diagonal matrix with non-negative real numbers on the diagonal, and V is an n×n orthogonal matrix."
        )
    )

    // Auth & Navigation States
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authPinInput = MutableStateFlow("")
    val authPinInput: StateFlow<String> = _authPinInput.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _vaultTab = MutableStateFlow(VaultNavTab.CHAT)
    val vaultTab: StateFlow<VaultNavTab> = _vaultTab.asStateFlow()

    private val _isDecryptionMode = MutableStateFlow(false)
    val isDecryptionMode: StateFlow<Boolean> = _isDecryptionMode.asStateFlow()

    private val _showDecryptionAuthModal = MutableStateFlow(false)
    val showDecryptionAuthModal: StateFlow<Boolean> = _showDecryptionAuthModal.asStateFlow()

    // Mesh & Chat State
    val peers: StateFlow<List<MeshPeer>> = meshManager.discoveredPeers

    private val _selectedPeer = MutableStateFlow<MeshPeer?>(null)
    val selectedPeer: StateFlow<MeshPeer?> = _selectedPeer.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = messageDao.getAllMessages().map { list ->
        list.map { entity ->
            ChatMessage(
                id = entity.id,
                peerId = entity.peerId,
                senderName = entity.senderName,
                cipherStream = entity.cipherStream,
                rawText = entity.rawText,
                encryptedBlob = entity.encryptedBlob,
                timestamp = entity.timestamp,
                isFromMe = entity.isFromMe,
                deliveryStatus = try { DeliveryStatus.valueOf(entity.deliveryStatus) } catch (_: Exception) { DeliveryStatus.SENT_CODED_PHY },
                distanceEstimateMeters = entity.distanceEstimateMeters
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cipher Lab Interactive State
    private val _cipherLabInput = MutableStateFlow("HI SECURE MESH")
    val cipherLabInput: StateFlow<String> = _cipherLabInput.asStateFlow()

    private val _cipherLabSteps = MutableStateFlow<List<CipherStep>>(emptyList())
    val cipherLabSteps: StateFlow<List<CipherStep>> = _cipherLabSteps.asStateFlow()

    private var playbackJob: Job? = null

    init {
        updateCipherLab("HI SECURE MESH")
        listenToIncomingMeshMessages()
        startDecoyAudioProgressSimulation()
        seedInitialMessagesIfEmpty()
    }

    private fun seedInitialMessagesIfEmpty() {
        viewModelScope.launch {
            delay(500)
            val currentPeers = meshManager.discoveredPeers.value
            if (currentPeers.isNotEmpty()) {
                val peer = currentPeers.first()
                _selectedPeer.value = peer

                // Initial greeting exchange demonstrating "HI" -> "4.0 4.5"
                val raw1 = "HI"
                val cipher1 = HalfNumberCipher.encrypt(raw1)
                val enc1 = AES256GCM.encrypt(raw1, AES256GCM.deriveKeyFromSecret("KEY_DELTA"))

                messageDao.insertMessage(
                    MessageEntity(
                        id = "msg_init_1",
                        peerId = peer.peerId,
                        senderName = "Me (Owner)",
                        cipherStream = cipher1,
                        rawText = raw1,
                        encryptedBlob = enc1,
                        timestamp = System.currentTimeMillis() - 120000,
                        isFromMe = true,
                        deliveryStatus = DeliveryStatus.VERIFIED_E2EE.name,
                        distanceEstimateMeters = peer.estimatedDistanceMeters
                    )
                )

                val raw2 = "LINK CONFIRMED CODED PHY 300M"
                val cipher2 = HalfNumberCipher.encrypt(raw2)
                val enc2 = AES256GCM.encrypt(raw2, AES256GCM.deriveKeyFromSecret("KEY_DELTA"))

                messageDao.insertMessage(
                    MessageEntity(
                        id = "msg_init_2",
                        peerId = peer.peerId,
                        senderName = peer.name,
                        cipherStream = cipher2,
                        rawText = raw2,
                        encryptedBlob = enc2,
                        timestamp = System.currentTimeMillis() - 60000,
                        isFromMe = false,
                        deliveryStatus = DeliveryStatus.VERIFIED_E2EE.name,
                        distanceEstimateMeters = peer.estimatedDistanceMeters
                    )
                )
            }
        }
    }

    private fun listenToIncomingMeshMessages() {
        viewModelScope.launch {
            meshManager.incomingMessages.collect { msg ->
                messageDao.insertMessage(
                    MessageEntity(
                        id = msg.id,
                        peerId = msg.peerId,
                        senderName = msg.senderName,
                        cipherStream = msg.cipherStream,
                        rawText = msg.rawText,
                        encryptedBlob = msg.encryptedBlob,
                        timestamp = msg.timestamp,
                        isFromMe = msg.isFromMe,
                        deliveryStatus = msg.deliveryStatus.name,
                        distanceEstimateMeters = msg.distanceEstimateMeters
                    )
                )
            }
        }
    }

    private fun startDecoyAudioProgressSimulation() {
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isPlaying.value) {
                    val next = _playbackProgress.value + 0.01f
                    if (next >= 1.0f) {
                        nextTrack()
                    } else {
                        _playbackProgress.value = next
                    }
                }
            }
        }
    }

    // Decoy Player Actions
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % trackList.size
        _playbackProgress.value = 0f
    }

    fun previousTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else trackList.size - 1
        _playbackProgress.value = 0f
    }

    fun selectTrack(index: Int) {
        _currentTrackIndex.value = index
        _playbackProgress.value = 0f
        _isPlaying.value = true
    }

    fun setDecoyTab(tab: DecoyTab) {
        _decoyTab.value = tab
    }

    fun openStudyDoc(doc: StudyDocument?) {
        _activeStudyDoc.value = doc
    }

    // Auth & Vault Actions
    fun triggerHiddenStealthPortal() {
        _authPinInput.value = ""
        _authError.value = null
        _showAuthDialog.value = true
    }

    fun dismissAuthDialog() {
        _showAuthDialog.value = false
        _authPinInput.value = ""
        _authError.value = null
    }

    fun onPinDigitEntered(digit: Char) {
        if (_authPinInput.value.length < 6) {
            val updated = _authPinInput.value + digit
            _authPinInput.value = updated
            if (updated.length == 6) {
                verifyVaultPin(updated)
            }
        }
    }

    fun onPinBackspace() {
        if (_authPinInput.value.isNotEmpty()) {
            _authPinInput.value = _authPinInput.value.dropLast(1)
            _authError.value = null
        }
    }

    fun verifyVaultPin(pin: String) {
        if (securityVault.verifyPin(pin)) {
            _showAuthDialog.value = false
            _isVaultUnlocked.value = true
            _authError.value = null
        } else {
            val failed = securityVault.getFailedAttempts()
            if (securityVault.isLockedOut()) {
                // 3 failed attempts: Auto-close vault and trigger emergency decoy lockdown
                _showAuthDialog.value = false
                _isVaultUnlocked.value = false
                _authError.value = "LOCKOUT: 3 Failed Attempts. Decoy Mode Restored."
                panicSelfDestruct()
            } else {
                _authError.value = "Invalid PIN. Attempt $failed/${SecurityVaultManager.MAX_FAILED_ATTEMPTS}"
                _authPinInput.value = ""
            }
        }
    }

    fun onBiometricSuccess() {
        securityVault.resetFailedAttempts()
        _showAuthDialog.value = false
        _isVaultUnlocked.value = true
    }

    fun onBiometricDecryptionUnlockSuccess() {
        _isDecryptionMode.value = true
        _showDecryptionAuthModal.value = false
    }

    fun setVaultTab(tab: VaultNavTab) {
        _vaultTab.value = tab
    }

    fun requestToggleDecryptionMode() {
        if (_isDecryptionMode.value) {
            _isDecryptionMode.value = false
        } else {
            _showDecryptionAuthModal.value = true
        }
    }

    fun dismissDecryptionAuthModal() {
        _showDecryptionAuthModal.value = false
    }

    fun unlockDecryptionWithPin(pin: String) {
        if (securityVault.verifyPin(pin)) {
            _isDecryptionMode.value = true
            _showDecryptionAuthModal.value = false
        }
    }

    fun selectPeer(peer: MeshPeer) {
        _selectedPeer.value = peer
        meshManager.connectToPeer(peer)
    }

    /**
     * Sends a message with Half-Number Cipher + AES-256-GCM over BLE Mesh Coded PHY.
     */
    fun sendMessage(rawInput: String) {
        if (rawInput.isBlank()) return
        val targetPeer = _selectedPeer.value ?: meshManager.discoveredPeers.value.firstOrNull() ?: return

        // 1. Transform raw text to Half-Number Cipher (e.g. "HI" -> "4.0 4.5")
        val halfCipherStream = HalfNumberCipher.encrypt(rawInput.trim())

        // 2. Encrypt payload using AES-256-GCM
        val sessionKey = AES256GCM.deriveKeyFromSecret(targetPeer.publicKeyFingerprint)
        val encryptedEnvelope = AES256GCM.encrypt(rawInput.trim(), sessionKey)

        val message = ChatMessage(
            peerId = targetPeer.peerId,
            senderName = "Me (Owner)",
            cipherStream = halfCipherStream,
            rawText = rawInput.trim(),
            encryptedBlob = encryptedEnvelope,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            deliveryStatus = DeliveryStatus.SENT_CODED_PHY,
            distanceEstimateMeters = targetPeer.estimatedDistanceMeters
        )

        viewModelScope.launch {
            messageDao.insertMessage(
                MessageEntity(
                    id = message.id,
                    peerId = message.peerId,
                    senderName = message.senderName,
                    cipherStream = message.cipherStream,
                    rawText = message.rawText,
                    encryptedBlob = message.encryptedBlob,
                    timestamp = message.timestamp,
                    isFromMe = message.isFromMe,
                    deliveryStatus = message.deliveryStatus.name,
                    distanceEstimateMeters = message.distanceEstimateMeters
                )
            )

            // Transmit over Bluetooth Long Range Coded PHY
            meshManager.transmitMessage(message)
        }
    }

    fun updateCipherLab(text: String) {
        _cipherLabInput.value = text
        _cipherLabSteps.value = HalfNumberCipher.analyzeTransformation(text)
    }

    /**
     * Emergency Panic Self-Destruct:
     * 1. Wipes all messages from Database & in-memory cache
     * 2. Destroys all encryption keys and session parameters
     * 3. Resets authentication state
     * 4. Immediately reverts screen to Music Player Decoy
     */
    fun panicSelfDestruct() {
        viewModelScope.launch {
            messageDao.clearAllMessages()
            peerDao.clearAllPeers()
            securityVault.panicWipe()
            meshManager.purgeMeshSession()
            _isDecryptionMode.value = false
            _isVaultUnlocked.value = false
            _showAuthDialog.value = false
            _showDecryptionAuthModal.value = false
            _decoyTab.value = DecoyTab.MUSIC
        }
    }
}
