package com.example.mybatagor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnTodoList: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        btnTodoList = findViewById(R.id.btnTodoList)
        btnLogout = findViewById(R.id.btnLogout)

        // pindah ke halaman to-do list
        btnTodoList.setOnClickListener {
            startActivity(Intent(this, TodoListActivity::class.java))
        }

        // logout
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
