package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.open_in_new
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.screen.view.table.CopyButton
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.SuccessGreen
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.screen.view.resolveDisplayName
import world.gregs.voidps.tools.search.screen.view.resolveNavigationFilters
import kotlin.collections.forEach
import kotlin.reflect.KProperty1

@Composable
fun DetailPanel(
    item: Definition,
    properties: List<KProperty1<Definition, *>>,
    fieldLinks: List<FieldLink>,
    onNavigate: (targetLabel: String, filters: Map<String, String>) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Box(Modifier.background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(5.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("#${item.id}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentBlue, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.width(8.dp))
            val label = remember(item) { resolveLabel(item, properties) }
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        // Clipboard buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            CopyButton(item.id.toString(), "Copy ID")
            if (item is Parameterized && item.stringId.isNotBlank()) CopyButton(item.stringId, "Copy string ID")
        }

        HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 10.dp))

        properties.forEach { prop ->
            val raw = try {
                prop.get(item)
            } catch (_: Exception) {
                null
            }
            val isNull = raw == null || raw.toString() == "null"
            val isNeg1 = raw is Int && raw == -1
            val faded = isNull || isNeg1
            val link = fieldLinks.find { it.fieldName == prop.name }
            val rawInt = raw as? Int
            val canLink = link != null && rawInt != null && rawInt != -1

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                // Field name — clickable if linkable (single int)
                Text(
                    prop.name, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    color = when {
                        canLink -> LinkColor; faded -> TextMuted; else -> TextSecond
                    },
                    modifier = Modifier.weight(0.35f).then(
                        if (canLink) Modifier.clickable { onNavigate(link.targetTabLabel, resolveNavigationFilters(link, rawInt, item)) } else Modifier
                    ))
                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(0.65f)) {
                    when {
                        prop.name == "params" && raw is Map<*, *> ->
                            ParamsDetail(raw, fieldLinks, prop.name, onNavigate, item)

                        raw is Boolean -> Box(
                            Modifier.background(
                                if (raw) SuccessGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.1f),
                                RoundedCornerShape(3.dp)
                            ).padding(horizontal = 6.dp, vertical = 1.dp)
                        ) { Text(raw.toString(), fontSize = 11.sp, color = if (raw) SuccessGreen else TextMuted) }

                        raw is IntArray -> {
                            IntArrayDetail(
                                arr = raw,
                                link = link,
                                sourceDef = item,
                                onNavigate = if (link != null) onNavigate else null,
                            )
                        }

                        raw is Array<*> -> StringArrayDetail(raw)

                        canLink -> {
                            // Single int with link — show id + resolved name
                            val resolved = resolveDisplayName(link.targetTabLabel, rawInt)
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.clickable { onNavigate(link.targetTabLabel, resolveNavigationFilters(link, rawInt, item)) }
                                ) {
                                    Text(raw.toString(), fontSize = 12.sp, color = LinkColor, fontFamily = FontFamily.Monospace)
                                    Icon(painterResource(Res.drawable.open_in_new), null, tint = LinkColor.copy(alpha = 0.55f), modifier = Modifier.size(11.dp))
                                }
                                if (resolved != null) {
                                    Text(resolved, fontSize = 10.sp, color = TextSecond)
                                }
                            }
                        }

                        else -> Text(
                            displayValue(raw), fontSize = 12.sp,
                            color = if (faded) TextMuted else TextPrimary,
                            fontFamily = if (raw is Number) FontFamily.Monospace else FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

private fun resolveLabel(item: Definition, properties: List<KProperty1<Definition, *>>): String {
    fun valueOf(fieldName: String) = properties.find { it.name == fieldName }
        ?.let {
            try {
                it.get(item)?.toString()
            } catch (_: Exception) {
                null
            }
        }
        ?.takeIf { it.isNotBlank() && it != "null" }

    val stringId = valueOf("stringId")
    val name = valueOf("name")
    val idStr = item.id.toString()

    return when {
        !stringId.isNullOrBlank() && stringId != idStr -> stringId
        !name.isNullOrBlank() && name != idStr -> name
        else -> ""
    }
}

