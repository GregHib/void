package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.ParamKey
import world.gregs.voidps.tools.search.ParamVal
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TagText
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.screen.view.resolveDisplayName

@Composable
fun ParamsDetail(
    map: Map<*, *>,
    fieldLinks: List<FieldLink>,
    fieldName: String,
    onNavigate: (String, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        map.entries.forEach { (k, v) ->
            val keyInt = k as? Int
            val paramName = keyInt?.let { ParamLookup.of(it) }
            // Check if this param value is itself a link target
            val link = fieldLinks.find { it.fieldName == fieldName }
            val valueInt = when (v) {
                is Int -> v
                is Number -> v.toInt()
                else -> v.toString().toIntOrNull()
            }
            val canLinkValue = link != null && valueInt != null && valueInt != -1

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (paramName != null) {
                    Box(Modifier.background(ParamKey.copy(alpha = 0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text(paramName, fontSize = 11.sp, color = ParamKey)
                    }
                    Text("($keyInt)", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                } else {
                    Box(Modifier.background(TagBg, RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text(k.toString(), fontSize = 11.sp, color = TagText, fontFamily = FontFamily.Monospace)
                    }
                }
                Text("=", fontSize = 11.sp, color = TextMuted)
                if (canLinkValue) {
                    // Clickable value
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { onNavigate(link.targetTabLabel, valueInt) }
                    ) {
                        Text(v.toString(), fontSize = 12.sp, color = LinkColor, fontFamily = FontFamily.Monospace)
                        val resolved = resolveDisplayName(link.targetTabLabel, valueInt)
                        if (resolved != null) {
                            Text("($resolved)", fontSize = 10.sp, color = TextSecond)
                        }
                        Icon(painterResource(Res.drawable.open_in_new), null, tint = LinkColor.copy(0.55f), modifier = Modifier.size(10.dp))
                    }
                } else {
                    Text(v.toString(), fontSize = 12.sp, color = ParamVal)
                }
            }
        }
    }
}