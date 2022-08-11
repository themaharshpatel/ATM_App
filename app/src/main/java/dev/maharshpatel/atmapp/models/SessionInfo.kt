package dev.maharshpatel.atmapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Session")
data class SessionInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountWithdrawn: Int
)
