package world.gregs.voidps.tools.search.screen.view.table.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextSecond

@Composable
fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
            .border(0.5.dp, if (selected) AccentBlue.copy(alpha = 0.6f) else BorderColor, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) { Text(label, fontSize = 10.sp, color = if (selected) AccentLight else TextSecond) }
}