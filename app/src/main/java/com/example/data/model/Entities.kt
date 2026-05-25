package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val username: String,
    val balance: Double = 10000.0,
    val initialDeposit: Double = 10000.0,
    val currency: String = "USD",
    val registrationTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val asset: String,                      // e.g. BTCUSD, EURUSD, AAPL
    val isBuy: Boolean,                     // true = Buy, false = Sell
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double = 0.0,
    val lotSize: Double,                    // position sizing
    val riskPercentage: Double = 1.0,       // account risk e.g. 1%
    val profitLoss: Double = 0.0,           // realized PnL
    val isCompleted: Boolean = true,        // Completed or Pending/Open
    val strategy: String = "Breakout",      // e.g. Breakout, Support/Resist, EMS
    val timeframe: String = "H1",           // e.g. M15, H1, H4, D1
    val marketCondition: String = "Trending",// Trending, Ranging, Volatile
    val emotionalState: String = "Calm",    // Calm, Greedy, Fearful, Patient
    val mistakes: String = "None",          // FOMO, Moved SL, Over-leveraging, None
    val lessons: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val screenshotsCsv: String = "",        // Path to cached files, comma-separated
    val isFavorite: Boolean = false,
    val tagsCsv: String = ""                // tags list comma-separated
) : Serializable {
    fun getScreenshotsList(): List<String> {
        if (screenshotsCsv.isEmpty()) return emptyList()
        return screenshotsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    fun getTagsList(): List<String> {
        if (tagsCsv.isEmpty()) return emptyList()
        return tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

@Entity(tableName = "notes")
data class JournalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,                       // DAILY, WEEKLY, MONTHLY, GOALS, PSYCHOLOGY
    val title: String,
    val content: String,
    val moodRating: Int = 3,                // 1 to 5 (Mood Tracker)
    val dateString: String,                 // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
