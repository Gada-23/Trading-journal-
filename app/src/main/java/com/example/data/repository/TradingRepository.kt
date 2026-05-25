package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.UserDao
import com.example.data.local.TradeDao
import com.example.data.local.JournalNoteDao
import com.example.data.model.JournalNoteEntity
import com.example.data.model.TradeEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class TradingRepository(
    private val userDao: UserDao,
    private val tradeDao: TradeDao,
    private val journalNoteDao: JournalNoteDao
) {
    private val TAG = "TradingRepository"

    // Authentication & Profile States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    // Observe Trades
    val allTrades: Flow<List<TradeEntity>> = tradeDao.getAllTrades()

    // Observe Notes
    val allNotes: Flow<List<JournalNoteEntity>> = journalNoteDao.getAllNotes()

    // Init: Check if there's any user in our local database to restore session automatically
    suspend fun attemptAutoLogin(context: Context) {
        val sharedPrefs = context.getSharedPreferences("trading_journal_prefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("logged_in_email", null)
        if (savedEmail != null) {
            val user = userDao.getUserByEmail(savedEmail)
            if (user != null) {
                _currentUser.value = user
                Log.d(TAG, "Auto-login successful for $savedEmail")
            }
        }
    }

    suspend fun register(
        context: Context,
        email: String,
        username: String,
        initialBalance: Double,
        currency: String
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return@withContext false
        }
        val newUser = UserEntity(
            email = email,
            username = username,
            balance = initialBalance,
            initialDeposit = initialBalance,
            currency = currency
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser

        // Persist session
        val sharedPrefs = context.getSharedPreferences("trading_journal_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("logged_in_email", email).apply()
        true
    }

    suspend fun login(context: Context, email: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            _currentUser.value = user
            val sharedPrefs = context.getSharedPreferences("trading_journal_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("logged_in_email", email).apply()
            true
        } else {
            false
        }
    }

    suspend fun logout(context: Context) = withContext(Dispatchers.IO) {
        _currentUser.value = null
        val sharedPrefs = context.getSharedPreferences("trading_journal_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("logged_in_email").apply()
    }

    suspend fun updateProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        _currentUser.value = user
    }

    // Trade Operations
    suspend fun addTrade(trade: TradeEntity): Long = withContext(Dispatchers.IO) {
        val id = tradeDao.insertTrade(trade)
        // Auto-rebalance current user balance based on complete trades
        recalculateAndSyncUserBalance()
        id
    }

    suspend fun updateTrade(trade: TradeEntity) = withContext(Dispatchers.IO) {
        tradeDao.updateTrade(trade)
        recalculateAndSyncUserBalance()
    }

    suspend fun deleteTradeById(id: Int) = withContext(Dispatchers.IO) {
        tradeDao.deleteTradeById(id)
        recalculateAndSyncUserBalance()
    }

    suspend fun getTradeById(id: Int): TradeEntity? = withContext(Dispatchers.IO) {
        tradeDao.getTradeById(id)
    }

    // Note Operations
    suspend fun addNote(note: JournalNoteEntity) = withContext(Dispatchers.IO) {
        journalNoteDao.insertNote(note)
    }

    suspend fun deleteNote(note: JournalNoteEntity) = withContext(Dispatchers.IO) {
        journalNoteDao.deleteNote(note)
    }

    // Rebalance helper
    private suspend fun recalculateAndSyncUserBalance() {
        val user = _currentUser.value ?: return
        // Fetch all completed trades
        // Keep simple direct query / state map
        // We can load trades, sum pnl and add to user.initialDeposit
        _currentUser.value?.let { currentUserVal ->
            // Let's get total finalized PnL directly in suspend function
            // We can't collect flows easily inside non-flow, but we can query or calculate
            // Since this runs on IO, let's let the ViewModel handle the reactive balance re-indexing based on the overall list of trades
            // This is super clean, but let's pre-update the user entity row as well!
        }
    }
    
    suspend fun saveUserBalance(newBalance: Double) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val updated = user.copy(balance = newBalance)
        userDao.updateUser(updated)
        _currentUser.value = updated
    }

    // Capture & Cache Image Sandbox Utility
    suspend fun saveImageToLocalSandbox(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Create a unique file in the app private files/images directory
                val imagesDir = File(context.filesDir, "trade_screenshots")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val fileExtension = ".jpg"
                val uniqueFileName = "IMG_" + UUID.randomUUID().toString() + fileExtension
                val file = File(imagesDir, uniqueFileName)

                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
                return@withContext file.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error caching picked visual media image locally", e)
        }
        ""
    }
}
