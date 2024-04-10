package com.plcoding.bluetoothchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.bluetoothchat.MostlyActivity

/**
 * 排行榜组件
 */

data class LeaderboardItem(val rank: Int, val username: String, val score: Double)

@Composable
fun Leaderboard(leaderboardItems: List<MostlyActivity.LeaderboardItem>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color= Color(0xFFFFFFFF))
    ){
        LazyColumn (
            modifier = Modifier.fillMaxSize()
        ){
            items(leaderboardItems) { item ->
                LeaderboardItemRow(item)
            }
        }
    }

}

@Composable
fun LeaderboardItemRow(item: MostlyActivity.LeaderboardItem) {
    val textColor = when {
        item.rank==1->Color(0xFFC53E14)
        item.rank==2->Color(0xFFD6691F)
        item.rank==3->Color(0xFFDABF39)
        else -> Color(0xFF3D3D3D)
    }
    val backgroundColor=when{
        item.rank<=3->Color(0x1AF7F7F7)
        else-> Color(0x1AD3D3D3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.rank.toString(), style = TextStyle(fontSize = 20.sp, color = textColor))
        Spacer(modifier = Modifier.width(35.dp))
        Text(text = item.username, style = TextStyle(fontSize = 20.sp, color = textColor))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = item.score.toString(), style = TextStyle(fontSize = 20.sp, color = textColor))
    }
}

@Preview
@Composable
fun LeaderboardPreview() {
//    val sampleData = listOf(
//        LeaderboardItem(1, "User1", 100),
//        LeaderboardItem(2, "User2", 90),
//        LeaderboardItem(3, "User3", 80),
//        LeaderboardItem(4, "User4", 70),
//        LeaderboardItem(5, "User5", 60),
//        LeaderboardItem(6, "User5", 60),
//        LeaderboardItem(7, "User5", 60),
//        LeaderboardItem(8, "User5", 60),
//        LeaderboardItem(9, "User5", 60),
//        LeaderboardItem(10, "User5", 60),
//        LeaderboardItem(11, "User5", 60)
//    )
    MaterialTheme {
      //  Leaderboard(leaderboardItems = sampleData)
    }
}
