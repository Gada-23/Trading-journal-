package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TradeEntity
import com.example.ui.components.GlassyCard
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailsScreen(
    trade: TradeEntity,
    aiCritique: String?,
    isAiLoading: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: (TradeEntity) -> Unit,
    onDeleteTrade: (TradeEntity) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeImageZoom by remember { mutableStateOf<String?>(null) }

    val directionText = if (trade.isBuy) "BUY (Long)" else "SELL (Short)"
    val directionColor = if (trade.isBuy) FintechBlue else WarningGold
    val pnlColor = if (trade.profitLoss >= 0) BullishEmerald else BearishCrimson
    val sign = if (trade.profitLoss >= 0) "+" else ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trade.asset, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("details_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(trade) }) {
                        Icon(
                            imageVector = if (trade.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (trade.isFavorite) WarningGold else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(directionColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    directionText,
                                    color = directionColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                trade.title,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        // Realized PNL Indicator
                        if (trade.isCompleted) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("REALIZED PNL", color = TextMutedGray, fontSize = 9.sp)
                                Text(
                                    "$sign$ ${String.format("%.2f", trade.profitLoss)}",
                                    color = pnlColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarningGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("OPEN", color = WarningGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary info row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn(label = "LOT SIZE", value = trade.lotSize.toString(), modifier = Modifier.weight(1f))
                        InfoColumn(label = "RISK %", value = "${trade.riskPercentage}%", modifier = Modifier.weight(1f))
                        val riskR = if (trade.stopLoss > 0) {
                            val rsk = Math.abs(trade.entryPrice - trade.stopLoss)
                            val rwd = Math.abs(trade.takeProfit - trade.entryPrice)
                            if (rsk > 0) "1 : ${String.format("%.1f", rwd / rsk)}" else "N/A"
                        } else "N/A"
                        InfoColumn(label = "R:R RATIO", value = riskR, modifier = Modifier.weight(1f))
                    }
                }

                // Execution detailed analytics column
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Price Blueprint (Audit)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    PricePointRow("Entry Price", trade.entryPrice)
                    PricePointRow("Take Profit Target", trade.takeProfit, BullishEmerald)
                    PricePointRow("Stop Loss Buffer", trade.stopLoss, BearishCrimson)
                    if (trade.isCompleted) {
                        PricePointRow("Exit/Close Price", trade.exitPrice)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Logged: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss UTC", Locale.getDefault()).format(Date(trade.timestamp)),
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }

                // Trade setup tactics
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Tactic & Environmental Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("STRATEGY", color = TextMutedGray, fontSize = 10.sp)
                            Text(trade.strategy, color = TextSilverBase, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("TIMEFRAME", color = TextMutedGray, fontSize = 10.sp)
                            Text(trade.timeframe, color = TextSilverBase, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MARKET BIAS", color = TextMutedGray, fontSize = 10.sp)
                            Text(trade.marketCondition, color = TextSilverBase, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Psychological Behavioral States (requested)
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cognitive Execution Focus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Mood: ${trade.emotionalState}", color = TextMutedGray, fontSize = 11.sp)
                        }
                        val emoji = when (trade.emotionalState.lowercase(Locale.ROOT)) {
                            "greedy" -> "🤑"
                            "fearful" -> "😨"
                            "excited" -> "⚡"
                            "patient" -> "🧘"
                            else -> "🧘"
                        }
                        Text(emoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("MISTAKES LOGGED", color = TextMutedGray, fontSize = 10.sp)
                    Text(
                        trade.mistakes,
                        color = if (trade.mistakes.lowercase() == "none") BullishEmerald else BearishCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Screenshot Attachments Log List
                val screenshots = trade.getScreenshotsList()
                if (screenshots.isNotEmpty()) {
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Chart Analysis Captures (${screenshots.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(screenshots) { imgPath ->
                                val file = File(imgPath)
                                if (file.exists()) {
                                    val bitmap = remember(imgPath) { BitmapFactory.decodeFile(imgPath) }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Trade Screenshot",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp, 80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { activeImageZoom = imgPath }
                                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI trade review assistant (Gemini narrative output)
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = FintechBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BULL JOURNAL AI ASSESSMENT", color = FintechBlue, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAiLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(color = FintechBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "AI risk manager is auditing trade metrics and cognitive states...",
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            text = aiCritique ?: "Audit logs ready. Save the completed trade to retrieve automatic AI feedback analysis!",
                            color = TextSilverBase,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Manual Reflections
                if (trade.notes.isNotEmpty()) {
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Your Reflections Notes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(trade.notes, color = TextSilverBase.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
            }

            // Image Zoom Overlay Pinch Frame overlay
            AnimatedVisibility(
                visible = activeImageZoom != null,
                enter = fadeIn() + scaleIn(animationSpec = tween(300)),
                exit = fadeOut() + scaleOut()
            ) {
                activeImageZoom?.let { path ->
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        offset += offsetChange
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.95f))
                            .clickable { activeImageZoom = null },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Expanded screenshot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .transformable(state = transformState)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                            )
                        }

                        // Close overlay helper
                        IconButton(
                            onClick = { activeImageZoom = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = TextMutedGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PricePointRow(label: String, price: Double, color: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMutedGray, fontSize = 12.sp)
        Text(String.format("%.5f", price), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
