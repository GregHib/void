package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary

@Composable
fun StringArrayDetail(arr: Array<*>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        arr.forEachIndexed { i, v ->
            if (v != null) Row {
                Text("[$i]", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(22.dp))
                Text(v.toString(), fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}