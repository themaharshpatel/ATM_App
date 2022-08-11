package dev.maharshpatel.atmapp

import android.app.Application
import dev.maharshpatel.atmapp.database.MainDatabase
import dev.maharshpatel.atmapp.models.ATMBalance
import dev.maharshpatel.atmapp.models.SessionInfo
import dev.maharshpatel.atmapp.models.TransactionHistory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlin.math.min

class Repository(application: Application, private val coroutineScope: CoroutineScope) {

    private val atmDao = MainDatabase.getDatabase(application).getATMDao()

    suspend fun getATMCurrency() = flow {
        atmDao.getATMCurrencyFlow().collect {
            emit(Pair(transformList(it), atmDao.getTotalMoney()))
        }
    }

    suspend fun getTransactionHistory() = flow {
        atmDao.getSessions().collect {
            val transactions = mutableListOf<Deferred<Pair<Map<Int, Int>, Int>>>()
            for (session in it) {
                transactions.add(coroutineScope.async(Dispatchers.IO) {
                    Pair(
                        transformTransactionList(atmDao.getTransaction(session.id)),
                        atmDao.getTotalMoneyInTransaction(session.id)
                    )
                })

            }
            emit(transactions.awaitAll())
        }
    }


    suspend fun withdraw(_amount: Int): Result<Pair<Map<Int, Int>, Int>> {
        val availableNotes = atmDao.getATMCurrency()
        val availableNotesMap =
            transformList(availableNotes).toSortedMap(compareByDescending() { it }).toMap()

        var amount = _amount

        val notesUsed = buildMap {
            for ((noteValue, availableQuantity) in availableNotesMap) {
                val requiredNotes = min(amount / noteValue, availableQuantity)
                amount -= requiredNotes * noteValue
                put(noteValue, requiredNotes)
            }
        }
        if (amount == _amount)
            return Result.failure(InsufficientNotesException())

        coroutineScope.launch(Dispatchers.IO) {
            for (noteCount in availableNotes) {
                launch(Dispatchers.IO) {
                    atmDao.loadMoneyToATM(
                        noteCount.copy(
                            notesQuantity = noteCount.notesQuantity - notesUsed.getOrDefault(
                                noteCount.noteValue,
                                0
                            )
                        )
                    )
                }
            }
            val sessionId = atmDao.addSession(
                SessionInfo(
                    amountWithdrawn = _amount - amount
                )
            )
            val transactionHistoryList = mutableListOf<TransactionHistory>()
            for ((noteValue, usedQuantity) in notesUsed) {
                transactionHistoryList.add(
                    TransactionHistory(
                        sessionId = sessionId.toInt(),
                        noteValue = noteValue,
                        notesQuantity = usedQuantity
                    )
                )
            }
            launch {
                atmDao.addTransactionHistory(transactionHistoryList)
            }
        }
        return Result.success(Pair(notesUsed, _amount - amount))
    }

    private fun transformList(atmBalance: List<ATMBalance>) = buildMap {
        for (currencyValue in atmBalance) {
            put(currencyValue.noteValue, currencyValue.notesQuantity)
        }
    }

    private fun transformTransactionList(atmBalance: List<TransactionHistory>) = buildMap {
        for (currencyValue in atmBalance) {
            put(currencyValue.noteValue, currencyValue.notesQuantity)
        }
    }


}