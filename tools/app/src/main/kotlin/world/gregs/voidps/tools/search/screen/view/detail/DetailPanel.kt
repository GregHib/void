package world.gregs.voidps.tools.search.screen.view.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.screen.view.table.CopyButton
import world.gregs.voidps.tools.search.screen.view.table.filter.SearchField
import kotlin.reflect.KProperty1

@Composable
fun DetailPanel(
    item: Definition,
    properties: List<KProperty1<Definition, *>>,
    fieldLinks: List<FieldLink>,
    onNavigate: (targetLabel: String, filters: Map<String, String>) -> Unit,
) {
    var fieldSearch by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp),
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

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
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

