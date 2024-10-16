package com.auchan.account_business

interface AuthManager {
    fun login(email: String, password: String, onResult: (Boolean) -> Unit)
}