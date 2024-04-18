package com.plcoding.bluetoothchat.Hepler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USERNAME TEXT, $COL_PASSWORD TEXT, $COL_SCORE INTEGER)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }


    fun addUserRanking(username: String, score: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_USERNAME, username)
        contentValues.put(COL_SCORE, score)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    @SuppressLint("Range")
    fun getRankingData(): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COL_SCORE DESC", null)
        val stringBuilder = StringBuilder()
        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndex(COL_USERNAME))
            val score = cursor.getInt(cursor.getColumnIndex(COL_SCORE))
            stringBuilder.append("$username: $score\n")
        }
        cursor.close()
        db.close()
        return stringBuilder.toString()
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COL_USERNAME=? AND $COL_PASSWORD=?", arrayOf(username, password))
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }

    fun addUser(username: String, password: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_USERNAME, username)
        contentValues.put(COL_PASSWORD, password)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "LeaderboardDB"
        private const val TABLE_NAME = "leaderboard"
        private const val COL_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_SCORE = "score"
        private const val COL_PASSWORD = "password"
    }
}
