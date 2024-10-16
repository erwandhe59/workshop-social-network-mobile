package com.auchan.account_ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.auchan.account_business.AuthManager
import com.auchan.home_ui.HomeFragment
import org.koin.android.ext.android.inject

class LoginFragment : Fragment() {

    private val TAG = "LoginFragment"

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private val authManager: AuthManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Inflating fragment layout...")
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: Initializing UI components...")
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvRegister = view.findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            Log.d(TAG, "onViewCreated: Email: $email, Password: $password")

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: Logging in with credentials...")
                authManager.login(email, password) { isSuccess ->
                    if (isSuccess) {
                        Log.d(TAG, "onViewCreated: Login successful!")
                        parentFragmentManager.commit {
                            replace(com.auchan.account_ui.R.id.main_container, HomeFragment())
                            addToBackStack(null)
                        }
                    } else {
                        Log.e(TAG, "onViewCreated: Login failed")
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e(TAG, "onViewCreated: Email or password is empty")
                Toast.makeText(requireContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}