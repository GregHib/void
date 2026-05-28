package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.propertyTypeLabel
import kotlin.reflect.KProperty1

@Composable
fun BlockDetailRow(
    prop: KProperty1<Definition, *>,
    raw: Any?,
    link: FieldLink?,
    item: Definition,
    canLink: Boolean,
    faded: Boolean,
    fieldLinks: List<FieldLink>,
    onNavigate: (String, Map<String, String>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(BgCard.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .border(0.5.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Key as a small header label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                prop.name,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = when {
                    canLink -> LinkColor
                    faded -> TextMuted
                    else -> TextSecond
                },
                letterSpacing = 0.5.sp,
            )
            // Type badge
            val typeStr = propertyTypeLabel(prop)
            Box(
                Modifier
                    .background(TagBg, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                val text = when (raw) {
                    is ByteArray -> "${typeStr}[${raw.size}]"
                    is ShortArray -> "${typeStr}[${raw.size}]"
                    is IntArray -> "${typeStr}[${raw.size}]"
                    is Array<*> -> "${typeStr}[${raw.size}]"
                    is Map<*, *> -> "${typeStr}[${raw.size}]"
                    else -> typeStr
                }
                Text(text, fontSize = 11.sp, color = AccentLight.copy(alpha = 0.7f))
            }
        }

        // Value — full width
        when {
            prop.name == "params" && raw is Map<*, *> -> ParamsDetail(raw, fieldLinks, prop.name, onNavigate, item)
            raw is IntArray -> detailedArray(raw.asIterable(), link, item, onNavigate)
            raw is ShortArray -> detailedArray(raw.asIterable(), link, item, onNavigate)
            raw is ByteArray -> detailedArray(raw.asIterable(), link, item, onNavigate)
            raw is Array<*> -> StringArrayDetail(raw, link, item, onNavigate)
            else -> Text(displayValue(raw), fontSize = 12.sp, color = TextPrimary)
        }
    }
}

@Composable
internal fun detailedArray(raw: Iterable<Number>, link: FieldLink?, item: Definition, onNavigate: (String, Map<String, String>) -> Unit) {
    IntArrayDetail(
        arr = raw,
        link = link,
        sourceDef = item,
        onNavigate = if (link != null) onNavigate else null,
    )
}