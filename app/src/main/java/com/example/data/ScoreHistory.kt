package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "typing_scores")
data class ScoreHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String,          // "aesthetic" | "racer" | "laser"
    val wpm: Double,
    val accuracy: Double,
    val timeDurationSeconds: Int,
    val scorePoints: Int,       // custom points earned
    val timestamp: Long = System.currentTimeMillis()
)
