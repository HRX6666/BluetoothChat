package com.plcoding.bluetoothchat.Main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plcoding.bluetoothchat.Hepler.DatabaseHelper
import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.di.UserManager

class EnterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)
        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("login_status", Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // 如果用户已经登录，则直接跳转到主界面
            val username = sharedPreferences.getString("username", "")
            if (!username.isNullOrEmpty()) {
                UserManager.loggedInUsername = username
                val intent = Intent(this, MainUserActivity::class.java)
                startActivity(intent)
                finish() // 关闭当前 Activity
                return
            }
        }

        val btnLogin = findViewById<Button>(R.id.bt_enter)
        val enroll_bt = findViewById<TextView>(R.id.tv_enroll_intent)

        btnLogin.setOnClickListener {
            val username = findViewById<EditText>(R.id.edit_enter_account).text.toString()
            val password = findViewById<EditText>(R.id.edit_enter_password).text.toString()
            val isValidUser = dbHelper.checkUser(username, password)
            if (isValidUser) {
                // 登录成功
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                UserManager.loggedInUsername = username

                // 保存登录状态
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("username", username)
                editor.apply()

                val intent = Intent(this, MainUserActivity::class.java)
                startActivity(intent)
                finish() // 关闭当前 Activity
            } else {
                // 登录失败
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
            }
        }

        enroll_bt.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

