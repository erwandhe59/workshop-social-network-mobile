package com.auchan.account_business

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthManager : AuthManager {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "FirebaseAuthManager"

    override fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        Log.d(TAG, "login: Attempting login for email: $email")

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "login: SignIn successful for email: $email")
                    onResult(true)
                } else {
                    Log.e(TAG, "login: SignIn failed for email: $email, Error: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }
}