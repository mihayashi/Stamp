package jp.gogo_south.stamp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicator(currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(4.dp)
                    .clip(CircleShape) // 丸くするための設定
                    .background(if (index + 1 == currentPage) Color.White else Color.Gray)
            )
        }
    }
}
