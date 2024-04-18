package com.plcoding.bluetoothchat.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plcoding.bluetoothchat.Hepler.DatabaseHelper
import com.plcoding.bluetoothchat.R

class RegisterActivity:AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val btnRegister = findViewById<Button>(R.id.bt_btnRegister)

        // Initialize dbHelper
        dbHelper = DatabaseHelper(this)
        btnRegister.setOnClickListener {
            val username = findViewById<EditText>(R.id.edit_re_account).text.toString()
            val password = findViewById<EditText>(R.id.edit_re_password).text.toString()
            val password2 = findViewById<EditText>(R.id.edit_re2_password).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                dbHelper.addUser(username, password)
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
                val intent=Intent(this@RegisterActivity,EnterActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
            }
        }

    }


}