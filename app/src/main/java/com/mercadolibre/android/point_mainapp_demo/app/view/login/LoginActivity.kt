package com.mercadolibre.android.point_mainapp_demo.app.view.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mercadolibre.android.point_mainapp_demo.app.R
import com.mercadolibre.android.point_mainapp_demo.app.databinding.PointMainappDemoAppActivityLoginBinding
import com.mercadolibre.android.point_mainapp_demo.app.view.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private var binding: PointMainappDemoAppActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PointMainappDemoAppActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding?.pointMainappDemoAppLoginButton?.setOnClickListener {
            val username = binding?.pointMainappDemoAppLoginUsername?.text?.toString()?.trim() ?: ""
            val password = binding?.pointMainappDemoAppLoginPassword?.text?.toString() ?: ""

            if (username == LOGIN_USER && password == LOGIN_PASSWORD) {
                binding?.pointMainappDemoAppLoginError?.visibility = View.GONE
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                binding?.pointMainappDemoAppLoginError?.visibility = View.VISIBLE
                binding?.pointMainappDemoAppLoginError?.text = getString(R.string.point_mainapp_demo_app_login_error)
            }
        }
    }

    companion object {
        private const val LOGIN_USER = "admin"
        private const val LOGIN_PASSWORD = "admin"
    }
}
