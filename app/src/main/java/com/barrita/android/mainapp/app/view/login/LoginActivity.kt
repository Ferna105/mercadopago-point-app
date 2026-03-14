package com.barrita.android.mainapp.app.view.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barrita.android.mainapp.app.R
import com.barrita.android.mainapp.app.data.NetworkDependencyProvider
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.data.dto.LoginRequest
import com.barrita.android.mainapp.app.data.dto.LoginResponse
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityLoginBinding
import com.barrita.android.mainapp.app.view.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityLoginBinding? = null
    private val authService = NetworkDependencyProvider.authService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn(this)) {
            navigateToHome()
            return
        }

        binding = PointMainappDemoAppActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding?.pointMainappDemoAppLoginButton?.setOnClickListener {
            val email = binding?.pointMainappDemoAppLoginUsername?.text?.toString()?.trim() ?: ""
            val password = binding?.pointMainappDemoAppLoginPassword?.text?.toString() ?: ""

            if (email.isBlank() || password.isBlank()) {
                showError(getString(R.string.point_mainapp_demo_app_login_error))
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        setLoading(true)
        binding?.pointMainappDemoAppLoginError?.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = authService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        onLoginSuccess(loginResponse)
                    } ?: showError(getString(R.string.point_mainapp_demo_app_login_error))
                } else {
                    showError(getString(R.string.point_mainapp_demo_app_login_error))
                }
            } catch (e: Exception) {
                showError(getString(R.string.point_mainapp_demo_app_login_error))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun onLoginSuccess(loginResponse: LoginResponse) {
        val accessToken = loginResponse.accessToken
        val refreshToken = loginResponse.refreshToken

        if (accessToken != null && refreshToken != null) {
            SessionManager.saveSession(
                context = this,
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = loginResponse.user
            )
            navigateToHome()
        } else {
            showError(getString(R.string.point_mainapp_demo_app_login_error))
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        binding?.pointMainappDemoAppLoginError?.visibility = View.VISIBLE
        binding?.pointMainappDemoAppLoginError?.text = message
    }

    private fun setLoading(loading: Boolean) {
        binding?.pointMainappDemoAppLoginButton?.isEnabled = !loading
    }
}
