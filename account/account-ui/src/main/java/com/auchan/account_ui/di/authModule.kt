package com.auchan.account_ui.di

import com.auchan.account_business.AuthManager
import com.auchan.account_business.FirebaseAuthManager
import org.koin.dsl.module

val authModule = module {
    single<AuthManager> { FirebaseAuthManager() }
}
