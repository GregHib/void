package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.open_in_new
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.SuccessGreen
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TagText
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.screen.view.resolveDisplayName
import world.gregs.voidps.tools.search.screen.view.resolveNavigationFilters
import kotlin.reflect.KProperty1

@Composable
fun InlineDetailRow(
    prop: KProperty1<Definition, *>,
    raw: Any?,
    faded: Boolean,
    link: FieldLink?,
    rawInt: Int?,
    canLink: Boolean,
    item: Definition,
    onNavigate: (String, Map<String, String>) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Key — natural width, never wraps
        Text(
            text = prop.name,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            softWrap = false,
            color = when {
                canLink -> LinkColor
                faded -> TextMuted
                else -> TextSecond
            },
            modifier = if (canLink)
                Modifier.clickable { onNavigate(link!!.targetTabLabel, resolveNavigationFilters(link, rawInt!!, item)) }
            else Modifier,
        )

        // Subtle separator
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(BorderColor.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        // Value — right aligned so values form a ragged-right column naturally
        Box(contentAlignment = Alignment.CenterEnd) {
            InlineValue(raw, faded, canLink, link, rawInt, item, onNavigate)
        }
    }
}

@Composable
fun InlineValue(
    raw: Any?,
    faded: Boolean,
    canLink: Boolean,
    link: FieldLink?,
    rawInt: Int?,
    item: Definition,
    onNavigate: (String, Map<String, String>) -> Unit,
) {
    when {
        raw is Boolean -> Box(
            Modifier
                .background(
                    if (raw) SuccessGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.1f),
                    RoundedCornerShape(3.dp)
                )
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(raw.toString(), fontSize = 11.sp, color = if (raw) SuccessGreen else TextMuted)
        }
        raw is IntArray -> smallArray(raw.asIterable())
        raw is ShortArray -> smallArray(raw.asIterable())
        raw is ByteArray -> smallArray(raw.asIterable())
        canLink -> {
            val resolved = resolveDisplayName(link!!.targetTabLabel, rawInt!!, link)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onNavigate(link.targetTabLabel, resolveNavigationFilters(link, rawInt, item)) }
            ) {
                if (resolved != null) {
                    Text(resolved, fontSize = 11.sp, color = TextSecond)
                }
                Text(rawInt.toString(), fontSize = 12.sp, color = LinkColor, fontFamily = FontFamily.Monospace)
                Icon(painterResource(Res.drawable.open_in_new), null, tint = LinkColor.copy(0.55f), modifier = Modifier.size(10.dp))
            }
        }

        else -> Text(
            displayValue(raw),
            fontSize = 12.sp,
            color = if (faded) TextMuted else TextPrimary,
            fontFamily = if (raw is Number) FontFamily.Monospace else FontFamily.Default,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun smallArray(raw: Iterable<Number>) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        raw.forEach { v ->
            Box(
                Modifier
                    .background(TagBg, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(v.toString(), fontSize = 11.sp, color = TagText, fontFamily = FontFamily.Monospace)
            }
        }
    }
}