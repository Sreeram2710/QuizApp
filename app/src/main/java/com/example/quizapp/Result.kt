package com.example.quizapp

data class Result(
    val id: Int,
    val email: String,
    val level: String,
    val score: Int,
    val total: Int,
    val timestamp: String
)