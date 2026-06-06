package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM typing_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<ScoreHistory>>

    @Query("SELECT * FROM typing_scores WHERE mode = :mode ORDER BY scorePoints DESC, wpm DESC LIMIT 5")
    fun getHighScoresForMode(mode: String): Flow<List<ScoreHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreHistory)

    @Query("DELETE FROM typing_scores")
    suspend fun clearAllScores()
}
