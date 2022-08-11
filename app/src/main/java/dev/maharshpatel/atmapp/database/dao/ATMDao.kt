package dev.maharshpatel.atmapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.maharshpatel.atmapp.models.ATMBalance
import dev.maharshpatel.atmapp.models.SessionInfo
import dev.maharshpatel.atmapp.models.TransactionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ATMDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun loadMoneyToATM(atmBalance: ATMBalance)

    @Query("select * from ATMBalance")
    fun getATMCurrencyFlow(): Flow<List<ATMBalance>>

    @Query("select * from ATMBalance")
    suspend fun getATMCurrency(): List<ATMBalance>

    @Query("select sum(noteValue * notesQuantity) from ATMBalance")
    suspend fun getTotalMoney(): Int

    @Insert
    suspend fun addSession(sessionInfo: SessionInfo): Long

    @Insert
    suspend fun addTransactionHistory(transactionHistory: List<TransactionHistory>)

    @Query("select * from Session order by id desc")
    fun getSessions(): Flow<List<SessionInfo>>

    @Query("select * from `Transaction History` where sessionId = :sessionId")
    suspend fun getTransaction(sessionId: Int): List<TransactionHistory>

    @Query("select sum(noteValue * notesQuantity) from `Transaction History` where sessionId = :sessionId")
    suspend fun getTotalMoneyInTransaction(sessionId: Int): Int

}