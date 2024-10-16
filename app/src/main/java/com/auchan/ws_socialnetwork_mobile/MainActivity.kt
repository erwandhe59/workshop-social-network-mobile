package com.auchan.ws_socialnetwork_mobile

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.auchan.account_ui.LoginFragment
import com.auchan.account_ui.di.authModule
import com.google.firebase.FirebaseApp
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Initializing FirebaseApp...")
        FirebaseApp.initializeApp(this)

        Log.d(TAG, "onCreate: Setting content view...")
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: Starting Koin...")
        startKoin {
            modules(listOf(authModule))
        }

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: No savedInstanceState, replacing fragment...")
            supportFragmentManager.commit {
                replace(R.id.main_container, LoginFragment())
            }
        }
    }
}