package com.example.ui.decoy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecoyTab
import com.example.data.model.StudyDocument
import com.example.ui.theme.MusicAccent
import com.example.ui.theme.MusicBackground
import com.example.ui.theme.MusicCard
import com.example.ui.theme.MusicPrimary
import com.example.ui.theme.MusicSecondary
import com.example.ui.theme.MusicSurface
import com.example.ui.theme.MusicTertiary
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoyMusicPlayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val decoyTab by viewModel.decoyTab.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val trackIndex by viewModel.currentTrackIndex.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val activeDoc by viewModel.activeStudyDoc.collectAsState()

    val currentTrack = viewModel.trackList[trackIndex]
    var vinylTapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF090D16),
                        Color(0xFF030712)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // App Bar with Subtle Hidden Portal Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MusicPrimary, MusicSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Pulse Audio",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pulse Audio Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Offline Player • 320kbps Lossless",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // SECRET TRIGGER: Top-Right Discreet Equalizer Indicator
                // Can be tapped to open the double-lock PIN prompt
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            viewModel.triggerHiddenStealthPortal()
                        }
                        .testTag("stealth_secret_trigger"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isPlaying) 18.dp else 8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MusicTertiary.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isPlaying) 24.dp else 14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MusicPrimary.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isPlaying) 12.dp else 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MusicSecondary.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isPlaying) 20.dp else 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MusicTertiary.copy(alpha = 0.8f))
                        )
                    }
                }
            }

            // Tab Navigation (Music Tracks / Study PDFs / Equalizer)
            TabRow(
                selectedTabIndex = decoyTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                divider = {},
                indicator = { tabPositions ->
                    if (decoyTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[decoyTab.ordinal]),
                            color = MusicPrimary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                Tab(
                    selected = decoyTab == DecoyTab.MUSIC,
                    onClick = { viewModel.setDecoyTab(DecoyTab.MUSIC) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Music", fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = decoyTab == DecoyTab.STUDY_PDFS,
                    onClick = { viewModel.setDecoyTab(DecoyTab.STUDY_PDFS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Study Docs", fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = decoyTab == DecoyTab.EQUALIZER,
                    onClick = { viewModel.setDecoyTab(DecoyTab.EQUALIZER) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Equalizer", fontSize = 13.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (decoyTab) {
                DecoyTab.MUSIC -> {
                    MusicPlayerContent(
                        viewModel = viewModel,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        progress = progress,
                        trackIndex = trackIndex,
                        onVinylTripleTap = {
                            vinylTapCount++
                            if (vinylTapCount >= 3) {
                                vinylTapCount = 0
                                viewModel.triggerHiddenStealthPortal()
                            }
                        }
                    )
                }
                DecoyTab.STUDY_PDFS -> {
                    StudyDocumentsContent(
                        viewModel = viewModel
                    )
                }
                DecoyTab.EQUALIZER -> {
                    EqualizerContent()
                }
            }
        }

        // Active Study PDF Modal Reader
        if (activeDoc != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.openStudyDoc(null) },
                containerColor = Color(0xFF0F172A),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                StudyDocReaderSheet(
                    doc = activeDoc!!,
                    onClose = { viewModel.openStudyDoc(null) }
                )
            }
        }
    }
}

@Composable
private fun MusicPlayerContent(
    viewModel: MainViewModel,
    currentTrack: com.example.data.model.DecoyTrack,
    isPlaying: Boolean,
    progress: Float,
    trackIndex: Int,
    onVinylTripleTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Now Playing Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("now_playing_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MusicCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rotating Vinyl Artwork
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF4338CA),
                                        Color(0xFF818CF8),
                                        Color(0xFF38BDF8),
                                        Color(0xFF1E1B4B)
                                    )
                                )
                            )
                            .clickable { onVinylTripleTap() }
                            .rotate(if (isPlaying) rotation else 0f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vinyl grooves
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MusicPrimary, MusicSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${currentTrack.artist} • ${currentTrack.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Slider
                    Slider(
                        value = progress,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("track_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MusicTertiary,
                            activeTrackColor = MusicPrimary,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentSecs = (progress * currentTrack.durationSeconds).toInt()
                        Text(
                            text = "%d:%02d".format(currentSecs / 60, currentSecs % 60),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "%d:%02d".format(currentTrack.durationSeconds / 60, currentTrack.durationSeconds % 60),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color(0xFF94A3B8))
                        }
                        IconButton(onClick = { viewModel.previousTrack() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MusicPrimary, MusicAccent)
                                    )
                                )
                                .clickable { viewModel.togglePlayPause() }
                                .testTag("play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.nextTrack() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Audio Playlist & Focus Tracks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        itemsIndexed(viewModel.trackList) { index, track ->
            val isCurrent = index == trackIndex
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTrack(index) }
                    .testTag("track_item_$index"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrent) MusicPrimary else Color(0xFF334155)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCurrent && isPlaying) Icons.Default.Equalizer else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MusicTertiary else Color.White
                        )
                        Text(
                            text = "${track.artist} • ${track.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Text(
                        text = "%d:%02d".format(track.durationSeconds / 60, track.durationSeconds % 60),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyDocumentsContent(
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Offline College Document Reader",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Read PDFs, lecture slides & formula sheets locally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        itemsIndexed(viewModel.studyDocuments) { idx, doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openStudyDoc(doc) }
                    .testTag("study_doc_$idx"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MusicTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = doc.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${doc.pageCount} pgs",
                        fontSize = 11.sp,
                        color = MusicPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyDocReaderSheet(
    doc: StudyDocument,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${doc.category} • ${doc.pageCount} Pages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14))
        ) {
            Text(
                text = doc.contentPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EqualizerContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MusicCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Acoustic 5-Band Equalizer (Preset: Bass Boost)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                val bands = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
                val values = listOf(0.8f, 0.65f, 0.5f, 0.7f, 0.85f)

                bands.forEachIndexed { i, band ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = band, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(60.dp))
                        Slider(
                            value = values[i],
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MusicTertiary,
                                activeTrackColor = MusicPrimary
                            )
                        )
                        Text(text = "+${(values[i] * 6).toInt()}dB", fontSize = 11.sp, color = MusicTertiary, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}
