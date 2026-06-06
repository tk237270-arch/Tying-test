package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {

    val allScores: Flow<List<ScoreHistory>> = scoreDao.getAllScores()

    fun getHighScoresForMode(mode: String): Flow<List<ScoreHistory>> {
        return scoreDao.getHighScoresForMode(mode)
    }

    suspend fun insertScore(score: ScoreHistory) {
        scoreDao.insertScore(score)
    }

    suspend fun clearHistory() {
        scoreDao.clearAllScores()
    }
}
