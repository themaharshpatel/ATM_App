package dev.maharshpatel.atmapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Transaction History")
data class TransactionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId:Int,
    val noteValue: Int,
    val notesQuantity: Int
)
