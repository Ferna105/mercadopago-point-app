package com.barrita.android.mainapp.app.view.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn(this)) {
            navigateToHome()
            return
        }

        binding = PointMainappDemoAppActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupLoginButton()
        setupPasswordToggle()
    }

    private fun setupLoginButton() {
        binding?.pointMainappDemoAppLoginButton?.setOnClickListener {
            val email = binding?.pointMainappDemoAppLoginUsername?.text?.toString()?.trim() ?: ""
            val password = binding?.pointMainappDemoAppLoginPassword?.text?.toString() ?: ""

            if (email.isBlank() || password.isBlank()) {
                showError(getString(R.string.point_mainapp_demo_app_login_fields_required))
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    private fun setupPasswordToggle() {
        binding?.pointMainappDemoAppLoginTogglePassword?.setOnClickListener {
            passwordVisible = !passwordVisible
            val passwordField = binding?.pointMainappDemoAppLoginPassword ?: return@setOnClickListener
            if (passwordVisible) {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding?.pointMainappDemoAppLoginTogglePassword?.setImageResource(R.drawable.point_mainapp_demo_app_ic_eye_off)
            } else {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding?.pointMainappDemoAppLoginTogglePassword?.setImageResource(R.drawable.point_mainapp_demo_app_ic_eye)
            }
            passwordField.setSelection(passwordField.text?.length ?: 0)
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
                    } ?: run {
                        showError(getString(R.string.point_mainapp_demo_app_login_error))
                        setLoading(false)
                    }
                } else {
                    showError(getString(R.string.point_mainapp_demo_app_login_error))
                    setLoading(false)
                }
            } catch (e: Exception) {
                showError(getString(R.string.point_mainapp_demo_app_login_error))
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
            setLoading(false)
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
        binding?.pointMainappDemoAppLoginButton?.text =
            if (loading) getString(R.string.point_mainapp_demo_app_login_loading)
            else getString(R.string.point_mainapp_demo_app_login_submit)
        binding?.pointMainappDemoAppLoginUsername?.isEnabled = !loading
        binding?.pointMainappDemoAppLoginPassword?.isEnabled = !loading
    }
}
