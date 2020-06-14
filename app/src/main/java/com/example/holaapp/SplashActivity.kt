package com.example.holaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        updateUI(user)
    }
    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
        else{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
