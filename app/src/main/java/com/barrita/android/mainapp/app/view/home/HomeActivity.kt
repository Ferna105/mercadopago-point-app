package com.barrita.android.mainapp.app.view.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barrita.android.mainapp.app.data.SessionManager
import com.barrita.android.mainapp.app.databinding.PointMainappDemoAppActivityHomeBinding
import com.barrita.android.mainapp.app.view.login.LoginActivity

class HomeActivity : AppCompatActivity() {

    private val binding: PointMainappDemoAppActivityHomeBinding by lazy {
        PointMainappDemoAppActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (SessionManager.getAccessToken(this) == null) {
            redirectToLogin()
            return
        }
        setupUserInfo()
        setupButtons()
    }

    private fun setupUserInfo() {
        val userEmail = SessionManager.getUserEmail(this)
        binding.pointMainappDemoAppUserEmail.text = userEmail ?: ""
    }

    private fun setupButtons() {
        binding.pointMainappDemoAppBtnMyStores.setOnClickListener {
            startActivity(Intent(this, MyStoresActivity::class.java))
        }
        binding.pointMainappDemoAppBtnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun redirectToLogin() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
