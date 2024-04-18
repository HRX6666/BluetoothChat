package com.plcoding.bluetoothchat.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.plcoding.bluetoothchat.R

class FirstActivity :AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        val tv_play:TextView=findViewById(R.id.tv_play)
        tv_play.setOnClickListener {
            val intent=Intent(this@FirstActivity,EnterActivity::class.java)
            startActivity(intent)
        }
    }
}