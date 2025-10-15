package com.example.kubhubsystem_gp13_dam.model

enum class ErrorType {
    USERNAME,
    PASSWORD,
    BOTH
}

data class UserError(
    val type: ErrorType,
    val message: String
)