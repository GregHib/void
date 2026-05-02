package world.gregs.voidps.tools.search.screen.view.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.content_copy
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.copyToClipboard

@Composable
fun CopyButton(text: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BgCard)
            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
            .clickable { copyToClipboard(text) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(painterResource(Res.drawable.content_copy), null, tint = TextSecond, modifier = Modifier.size(12.dp))
        Text(label, fontSize = 11.sp, color = TextSecond)
    }
}
