package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.JournalNoteEntity
import com.example.data.model.TradeEntity
import com.example.data.model.UserEntity
import com.example.data.remote.GeminiService
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FilterParams(
    val query: String = "",
    val asset: String = "All",
    val strategy: String = "All",
    val timeframe: String = "All",
    val outcome: String = "All",
    val sortBy: String = "Newest"
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TradingRepository(db.userDao(), db.tradeDao(), db.journalNoteDao())

    // Direct exposure of repository flows
    val currentUser: StateFlow<UserEntity?> = repository.currentUser
    val allTrades: StateFlow<List<TradeEntity>> = repository.allTrades.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val allNotes: StateFlow<List<JournalNoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current screen navigation state inside simple view state (or we can use standard nav, but maintaining local states can be helpful too)
    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    // Search and Filtering State represented as a single state flow
    private val _filterParams = MutableStateFlow(FilterParams())
    val filterParams: StateFlow<FilterParams> = _filterParams.asStateFlow()

    fun updateFilters(
        query: String = _filterParams.value.query,
        asset: String = _filterParams.value.asset,
        strategy: String = _filterParams.value.strategy,
        timeframe: String = _filterParams.value.timeframe,
        outcome: String = _filterParams.value.outcome,
        sortBy: String = _filterParams.value.sortBy
    ) {
        _filterParams.value = FilterParams(query, asset, strategy, timeframe, outcome, sortBy)
    }

    // Computed Filtered Trades by combining exactly TWO flows
    val filteredTrades: StateFlow<List<TradeEntity>> = combine(
        allTrades,
        _filterParams
    ) { trades, params ->
        var list = trades

        // 1. Filter by search query
        if (params.query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(params.query, ignoreCase = true) ||
                it.asset.contains(params.query, ignoreCase = true) ||
                it.strategy.contains(params.query, ignoreCase = true)
            }
        }

        // 2. Filter by Asset
        if (params.asset != "All") {
            list = list.filter { it.asset.equals(params.asset, ignoreCase = true) }
        }

        // 3. Filter by Strategy
        if (params.strategy != "All") {
            list = list.filter { it.strategy.equals(params.strategy, ignoreCase = true) }
        }

        // 4. Filter by Timeframe
        if (params.timeframe != "All") {
            list = list.filter { it.timeframe.equals(params.timeframe, ignoreCase = true) }
        }

        // 5. Filter by Outcome
        if (params.outcome != "All") {
            list = when (params.outcome) {
                "Win" -> list.filter { it.isCompleted && it.profitLoss > 0 }
                "Loss" -> list.filter { it.isCompleted && it.profitLoss <= 0 }
                "Open" -> list.filter { !it.isCompleted }
                else -> list
            }
        }

        // 6. Sorting
        list = when (params.sortBy) {
            "Newest" -> list.sortedByDescending { it.timestamp }
            "Oldest" -> list.sortedBy { it.timestamp }
            "Profit (High to Low)" -> list.sortedByDescending { it.profitLoss }
            "Profit (Low to High)" -> list.sortedBy { it.profitLoss }
            "Lot Size" -> list.sortedByDescending { it.lotSize }
            else -> list.sortedByDescending { it.timestamp }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current selected trade detail with AI critique state
    private val _selectedTrade = MutableStateFlow<TradeEntity?>(null)
    val selectedTrade: StateFlow<TradeEntity?> = _selectedTrade

    private val _aiCritique = MutableStateFlow<String?>(null)
    val aiCritique: StateFlow<String?> = _aiCritique

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    init {
        // Automatically attempt auto-login
        viewModelScope.launch {
            repository.attemptAutoLogin(getApplication())
            // Sync dynamic user account balance based on current trades once loaded
            allTrades.collect { trades ->
                syncUserBalance(trades)
            }
        }
    }

    // Auth actions
    fun register(email: String, username: String, balance: Double, currency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null
            val success = repository.register(getApplication(), email, username, balance, currency)
            _isLoginLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _loginError.value = "An account with this email already exists."
            }
        }
    }

    fun login(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null
            val success = repository.login(getApplication(), email)
            _isLoginLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _loginError.value = "No account found with this email. Please click register first!"
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout(getApplication())
            onSuccess()
        }
    }

    fun updateProfile(username: String, currency: String) {
        viewModelScope.launch {
            currentUser.value?.let { current ->
                val updated = current.copy(username = username, currency = currency)
                repository.updateProfile(updated)
            }
        }
    }

    // Trade DB CRUD Actions
    fun insertTrade(
        title: String,
        asset: String,
        isBuy: Boolean,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        exitPrice: Double,
        lotSize: Double,
        riskPercentage: Double,
        profitLoss: Double,
        isCompleted: Boolean,
        strategy: String,
        timeframe: String,
        marketCondition: String,
        emotionalState: String,
        mistakes: String,
        lessons: String,
        notes: String,
        isFavorite: Boolean,
        tagsCsv: String,
        cachedImages: List<String>
    ) {
        viewModelScope.launch {
            val imageCsv = cachedImages.joinToString(",")
            val newTrade = TradeEntity(
                title = title.ifEmpty { "${if (isBuy) "BUY" else "SELL"} $asset" },
                asset = asset.uppercase(Locale.ROOT),
                isBuy = isBuy,
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                exitPrice = if (isCompleted) exitPrice else 0.0,
                lotSize = lotSize,
                riskPercentage = riskPercentage,
                profitLoss = if (isCompleted) profitLoss else 0.0,
                isCompleted = isCompleted,
                strategy = strategy,
                timeframe = timeframe,
                marketCondition = marketCondition,
                emotionalState = emotionalState,
                mistakes = mistakes,
                lessons = lessons,
                notes = notes,
                timestamp = System.currentTimeMillis(),
                screenshotsCsv = imageCsv,
                isFavorite = isFavorite,
                tagsCsv = tagsCsv
            )
            repository.addTrade(newTrade)
        }
    }

    fun updateTrade(trade: TradeEntity) {
        viewModelScope.launch {
            repository.updateTrade(trade)
            // If currently viewing this trade, update loaded state
            if (_selectedTrade.value?.id == trade.id) {
                _selectedTrade.value = trade
            }
        }
    }

    fun deleteTrade(trade: TradeEntity) {
        viewModelScope.launch {
            repository.deleteTradeById(trade.id)
            if (_selectedTrade.value?.id == trade.id) {
                _selectedTrade.value = null
                _aiCritique.value = null
            }
        }
    }

    fun selectTrade(trade: TradeEntity) {
        _selectedTrade.value = trade
        _aiCritique.value = null
        // Trigger AI review automatically when looking at details if it's completed
        if (trade.isCompleted) {
            getAiTradeCritique(trade)
        }
    }

    // Save image locally (calls Repository which pipes file stream)
    fun persistImageUri(uri: Uri, callback: (String) -> Unit) {
        viewModelScope.launch {
            val localPath = repository.saveImageToLocalSandbox(getApplication(), uri)
            callback(localPath)
        }
    }

    // AI Critique helper
    private fun getAiTradeCritique(trade: TradeEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiCritique.value = "AI review engine is analyzing the market structure, emotional state and price action metrics..."
            val response = GeminiService.getTradeReview(
                asset = trade.asset,
                isBuy = trade.isBuy,
                entryPrice = trade.entryPrice,
                stopLoss = trade.stopLoss,
                takeProfit = trade.takeProfit,
                exitPrice = trade.exitPrice,
                lotSize = trade.lotSize,
                strategy = trade.strategy,
                notes = trade.notes,
                mistakes = trade.mistakes,
                emotionalState = trade.emotionalState
            )
            _aiCritique.value = response
            _isAiLoading.value = false
        }
    }

    // Sync Balance to DB based on completed trades
    private fun syncUserBalance(trades: List<TradeEntity>) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val totalPnL = trades.filter { it.isCompleted }.sumOf { it.profitLoss }
            val computedBalance = user.initialDeposit + totalPnL
            if (user.balance != computedBalance) {
                repository.saveUserBalance(computedBalance)
            }
        }
    }

    // Notes Actions
    fun insertNote(type: String, title: String, content: String, moodRating: Int, dateString: String) {
        viewModelScope.launch {
            val newNote = JournalNoteEntity(
                type = type,
                title = title.ifEmpty { "$type Journal Reflection" },
                content = content,
                moodRating = moodRating,
                dateString = dateString,
                timestamp = System.currentTimeMillis()
            )
            repository.addNote(newNote)
        }
    }

    fun deleteNote(note: JournalNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Interactive Analytics Calculations for the client dashboard!
    fun getOverviewStats(trades: List<TradeEntity>): OverviewStats {
        val completed = trades.filter { it.isCompleted }
        val open = trades.filter { !it.isCompleted }
        
        val totalTradesCount = completed.size
        if (totalTradesCount == 0) {
            return OverviewStats(
                totalTrades = 0,
                openTradesCount = open.size,
                winRate = 0f,
                totalProfitLoss = 0.0,
                consecutiveWins = 0,
                consecutiveLosses = 0,
                avgRiskRewardRatio = 0.0,
                profitFactor = 0.0
            )
        }
        
        val winningTrades = completed.filter { it.profitLoss > 0 }
        val losingTrades = completed.filter { it.profitLoss <= 0 }
        
        val winRate = (winningTrades.size.toFloat() / totalTradesCount.toFloat()) * 100f
        val totalPnL = completed.sumOf { it.profitLoss }

        // Streaks computation
        var maxWinStreak = 0
        var maxLossStreak = 0
        var currentWinStreak = 0
        var currentLossStreak = 0

        // Sort trades ascending to compute chronological stats
        val sortedChronological = completed.sortedBy { it.timestamp }
        for (trade in sortedChronological) {
            if (trade.profitLoss > 0) {
                currentWinStreak++
                currentLossStreak = 0
                if (currentWinStreak > maxWinStreak) maxWinStreak = currentWinStreak
            } else {
                currentLossStreak++
                currentWinStreak = 0
                if (currentLossStreak > maxLossStreak) maxLossStreak = currentLossStreak
            }
        }

        // Avg Risk Reward
        var totalRRatio = 0.0
        var rRatioCounts = 0
        for (trade in completed) {
            val risk = Math.abs(trade.entryPrice - trade.stopLoss)
            val reward = Math.abs(trade.takeProfit - trade.entryPrice)
            if (risk > 0) {
                totalRRatio += (reward / risk)
                rRatioCounts++
            }
        }
        val avgRR = if (rRatioCounts > 0) totalRRatio / rRatioCounts else 1.5

        // Profit Factor = Total Gross Profits / Total Gross Losses
        val grossProfit = winningTrades.sumOf { it.profitLoss }
        val grossLoss = Math.abs(losingTrades.sumOf { it.profitLoss })
        val profitFactor = if (grossLoss > 0) grossProfit / grossLoss else grossProfit

        return OverviewStats(
            totalTrades = totalTradesCount,
            openTradesCount = open.size,
            winRate = winRate,
            totalProfitLoss = totalPnL,
            consecutiveWins = maxWinStreak,
            consecutiveLosses = maxLossStreak,
            avgRiskRewardRatio = avgRR,
            profitFactor = profitFactor
        )
    }

    // Breakdown stats for graph visualizations
    fun getStrategyPerformance(trades: List<TradeEntity>): List<Pair<String, Double>> {
        val completed = trades.filter { it.isCompleted }
        return completed.groupBy { it.strategy }
            .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
            .toList()
            .sortedByDescending { it.second }
    }

    fun getAssetPerformance(trades: List<TradeEntity>): List<Pair<String, Double>> {
        val completed = trades.filter { it.isCompleted }
        return completed.groupBy { it.asset }
            .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
            .toList()
            .sortedByDescending { it.second }
    }

    fun getEmotionalPerformance(trades: List<TradeEntity>): List<Pair<String, Double>> {
        val completed = trades.filter { it.isCompleted }
        return completed.groupBy { it.emotionalState }
            .mapValues { entry -> entry.value.sumOf { it.profitLoss } }
            .toList()
    }
    
    fun getMistakesCount(trades: List<TradeEntity>): List<Pair<String, Int>> {
        val completed = trades.filter { it.isCompleted }
        return completed.groupBy { it.mistakes }
            .mapValues { entry -> entry.value.size }
            .toList()
            .filter { it.first != "None" }
            .sortedByDescending { it.second }
    }

    fun getEquityCurvePoints(trades: List<TradeEntity>, initialBalance: Double): List<Float> {
        val completed = trades.filter { it.isCompleted }.sortedBy { it.timestamp }
        val points = mutableListOf<Float>()
        points.add(initialBalance.toFloat())
        var current = initialBalance
        for (trade in completed) {
            current += trade.profitLoss
            points.add(current.toFloat())
        }
        return points
    }
}

data class OverviewStats(
    val totalTrades: Int,
    val openTradesCount: Int,
    val winRate: Float,
    val totalProfitLoss: Double,
    val consecutiveWins: Int,
    val consecutiveLosses: Int,
    val avgRiskRewardRatio: Double,
    val profitFactor: Double
)
