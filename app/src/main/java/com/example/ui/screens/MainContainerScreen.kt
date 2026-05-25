package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalNoteEntity
import com.example.data.model.TradeEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FilterParams
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Local Stats Container Class
data class DashboardStatsLocal(
    val total: Int,
    val openCount: Int,
    val winRate: Float,
    val pnl: Double,
    val streak: Int,
    val rr: Double,
    val pf: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    user: UserEntity,
    trades: List<TradeEntity>,
    filteredTrades: List<TradeEntity>,
    filterParams: FilterParams,
    allNotes: List<JournalNoteEntity>,
    onFilterChange: (FilterParams) -> Unit,
    onAddTrade: (
        title: String, asset: String, isBuy: Boolean, entryPrice: Double, stopLoss: Double,
        takeProfit: Double, exitPrice: Double, lotSize: Double, riskPercentage: Double,
        profitLoss: Double, isCompleted: Boolean, strategy: String, timeframe: String,
        marketCondition: String, emotionalState: String, mistakes: String, lessons: String,
        notes: String, isFavorite: Boolean, tags: String, images: List<String>
    ) -> Unit,
    onDeleteTrade: (TradeEntity) -> Unit,
    onSelectTrade: (TradeEntity) -> Unit,
    onAddNote: (type: String, title: String, content: String, mood: Int, date: String) -> Unit,
    onDeleteNote: (JournalNoteEntity) -> Unit,
    onUpdateProfile: (username: String, currency: String) -> Unit,
    onResetBalance: (Double) -> Unit,
    onLogout: () -> Unit,
    onNavigateToDetails: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Dashboard") } 
    var showAddTradeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FintechBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.username.take(1).uppercase(Locale.ROOT),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Hello, ${user.username}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Live Portfolio",
                                color = TextMutedGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${user.currency} ${String.format("%.2f", user.balance)}",
                                color = TextSilverBase,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBg)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navigationItems = listOf(
                    Triple("Dashboard", Icons.Default.Home, "Dashboard"),
                    Triple("Journal", Icons.Default.List, "Journal"),
                    Triple("Analytics", Icons.Default.Settings, "Analytics"),
                    Triple("Calendar", Icons.Default.Info, "Calendar"),
                    Triple("Reflections", Icons.Default.Person, "Notes")
                )

                navigationItems.forEach { (label, icon, tabId) ->
                    NavigationBarItem(
                        selected = activeTab == tabId,
                        onClick = { activeTab = tabId },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = FintechBlue,
                            unselectedIconColor = TextMutedGray,
                            unselectedTextColor = TextMutedGray
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "Journal") {
                FloatingActionButton(
                    onClick = { showAddTradeDialog = true },
                    containerColor = FintechBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_trade_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Journal Entry")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Dashboard" -> DashboardTab(user, trades, onSelectTrade, onNavigateToDetails, onResetBalance)
                "Journal" -> JournalTab(filteredTrades, filterParams, onFilterChange, onSelectTrade, onNavigateToDetails, onDeleteTrade)
                "Analytics" -> AnalyticsTab(user, trades)
                "Calendar" -> CalendarTab(trades, onSelectTrade, onNavigateToDetails)
                "Notes" -> ReflectionsTab(allNotes, onAddNote, onDeleteNote)
                "Settings" -> SettingsTab(user, onUpdateProfile, onResetBalance, onLogout)
            }
        }
    }

    if (showAddTradeDialog) {
        AddTradeDialog(
            userBalance = user.balance,
            onDismiss = { showAddTradeDialog = false },
            onConfirm = { mapInputs ->
                onAddTrade(
                    mapInputs["title"] as? String ?: "",
                    mapInputs["asset"] as? String ?: "",
                    mapInputs["isBuy"] as? Boolean ?: true,
                    mapInputs["entryPrice"] as? Double ?: 0.0,
                    mapInputs["stopLoss"] as? Double ?: 0.0,
                    mapInputs["takeProfit"] as? Double ?: 0.0,
                    mapInputs["exitPrice"] as? Double ?: 0.0,
                    mapInputs["lotSize"] as? Double ?: 0.0,
                    mapInputs["riskPercentage"] as? Double ?: 1.0,
                    mapInputs["profitLoss"] as? Double ?: 0.0,
                    mapInputs["isCompleted"] as? Boolean ?: true,
                    mapInputs["strategy"] as? String ?: "",
                    mapInputs["timeframe"] as? String ?: "",
                    mapInputs["marketCondition"] as? String ?: "",
                    mapInputs["emotionalState"] as? String ?: "",
                    mapInputs["mistakes"] as? String ?: "",
                    mapInputs["lessons"] as? String ?: "",
                    mapInputs["notes"] as? String ?: "",
                    mapInputs["isFavorite"] as? Boolean ?: false,
                    mapInputs["tags"] as? String ?: "",
                    mapInputs["images"] as? List<String> ?: emptyList()
                )
                showAddTradeDialog = false
            }
        )
    }
}

// ---------------- DASHBOARD TAB VIEW ----------------
@Composable
fun DashboardTab(
    user: UserEntity,
    trades: List<TradeEntity>,
    onSelectTrade: (TradeEntity) -> Unit,
    onNavigateToDetails: () -> Unit,
    onResetBalance: (Double) -> Unit
) {
    val helperStats = remember(trades) {
        val completed = trades.filter { it.isCompleted }
        val open = trades.filter { !it.isCompleted }
        val winTrades = completed.filter { it.profitLoss > 0 }
        val loseTrades = completed.filter { it.profitLoss <= 0 }
        
        val winRate = if (completed.isNotEmpty()) (winTrades.size.toFloat() / completed.size * 100f) else 0f
        val totalPnL = completed.sumOf { it.profitLoss }
        
        var maxWinStreak = 0
        var curWinStreak = 0
        completed.sortedBy { it.timestamp }.forEach {
            if (it.profitLoss > 0) {
                curWinStreak++
                if (curWinStreak > maxWinStreak) maxWinStreak = curWinStreak
            } else {
                curWinStreak = 0
            }
        }
        
        var totalR = 0.0
        var rCount = 0
        completed.forEach {
            val rsk = Math.abs(it.entryPrice - it.stopLoss)
            val rwd = Math.abs(it.takeProfit - it.entryPrice)
            if (rsk > 0) {
                totalR += (rwd / rsk)
                rCount++
            }
        }
        val avgRR = if (rCount > 0) totalR / rCount else 1.5

        val grossProfits = winTrades.sumOf { it.profitLoss }
        val grossLosses = Math.abs(loseTrades.sumOf { it.profitLoss })
        val profitFactor = if (grossLosses > 0) grossProfits / grossLosses else grossProfits

        DashboardStatsLocal(
            total = completed.size,
            openCount = open.size,
            winRate = winRate,
            pnl = totalPnL,
            streak = maxWinStreak,
            rr = avgRR,
            pf = profitFactor
        )
    }

    val liveTimeStr = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val sdf = SimpleDateFormat("HH:mm:ss UTC (yyyy-MM-dd)", Locale.US)
            liveTimeStr.value = sdf.format(cal.time)
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PORTFOLIO TELEMETRY", color = FintechBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Proprietary Metrics", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    liveTimeStr.value,
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            val points = remember(trades, user.initialDeposit) {
                val completed = trades.filter { it.isCompleted }.sortedBy { it.timestamp }
                val accum = mutableListOf<Float>()
                accum.add(user.initialDeposit.toFloat())
                var current = user.initialDeposit
                for (t in completed) {
                    current += t.profitLoss
                    accum.add(current.toFloat())
                }
                accum
            }
            GlassyCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                CustomEquityCurve(points = points, modifier = Modifier.fillMaxSize(), currencyString = user.currency)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("NET REALIZED PNL", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val sign = if (helperStats.pnl >= 0) "+" else ""
                        val color = if (helperStats.pnl >= 0) BullishEmerald else BearishCrimson
                        Text(
                            "$sign${user.currency} ${String.format("%.2f", helperStats.pnl)}",
                            color = color,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Growth: ${String.format("%.1f", (helperStats.pnl / user.initialDeposit * 100))}%",
                            color = color.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("WIN RATIO", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${String.format("%.1f", helperStats.winRate)}%",
                            color = BullishEmerald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Completed: ${helperStats.total} trades",
                            color = TextMutedGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PROFIT FACTOR", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            String.format("%.2f", helperStats.pf),
                            color = if (helperStats.pf >= 1.5) BullishEmerald else WarningGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PEAK WIN STREAK", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Streak", tint = WarningGold, modifier = Modifier.size(16.dp))
                            Text(
                                "${helperStats.streak} Wins",
                                color = TextSilverBase,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("AVG RISK:REWARD", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "1 : ${String.format("%.1f", helperStats.rr)}",
                            color = FintechBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            PositionSizeCalculator(accountBalance = user.balance)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RECENT COMPLETED ENTRIES", color = TextSilverBase, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Total Open: ${helperStats.openCount}",
                    color = if (helperStats.openCount > 0) WarningGold else TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        val recentList = trades.take(5)
        if (recentList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(ObsidianSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No trades recorded yet. Go to Journal to log your first trade!", color = TextMutedGray, fontSize = 12.sp)
                }
            }
        } else {
            items(recentList) { trade ->
                TradeItemRow(
                    trade = trade,
                    currency = user.currency,
                    onClick = {
                        onSelectTrade(trade)
                        onNavigateToDetails()
                    }
                )
            }
        }
    }
}

// ---------------- JOURNAL / FILTER SCREEN ----------------
@Composable
fun JournalTab(
    filteredTrades: List<TradeEntity>,
    params: FilterParams,
    onFilterChange: (FilterParams) -> Unit,
    onSelectTrade: (TradeEntity) -> Unit,
    onNavigateToDetails: () -> Unit,
    onDeleteTrade: (TradeEntity) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = params.query,
                onValueChange = { onFilterChange(params.copy(query = it)) },
                placeholder = { Text("Search asset or strategy...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMutedGray, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FintechBlue,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextSilverBase,
                    unfocusedTextColor = TextSilverBase
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("trade_search_input")
            )

            IconButton(
                onClick = { showFilterMenu = !showFilterMenu },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, if (showFilterMenu) FintechBlue else GlassBorder, RoundedCornerShape(12.dp))
                    .size(52.dp)
                    .testTag("filter_options_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Filter",
                    tint = if (showFilterMenu) FintechBlue else Color.White
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterMenu,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text("ADVANCED SAAS FILTERS", color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Outcome: ", color = TextMutedGray, fontSize = 12.sp, modifier = Modifier.width(70.dp))
                    listOf("All", "Win", "Loss", "Open").forEach { opt ->
                        val active = params.outcome == opt
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) FintechBlue else ObsidianCard)
                                .clickable { onFilterChange(params.copy(outcome = opt)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(opt, color = if (active) Color.White else TextMutedGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort By: ", color = TextMutedGray, fontSize = 12.sp, modifier = Modifier.width(70.dp))
                    listOf("Newest", "Oldest", "Profit (High to Low)").forEach { opt ->
                        val active = params.sortBy == opt
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) FintechBlue else ObsidianCard)
                                .clickable { onFilterChange(params.copy(sortBy = opt)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val disp = opt.substringBefore(" (")
                            Text(disp, color = if (active) Color.White else TextMutedGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Asset: ", color = TextMutedGray, fontSize = 12.sp, modifier = Modifier.width(70.dp))
                    listOf("All", "BTCUSD", "EURUSD", "AAPL", "XAUUSD").forEach { opt ->
                        val active = params.asset == opt
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) FintechBlue else ObsidianCard)
                                .clickable { onFilterChange(params.copy(asset = opt)) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(opt, color = if (active) Color.White else TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "No trades", tint = TextMutedGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No matching trade logs found.", color = TextMutedGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTrades) { trade ->
                    TradeItemRow(
                        trade = trade,
                        currency = "USD",
                        onClick = {
                            onSelectTrade(trade)
                            onNavigateToDetails()
                        },
                        onDelete = { onDeleteTrade(trade) }
                    )
                }
            }
        }
    }
}

// ---------------- INDIVIDUAL ROW COMPONENT ----------------
@Composable
fun TradeItemRow(
    trade: TradeEntity,
    currency: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val directionText = if (trade.isBuy) "BUY (Long)" else "SELL (Short)"
    val directionColor = if (trade.isBuy) FintechBlue else WarningGold
    val pnlColor = if (trade.profitLoss >= 0) BullishEmerald else BearishCrimson
    val sign = if (trade.profitLoss >= 0) "+" else ""

    val rowGlassBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0x1AFFFFFF), // ~10% white glaze
                Color(0x05FFFFFF)  // ~2% white transparency
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(rowGlassBrush)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(directionColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            directionText,
                            color = directionColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        trade.asset,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (trade.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", tint = WarningGold, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    trade.title,
                    color = TextSilverBase.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Strat: ${trade.strategy}", color = TextMutedGray, fontSize = 10.sp)
                    Text("•", color = TextMutedGray, fontSize = 10.sp)
                    Text("Time: ${trade.timeframe}", color = TextMutedGray, fontSize = 10.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (trade.isCompleted) {
                    Text(
                        "$sign$ ${String.format("%.2f", trade.profitLoss)}",
                        color = pnlColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WarningGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("OPEN POSITION", color = WarningGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(trade.timestamp)),
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = BearishCrimson.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// ---------------- ANALYTICS VIEW TAB ----------------
@Composable
fun AnalyticsTab(
    user: UserEntity,
    trades: List<TradeEntity>
) {
    val completedTrades = remember(trades) { trades.filter { it.isCompleted } }

    if (completedTrades.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Analytics Settings", tint = TextMutedGray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Complete trades in your journal to unlock master analytics!", color = TextMutedGray, fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("FINTECH COGNITIVE METRICS", color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Quantitative Performance", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        item {
            val wins = completedTrades.filter { it.profitLoss > 0 }.size
            val rate = (wins.toFloat() / completedTrades.size) * 100f
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Portfolio Win Loss Ratio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                WinLossDonutChart(winRate = rate, totalTrades = completedTrades.size, modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            val stratList = remember(completedTrades) {
                completedTrades.groupBy { it.strategy }
                    .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
                    .toList()
                    .sortedByDescending { it.second }
            }
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                CustomStrategyPnlBarChart(strategyData = stratList, currencyString = user.currency)
            }
        }

        item {
            val assetList = remember(completedTrades) {
                completedTrades.groupBy { it.asset }
                    .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
                    .toList()
                    .sortedByDescending { it.second }
            }
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Profit/Loss by Asset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                assetList.take(4).forEach { (asset, pnl) ->
                    val color = if (pnl >= 0) BullishEmerald else BearishCrimson
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(asset, color = TextSilverBase, fontSize = 13.sp)
                        Text(
                            "${if (pnl >= 0) "+" else ""}${String.format("%.2f", pnl)} ${user.currency}",
                            color = color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            val emotionStats = remember(completedTrades) {
                completedTrades.groupBy { it.emotionalState }
                    .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
                    .toList()
            }
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Trader's Behavioral Psychology", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("P&L associated with your mental states", color = TextMutedGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                emotionStats.forEach { (state, pnl) ->
                    val color = if (pnl >= 0) BullishEmerald else BearishCrimson
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val emoji = when (state.lowercase(Locale.ROOT)) {
                                "greedy" -> "🤑"
                                "fearful" -> "😨"
                                "excited" -> "⚡"
                                "patient" -> "🧘"
                                "fomo" -> "🏃"
                                else -> "🧘"
                            }
                            Text(emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state, color = TextSilverBase, fontSize = 13.sp)
                        }
                        Text(
                            "${if (pnl >= 0) "+" else ""}${String.format("%.1f", pnl)} ${user.currency}",
                            color = color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            val mistakesList = remember(completedTrades) {
                completedTrades.groupBy { it.mistakes }
                    .mapValues { entry -> entry.value.size }
                    .toList()
                    .filter { it.first != "None" && it.first.isNotEmpty() }
                    .sortedByDescending { it.second }
            }
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Admitted Mistakes Loop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                if (mistakesList.isEmpty()) {
                    Text("Clean record! No technical or behavioral mistakes logged.", color = BullishEmerald, fontSize = 12.sp)
                } else {
                    mistakesList.take(3).forEach { (mistake, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(mistake, color = TextSilverBase, fontSize = 13.sp)
                            Text("$count occurrences", color = BearishCrimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TRADING CALENDAR VIEW ----------------
@Composable
fun CalendarTab(
    trades: List<TradeEntity>,
    onSelectTrade: (TradeEntity) -> Unit,
    onNavigateToDetails: () -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }

    val monthName = remember(currentMonth) {
        val formatter = SimpleDateFormat("MMMM", Locale.US)
        cal.set(Calendar.MONTH, currentMonth)
        formatter.format(cal.time)
    }

    val tradesByDateString = remember(trades) {
        val map = mutableMapOf<String, MutableList<TradeEntity>>()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (trade in trades) {
            val dateStr = formatter.format(Date(trade.timestamp))
            if (!map.containsKey(dateStr)) {
                map[dateStr] = mutableListOf()
            }
            map[dateStr]?.add(trade)
        }
        map
    }

    var selectedDateStr by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CHRONOLOGICAL LEDGER", color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("$monthName $currentYear", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else currentMonth--
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev", tint = Color.White)
                }
                IconButton(onClick = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else currentMonth++
                }) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("SU", "MO", "TU", "WE", "TH", "FR", "SA").forEach { day ->
                Text(
                    text = day,
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val daysInMonth = remember(currentMonth, currentYear) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, currentYear)
            c.set(Calendar.MONTH, currentMonth)
            c.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val firstDayOfWeek = remember(currentMonth, currentYear) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, currentYear)
            c.set(Calendar.MONTH, currentMonth)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.get(Calendar.DAY_OF_WEEK) - 1
        }

        val totalGridCells = 42
        val cellsList = mutableListOf<Int?>()
        for (i in 0 until totalGridCells) {
            if (i < firstDayOfWeek || i >= firstDayOfWeek + daysInMonth) {
                cellsList.add(null)
            } else {
                cellsList.add(i - firstDayOfWeek + 1)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            cellsList.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { dayNum ->
                        if (dayNum == null) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dayStr = String.format("%02d", dayNum)
                            val monthStr = String.format("%02d", currentMonth + 1)
                            val matchKey = "$currentYear-$monthStr-$dayStr"
                            val dayTrades = tradesByDateString[matchKey] ?: emptyList()

                            val dayNetPnl = dayTrades.filter { it.isCompleted }.sumOf { it.profitLoss }
                            val isProfitableDay = dayNetPnl > 0
                            val hasLosses = dayNetPnl < 0

                            val cellBg = if (dayTrades.isNotEmpty()) {
                                if (isProfitableDay) BullishEmerald.copy(alpha = 0.15f) else if (hasLosses) BearishCrimson.copy(alpha = 0.12f) else ObsidianCard
                            } else {
                                ObsidianCard
                            }

                            val cellBorder = if (selectedDateStr == matchKey) FintechBlue else GlassBorder

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .border(1.dp, cellBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedDateStr = matchKey }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        color = if (dayTrades.isNotEmpty()) Color.White else TextSilverBase.copy(alpha = 0.7f),
                                        fontWeight = if (dayTrades.isNotEmpty()) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                    if (dayTrades.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isProfitableDay) BullishEmerald else BearishCrimson)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedDateStr.isNotEmpty()) {
            val dateTrades = tradesByDateString[selectedDateStr] ?: emptyList()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Trades for $selectedDateStr",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (dateTrades.isNotEmpty()) {
                    val net = dateTrades.filter { it.isCompleted }.sumOf { it.profitLoss }
                    Text(
                        "Net PnL: ${if (net >= 0) "+" else ""}${String.format("%.2f", net)}",
                        color = if (net >= 0) BullishEmerald else BearishCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (dateTrades.isEmpty()) {
                Text("No trading entries recorded on this day.", color = TextMutedGray, fontSize = 12.sp)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(dateTrades) { trade ->
                        TradeItemRow(
                            trade = trade,
                            currency = "USD",
                            onClick = {
                                onSelectTrade(trade)
                                onNavigateToDetails()
                            }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Tap any highlighted calendar date to inspect trades taken!", color = TextMutedGray, fontSize = 12.sp)
            }
        }
    }
}

// ---------------- REFLECTIONS / NOTES VIEW TAB ----------------
@Composable
fun ReflectionsTab(
    allNotes: List<JournalNoteEntity>,
    onAddNote: (type: String, title: String, content: String, mood: Int, date: String) -> Unit,
    onDeleteNote: (JournalNoteEntity) -> Unit
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TRADER'S REFLECTIVE COGNITION", color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Psychology Logs & Goals", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FintechBlue)
                    .size(40.dp)
                    .testTag("add_note_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New note", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "No reflections", tint = TextMutedGray, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No psychology reviews logged yet. Write your first reflection!", color = TextMutedGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allNotes) { note ->
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FintechBlue.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(note.type, color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                val emoji = when (note.moodRating) {
                                    5 -> "😁 Best"
                                    4 -> "🙂 Patient"
                                    3 -> "😐 Neutral"
                                    2 -> "😰 Anxious"
                                    1 -> "🤮 Tilt"
                                    else -> "😐"
                                }
                                Text(emoji, color = TextMutedGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { onDeleteNote(note) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = BearishCrimson.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(note.content, color = TextSilverBase.copy(alpha = 0.85f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(note.dateString, color = TextMutedGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var noteType by remember { mutableStateOf("DAILY") }
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var moodRating by remember { mutableStateOf(3) }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Psychology Entry", color = Color.White) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DAILY", "WEEKLY", "GOALS", "BIAS").forEach { type ->
                            val active = noteType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) FintechBlue else ObsidianCard)
                                    .clickable { noteType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type, color = if (active) Color.White else TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = { Text("E.g., FOMO Post-Session Review") },
                        label = { Text("Log Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        placeholder = { Text("Detail your mental focus, rules followed and notes for optimization...") },
                        label = { Text("Review Narrative") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                    )

                    Text("Mental State Gauge:", color = TextMutedGray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            1 to "🤮 Tilt",
                            2 to "😰 Fear",
                            3 to "😐 Neutral",
                            4 to "🙂 Focus",
                            5 to "😁 Elite"
                        ).forEach { (rating, label) ->
                            val active = moodRating == rating
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { moodRating = rating }
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) FintechBlue.copy(alpha = 0.2f) else Color.Transparent)
                                    .padding(6.dp)
                            ) {
                                Text(label.substringBefore(" "), fontSize = 18.sp)
                                Text(label.substringAfter(" "), color = if (active) FintechBlue else TextMutedGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        onAddNote(noteType, noteTitle, noteContent, moodRating, simpleDate)
                        showAddNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = TextMutedGray)
                }
            },
            containerColor = ObsidianSurface
        )
    }
}

// ---------------- SETTINGS / PROFILE TAB ----------------
@Composable
fun SettingsTab(
    user: UserEntity,
    onUpdateProfile: (username: String, currency: String) -> Unit,
    onResetBalance: (Double) -> Unit,
    onLogout: () -> Unit
) {
    var editUsername by remember { mutableStateOf(user.username) }
    var editCurrency by remember { mutableStateOf(user.currency) }
    var resetValInput by remember { mutableStateOf("10000") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("LEDGER HARDWARE SETTINGS", color = FintechBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Admin & Profile Rules", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        item {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Trader Profile Config", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = editUsername,
                    onValueChange = { editUsername = it },
                    label = { Text("Username") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editCurrency,
                    onValueChange = { editCurrency = it },
                    label = { Text("Currency Indicator") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onUpdateProfile(editUsername, editCurrency) },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sync Changes")
                }
            }
        }

        item {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Text("Simulated Account Refunding", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Reset your capital back to default standard sizes.", color = TextMutedGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = resetValInput,
                    onValueChange = { resetValInput = it },
                    label = { Text("Balance Deposit Cash") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val d = resetValInput.toDoubleOrNull() ?: 10000.0
                        onResetBalance(d)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BearishCrimson),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("De-risk & Reset Portfolio")
                }
            }
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, BearishCrimson.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .testTag("logout_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit", tint = BearishCrimson)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De-authorize Persistent Session", color = BearishCrimson, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- DYNAMIC JOURNAL DIALOG CREATE ----------------
@Composable
fun AddTradeDialog(
    userBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var asset by remember { mutableStateOf("BTCUSD") }
    var isBuy by remember { mutableStateOf(true) }
    var entryPrice by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    var exitPrice by remember { mutableStateOf("") }
    var lotSize by remember { mutableStateOf("0.1") }
    var riskPercentage by remember { mutableStateOf("1.0") }
    var profitLoss by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(true) }
    var strategy by remember { mutableStateOf("Breakout") }
    var timeframe by remember { mutableStateOf("H1") }
    var marketCondition by remember { mutableStateOf("Trending") }
    var emotionalState by remember { mutableStateOf("Calm") }
    var lessons by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var tagsCsv by remember { mutableStateOf("") }

    var fomoChecked by remember { mutableStateOf(false) }
    var overLeveagedChecked by remember { mutableStateOf(false) }
    var movedSlChecked by remember { mutableStateOf(false) }
    var noneChecked by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val cachedImages = remember { mutableStateListOf<String>() }
    var isPickImageLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isPickImageLoading = true
            val imagesDir = java.io.File(context.filesDir, "trade_screenshots")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val finalFile = java.io.File(imagesDir, "IMG_" + UUID.randomUUID().toString() + ".jpg")
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    java.io.FileOutputStream(finalFile).use { out ->
                        stream.copyTo(out)
                    }
                }
                cachedImages.add(finalFile.absolutePath)
            } catch (e: Exception) {
                // handle logs
            }
            isPickImageLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Trade Entry", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { isBuy = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBuy) FintechBlue else ObsidianCard),
                        modifier = Modifier.weight(1f).testTag("select_buy_btn")
                    ) {
                        Text("BUY (Long)", color = if (isBuy) Color.White else TextMutedGray, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isBuy = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isBuy) WarningGold else ObsidianCard),
                        modifier = Modifier.weight(1f).testTag("select_sell_btn")
                    ) {
                        Text("SELL (Short)", color = if (!isBuy) Color.White else TextMutedGray, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = asset,
                    onValueChange = { asset = it },
                    label = { Text("Asset Ticker (e.g. BTCUSD)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("trade_asset_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = entryPrice,
                        onValueChange = { entryPrice = it },
                        label = { Text("Entry Price") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(1f).testTag("trade_entry_price_input")
                    )
                    OutlinedTextField(
                        value = lotSize,
                        onValueChange = { lotSize = it },
                        label = { Text("Lots / Size") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(1f).testTag("trade_lots_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stopLoss,
                        onValueChange = { stopLoss = it },
                        label = { Text("Stop Loss") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(1f).testTag("trade_sl_input")
                    )
                    OutlinedTextField(
                        value = takeProfit,
                        onValueChange = { takeProfit = it },
                        label = { Text("Take Profit") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(1f).testTag("trade_tp_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Completed State?", color = TextMutedGray)
                    Switch(checked = isCompleted, onCheckedChange = { isCompleted = it })
                }

                if (isCompleted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = exitPrice,
                            onValueChange = { exitPrice = it },
                            label = { Text("Close Price") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                            modifier = Modifier.weight(1f).testTag("trade_close_price_input")
                        )
                        OutlinedTextField(
                            value = profitLoss,
                            onValueChange = { profitLoss = it },
                            label = { Text("Result P&L (+ / -)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                            modifier = Modifier.weight(1f).testTag("trade_pnl_input")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = strategy,
                        onValueChange = { strategy = it },
                        label = { Text("Strategy Block") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(1.2f).testTag("trade_strategy_input")
                    )
                    OutlinedTextField(
                        value = timeframe,
                        onValueChange = { timeframe = it },
                        label = { Text("Timeframe") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                        modifier = Modifier.weight(0.8f).testTag("trade_timeframe_input")
                    )
                }

                Text("Emotional State during Execution:", color = TextMutedGray, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Calm", "Greedy", "Fearful", "Excited", "Patient").forEach { emo ->
                        val selected = emotionalState == emo
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) FintechBlue else ObsidianCard)
                                .clickable { emotionalState = emo }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(emo, color = if (selected) Color.White else TextMutedGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Admitted Mistakes Logic Check:", color = TextMutedGray, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = fomoChecked, onCheckedChange = { fomoChecked = it; if (it) noneChecked = false })
                        Text("FOMO execution", color = TextSilverBase, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = overLeveagedChecked, onCheckedChange = { overLeveagedChecked = it; if (it) noneChecked = false })
                        Text("Over leveraged / Lot sizing violation", color = TextSilverBase, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = movedSlChecked, onCheckedChange = { movedSlChecked = it; if (it) noneChecked = false })
                        Text("Moved SL manually / Emotion", color = TextSilverBase, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = noneChecked, onCheckedChange = { noneChecked = it; if (it) { fomoChecked = false; overLeveagedChecked = false; movedSlChecked = false } })
                        Text("Perfect Play (No technical/mental mistakes)", color = BullishEmerald, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Reflections") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSilverBase, unfocusedTextColor = TextSilverBase, focusedBorderColor = FintechBlue, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("trade_notes_input")
                )

                Text("Attach Chart Analysis / Proofs (Local Storage Safe!):", color = TextMutedGray, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "No gallery/image picker app available on this device config.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianCard),
                        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Picture", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Image", color = Color.White)
                    }
                    if (cachedImages.isNotEmpty()) {
                        Text("${cachedImages.size} images attached", color = BullishEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { cachedImages.clear() }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Picture", tint = BearishCrimson)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMistake = if (noneChecked) "None" else {
                        val lst = mutableListOf<String>()
                        if (fomoChecked) lst.add("FOMO")
                        if (overLeveagedChecked) lst.add("Over Leverage")
                        if (movedSlChecked) lst.add("Moved StopLoss")
                        lst.joinToString(", ")
                    }

                    val mapInputs = mapOf(
                        "title" to title.ifEmpty { "${if (isBuy) "BUY" else "SELL"} ${asset.uppercase(Locale.ROOT)}" },
                        "asset" to asset.uppercase(Locale.ROOT),
                        "isBuy" to isBuy,
                        "entryPrice" to (entryPrice.toDoubleOrNull() ?: 0.0),
                        "stopLoss" to (stopLoss.toDoubleOrNull() ?: 0.0),
                        "takeProfit" to (takeProfit.toDoubleOrNull() ?: 0.0),
                        "exitPrice" to (exitPrice.toDoubleOrNull() ?: 0.0),
                        "lotSize" to (lotSize.toDoubleOrNull() ?: 0.1),
                        "riskPercentage" to (riskPercentage.toDoubleOrNull() ?: 1.0),
                        "profitLoss" to (profitLoss.toDoubleOrNull() ?: 0.0),
                        "isCompleted" to isCompleted,
                        "strategy" to strategy,
                        "timeframe" to timeframe,
                        "marketCondition" to marketCondition,
                        "emotionalState" to emotionalState,
                        "mistakes" to finalMistake,
                        "lessons" to lessons,
                        "notes" to notes,
                        "isFavorite" to isFavorite,
                        "tags" to tagsCsv,
                        "images" to cachedImages.toList()
                    )
                    onConfirm(mapInputs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                modifier = Modifier.testTag("submit_add_trade_button")
            ) {
                Text("Record Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedGray)
            }
        },
        containerColor = ObsidianSurface
    )
}
