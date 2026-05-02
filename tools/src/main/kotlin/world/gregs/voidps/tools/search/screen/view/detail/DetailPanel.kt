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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
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
import world.gregs.voidps.tools.search.screen.view.table.filter.SearchField
import kotlin.collections.forEach
import kotlin.reflect.KProperty1

@Composable
fun DetailPanel(
    item: Definition,
    properties: List<KProperty1<Definition, *>>,
    fieldLinks: List<FieldLink>,
    onNavigate: (targetLabel: String, filters: Map<String, String>) -> Unit,
) {
    var fieldSearch by remember { mutableStateOf("") }
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

        // Search bar for fields
        SearchField(
            value = fieldSearch,
            onValueChange = { fieldSearch = it },
            placeholder = "Find field…",
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        )

        val visibleProperties = remember(fieldSearch, properties) {
            if (fieldSearch.isBlank()) properties
            else properties.filter { prop ->
                prop.name.contains(fieldSearch, ignoreCase = true)
            }
        }

        visibleProperties.forEach { prop ->
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
            val layout = layoutFor(raw)
            when (layout) {
                DetailLayout.Inline -> InlineDetailRow(
                    prop = prop,
                    raw = raw,
                    faded = faded,
                    link = link,
                    rawInt = rawInt,
                    canLink = canLink,
                    item = item,
                    onNavigate = onNavigate,
                )
                DetailLayout.Block -> BlockDetailRow(
                    prop = prop,
                    raw = raw,
                    link = link,
                    item = item,
                    canLink = canLink,
                    faded = faded,
                    fieldLinks = fieldLinks,
                    onNavigate = onNavigate,
                )
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

