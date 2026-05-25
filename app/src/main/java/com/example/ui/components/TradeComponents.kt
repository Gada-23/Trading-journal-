package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.absoluteValue

// Premium Glassmorphism Container with subtle borders and shadow depth
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0x1FFFFFFF), // Glaze top-left reflection (12% opacity)
                Color(0x05FFFFFF)  // Translucent bottom-right falloff (2% opacity)
            )
        )
    }

    val baseModifier = modifier
        .clip(shape)
        .background(glassBrush)
        .border(1.dp, GlassBorder, shape)
        .padding(20.dp)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Column(modifier = finalModifier) {
        content()
    }
}

// Gorgeous Custom Canvas Line Graph for the Equity Curve
@Composable
fun CustomEquityCurve(
    points: List<Float>,
    modifier: Modifier = Modifier,
    currencyString: String = "USD"
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(ObsidianCard, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Keep trading to visualize your Equity Curve!",
                color = TextMutedGray,
                fontSize = 13.sp
            )
        }
        return
    }

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(points) {
        animateTrigger = true
    }
    val animPercent by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val maxVal = points.maxOrNull() ?: 10000f
    val minVal = points.minOrNull() ?: 9000f
    val delta = (maxVal - minVal).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Account Growth (Equity Curve)",
                color = TextSilverBase,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "Peak: $currencyString ${String.format("%.1f", maxVal)}",
                color = BullishEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1)

            val path = Path()
            val fillPath = Path()

            // Calculate exact points coordinates
            val coords = points.mapIndexed { idx, pt ->
                val x = idx * spacing
                // invert y because Canvas drawing has 0 at the top
                val normY = (pt - minVal) / delta
                val y = height - (normY * height * 0.8f) - (height * 0.1f)
                Offset(x, y)
            }

            // Begin path
            path.moveTo(coords[0].x, coords[0].y)
            fillPath.moveTo(coords[0].x, height)
            fillPath.lineTo(coords[0].x, coords[0].y)

            for (i in 1 until coords.size) {
                // Draw path with animation interpolation
                val targetCoords = coords[i]
                val prevCoords = coords[i - 1]
                val curX = prevCoords.x + (targetCoords.x - prevCoords.x) * animPercent
                val curY = prevCoords.y + (targetCoords.y - prevCoords.y) * animPercent
                path.lineTo(curX, curY)
                fillPath.lineTo(curX, curY)
            }

            fillPath.lineTo(coords.last().x * animPercent, height)
            fillPath.close()

            // Draw gridlines
            val gridPaint = Paint().asFrameworkPaint().apply {
                color = Color.White.copy(alpha = 0.05f).toArgb()
                strokeWidth = 2f
            }
            // mid gridline
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 1f
            )

            // Draw gradient shadow fill under line
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    FintechBlue.copy(alpha = 0.25f),
                    FintechBlue.copy(alpha = 0.00f)
                )
            )
            drawPath(path = fillPath, brush = gradientBrush)

            // Draw line
            drawPath(
                path = path,
                color = FintechBlue,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw last point indicator pulsating
            if (coords.isNotEmpty()) {
                val lastC = coords.last()
                drawCircle(
                    color = FintechBlue.copy(alpha = 0.3f),
                    radius = 8.dp.toPx() * animPercent,
                    center = Offset(lastC.x * animPercent, lastC.y)
                )
                drawCircle(
                    color = FintechBlue,
                    radius = 4.dp.toPx() * animPercent,
                    center = Offset(lastC.x * animPercent, lastC.y)
                )
            }
        }
    }
}

// Custom Horizontal Bar Chart for Strategy P&L
@Composable
fun CustomStrategyPnlBarChart(
    strategyData: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    currencyString: String = "USD"
) {
    if (strategyData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(ObsidianCard, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No trade history found", color = TextMutedGray, fontSize = 13.sp)
        }
        return
    }

    Column(modifier = modifier) {
        Text(
            "Net Profit by Strategy",
            color = TextSilverBase,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        strategyData.take(5).forEach { (strategy, pnl) ->
            val color = if (pnl >= 0) BullishEmerald else BearishCrimson
            val pnlText = "${if (pnl >= 0) "+" else ""}${String.format("%.1f", pnl)} $currencyString"

            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strategy, color = TextSilverBase, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(pnlText, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Bar scale representation
                val maxPnlAbs = strategyData.maxOf { it.second.absoluteValue }.coerceAtLeast(1.0)
                val barFraction = (pnl.absoluteValue / maxPnlAbs).coerceIn(0.01, 1.0).toFloat()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barFraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

// Donut Chart for Win/Loss analytics
@Composable
fun WinLossDonutChart(
    winRate: Float,
    totalTrades: Int,
    modifier: Modifier = Modifier
) {
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(winRate) {
        animateTrigger = true
    }
    val animPercent by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val strokeWidth = 10.dp.toPx()
                // Draw losing background (Red arc)
                drawArc(
                    color = BearishCrimson.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Draw winning foreground (Green arc)
                val sweep = 360f * (winRate / 100f) * animPercent
                drawArc(
                    color = BullishEmerald,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${String.format("%.1f", winRate)}%",
                    color = BullishEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Win Rate",
                    color = TextMutedGray,
                    fontSize = 10.sp
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(BullishEmerald))
                Spacer(modifier = Modifier.width(8.dp))
                val winsCount = (totalTrades * (winRate / 100f)).toInt()
                Text("Wins: $winsCount", color = TextSilverBase, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(BearishCrimson))
                Spacer(modifier = Modifier.width(8.dp))
                val lossesCount = totalTrades - (totalTrades * (winRate / 100f)).toInt()
                Text("Losses: $lossesCount", color = TextSilverBase, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(TextMutedGray))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Total: $totalTrades", color = TextSilverBase, fontSize = 12.sp)
            }
        }
    }
}

// Position Sizing & Risk Calculator Widget (SaaS feature requested)
@Composable
fun PositionSizeCalculator(
    accountBalance: Double,
    modifier: Modifier = Modifier
) {
    var riskPctInput by remember { mutableStateOf("1.0") }
    var stopLossPipInput by remember { mutableStateOf("50") }
    var assetTypeInput by remember { mutableStateOf("Forex (1 Lot = 100k)") } // Forex, Crypto, Stocks

    val calculatedRiskCapital = accountBalance * ((riskPctInput.toDoubleOrNull() ?: 1.0) / 100.0)
    val slPips = stopLossPipInput.toDoubleOrNull() ?: 50.0
    val lotResult = if (slPips > 0) {
        if (assetTypeInput.contains("Forex")) {
            // (Standard Forex calculation: 1 pip on 1 lot ~ $10)
            calculatedRiskCapital / (slPips * 10.0)
        } else {
            // Stock/Crypto (SL distance represents risk price)
            calculatedRiskCapital / slPips
        }
    } else 0.0

    GlassyCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = FintechBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Risk & Position Size Calculator", color = TextSilverBase, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = riskPctInput,
                onValueChange = { riskPctInput = it },
                label = { Text("Risk %") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FintechBlue,
                    focusedTextColor = TextSilverBase,
                    unfocusedTextColor = TextSilverBase
                ),
                modifier = Modifier.weight(1f).testTag("calc_risk_input")
            )
            OutlinedTextField(
                value = stopLossPipInput,
                onValueChange = { stopLossPipInput = it },
                label = { Text("SL Pips/Points") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FintechBlue,
                    focusedTextColor = TextSilverBase,
                    unfocusedTextColor = TextSilverBase
                ),
                modifier = Modifier.weight(1f).testTag("calc_sl_input")
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Forex (1 Lot = 100k)", "Crypto/Crypto").forEach { type ->
                val selected = assetTypeInput == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) FintechBlue else ObsidianCard)
                        .clickable { assetTypeInput = type }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type.substringBefore(" ("),
                        color = if (selected) Color.White else TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Max Cash Risk", color = TextMutedGray, fontSize = 11.sp)
                Text(
                    "$ ${String.format("%.2f", calculatedRiskCapital)}",
                    color = BearishCrimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Recommended Size", color = TextMutedGray, fontSize = 11.sp)
                Text(
                    "${String.format("%.3f", lotResult)} Lots",
                    color = BullishEmerald,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
