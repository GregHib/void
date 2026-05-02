package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.open_in_new
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TagText
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.WarnAmber
import world.gregs.voidps.tools.search.screen.view.resolveDisplayName

@Composable
fun IntArrayDetail(
    arr: IntArray,
    /** If set, each element is clickable and navigates to this tab */
    linkTargetTab: String? = null,
    onNavigate: ((String, Int) -> Unit)? = null,
) {
    Column {
        Text("IntArray[${arr.size}]", fontSize = 10.sp, color = WarnAmber.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 3.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            arr.forEach { v ->
                val canLink = linkTargetTab != null && onNavigate != null && v != -1
                val resolved = if (canLink) resolveDisplayName(linkTargetTab, v) else null
                Box(
                    Modifier
                        .background(if (canLink) LinkColor.copy(alpha = 0.1f) else TagBg, RoundedCornerShape(3.dp))
                        .border(0.5.dp, if (canLink) LinkColor.copy(alpha = 0.4f) else BorderColor, RoundedCornerShape(3.dp))
                        .then(if (canLink) Modifier.clickable { onNavigate(linkTargetTab, v) } else Modifier)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(v.toString(), fontSize = 11.sp, color = if (canLink) LinkColor else TagText, fontFamily = FontFamily.Monospace)
                        if (resolved != null) {
                            Text(resolved, fontSize = 10.sp, color = TextSecond)
                        }
                        if (canLink) {
                            Icon(painterResource(Res.drawable.open_in_new), null, tint = LinkColor.copy(0.45f), modifier = Modifier.size(9.dp))
                        }
                    }
                }
            }
        }
    }
}
