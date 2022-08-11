package dev.maharshpatel.atmapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ATMBalance")
data class ATMBalance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteValue: Int,
    val notesQuantity: Int
)
