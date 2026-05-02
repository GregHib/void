package world.gregs.voidps.tools

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.*
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.*
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.UIManager
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

// ─────────────────────────────────────────────────────────────────────────────
// Cross-tab link — clicking a field in the detail panel navigates to another tab
// ─────────────────────────────────────────────────────────────────────────────

data class FieldLink(
    val fieldName: String,       // field on source, e.g. "runSound"
    val targetTabLabel: String,  // label of destination tab, e.g. "Sounds"
)

// ─────────────────────────────────────────────────────────────────────────────
// Tab descriptor  (async loader keeps tab order stable)
// ─────────────────────────────────────────────────────────────────────────────

data class DefinitionTab<T : Definition>(
    val label: String,
    val clazz: Class<T>,
    val defaultColumns: List<String>,
    val fieldLinks: List<FieldLink> = emptyList(),
    val loader: suspend () -> List<T>,
)

// ─────────────────────────────────────────────────────────────────────────────
// Per-tab mutable state — survives tab switches (lives outside the composable)
// ─────────────────────────────────────────────────────────────────────────────

class TabState(
    val label: String,
    val clazz: Class<out Definition>,
    val defaultColumns: List<String>,
    val fieldLinks: List<FieldLink>,
) {
    var definitions: List<Definition> by mutableStateOf(emptyList())
    var loading: Boolean by mutableStateOf(true)
    var error: String? by mutableStateOf(null)
    var visibleColumns: List<String> by mutableStateOf(defaultColumns)
    var columnFilters: Map<String, FieldFilter> by mutableStateOf(emptyMap())
    var selectedItem: Definition? by mutableStateOf(null)
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter model
// ─────────────────────────────────────────────────────────────────────────────

enum class MatchMode { CONTAINS, EXACT, GREATER_THAN, LESS_THAN, HAS_VALUE, PARAM_KEY, PARAM_VALUE }

data class FieldFilter(
    val fieldName: String,
    val value: String = "",
    val mode: MatchMode = MatchMode.CONTAINS,
)

// ─────────────────────────────────────────────────────────────────────────────
// Reflection helpers
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("UNCHECKED_CAST")
fun <T : Any> getProperties(clazz: Class<T>): List<KProperty1<T, *>> = clazz.declaredFields
    .mapNotNull { it.kotlinProperty as? KProperty1<T, *> }

fun propertyTypeLabel(prop: KProperty1<*, *>): String =
    prop.returnType.toString()
        .replace("kotlin.", "").replace("?", "")
        .substringAfterLast('.')

fun displayValue(value: Any?, resolveParams: Boolean = false): String = when (value) {
    null -> "null"
    is Array<*> -> value.joinToString(", ") { it?.toString() ?: "null" }
    is IntArray -> value.joinToString(", ")
    is ShortArray -> value.joinToString(", ")
    is Map<*, *> -> if (resolveParams) {
        value.entries.joinToString(", ") { (k, v) ->
            val name = (k as? Int)?.let { nameOfParam(it) } ?: k.toString()
            "$name=$v"
        }
    } else value.entries.joinToString(", ") { "${it.key}=${it.value}" }
    else -> value.toString()
}

fun matchesFilter(rawValue: Any?, filter: FieldFilter): Boolean {
    if (filter.value.isBlank()) return true
    val query = filter.value.trim()
    return when (filter.mode) {
        MatchMode.CONTAINS -> displayValue(rawValue).contains(query, ignoreCase = true)
        MatchMode.EXACT -> displayValue(rawValue).equals(query, ignoreCase = true)
        MatchMode.GREATER_THAN -> query.toLongOrNull()?.let { n ->
            when (rawValue) {
                is Number -> rawValue.toLong() > n; is IntArray -> rawValue.any { it > n }; else -> false
            }
        } ?: false
        MatchMode.LESS_THAN -> query.toLongOrNull()?.let { n ->
            when (rawValue) {
                is Number -> rawValue.toLong() < n; is IntArray -> rawValue.any { it < n }; else -> false
            }
        } ?: false
        MatchMode.HAS_VALUE -> when (rawValue) {
            is IntArray -> rawValue.any { it.toString().contains(query, ignoreCase = true) }
            is Array<*> -> rawValue.any { it?.toString()?.contains(query, ignoreCase = true) == true }
            is Map<*, *> -> rawValue.keys.any { it.toString().contains(query, ignoreCase = true) } ||
                    rawValue.values.any { it.toString().contains(query, ignoreCase = true) }
            else -> displayValue(rawValue).contains(query, ignoreCase = true)
        }
        MatchMode.PARAM_KEY -> {
            if (rawValue !is Map<*, *>) return false
            val queryId = Params.id(query).takeIf { it != -1 } ?: query.toIntOrNull()
            rawValue.keys.any { k ->
                k.toString() == queryId?.toString() ||
                        (k is Int && nameOfParam(k)?.contains(query, ignoreCase = true) == true)
            }
        }
        MatchMode.PARAM_VALUE -> {
            if (rawValue !is Map<*, *>) return false
            rawValue.values.any { it.toString().contains(query, ignoreCase = true) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clipboard
// ─────────────────────────────────────────────────────────────────────────────

fun copyToClipboard(text: String) =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)

// ─────────────────────────────────────────────────────────────────────────────
// Theme
// ─────────────────────────────────────────────────────────────────────────────

private val BgDark = Color(0xFF0F1117)
private val BgPanel = Color(0xFF161B27)
private val BgCard = Color(0xFF1E2535)
private val BgSelected = Color(0xFF1A3A5C)
private val AccentBlue = Color(0xFF4A9EFF)
private val AccentLight = Color(0xFF7BBEFF)
private val TextPrimary = Color(0xFFE8EDF5)
private val TextSecond = Color(0xFF8A95A8)
private val TextMuted = Color(0xFF4A5568)
private val BorderColor = Color(0xFF2A3347)
private val TagBg = Color(0xFF1A2840)
private val TagText = Color(0xFF5BA3E8)
private val SuccessGreen = Color(0xFF3DD68C)
private val WarnAmber = Color(0xFFFFB547)
private val ParamKey = Color(0xFFBF94E4)
private val ParamVal = Color(0xFFE5C07B)
private val LinkColor = Color(0xFF56B6C2)

// ─────────────────────────────────────────────────────────────────────────────
// UI primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(28.dp)
            .background(BgCard, RoundedCornerShape(4.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 12.sp)
        BasicTextField(
            value, onValueChange, singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
            cursorBrush = SolidColor(AccentBlue), modifier = Modifier.fillMaxWidth()
        )
    }
}

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

// ─────────────────────────────────────────────────────────────────────────────
// Column header  (click → dropdown filter + remove column)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RowScope.ColumnHeader(
    fieldName: String,
    typeStr: String,
    filter: FieldFilter?,
    onFilterChange: (FieldFilter?) -> Unit,
    onRemoveColumn: () -> Unit,
    weight: Float,
) {
    var showDropdown by remember { mutableStateOf(false) }
    val hasFilter = filter != null && filter.value.isNotBlank()

    val isParams = fieldName == "params"
    val isArray = typeStr.contains("Array") || typeStr.contains("IntArray")
    val isNumeric = listOf("Int", "Long", "Double", "Float", "Byte", "Short").any { typeStr.contains(it) }

    val modes: List<MatchMode> = when {
        isParams -> listOf(MatchMode.PARAM_KEY, MatchMode.PARAM_VALUE, MatchMode.CONTAINS)
        isArray -> listOf(MatchMode.HAS_VALUE, MatchMode.CONTAINS)
        isNumeric -> listOf(MatchMode.EXACT, MatchMode.GREATER_THAN, MatchMode.LESS_THAN, MatchMode.CONTAINS)
        else -> listOf(MatchMode.CONTAINS, MatchMode.EXACT)
    }

    Box(modifier = Modifier.weight(weight)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .clickable { showDropdown = true }
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                fieldName, fontSize = 11.sp,
                color = if (hasFilter) AccentBlue else TextSecond,
                fontWeight = if (hasFilter) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (hasFilter) Box(Modifier.size(6.dp).background(AccentBlue, RoundedCornerShape(3.dp)))
            Icon(painterResource(Res.drawable.arrow_drop_down), null, tint = if (hasFilter) AccentBlue else TextMuted, modifier = Modifier.size(14.dp))
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.width(230.dp).background(BgPanel).border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
        ) {
            Column(Modifier.padding(10.dp)) {
                // Header row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(TagBg, RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text(fieldName, fontSize = 11.sp, color = TagText, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(typeStr, fontSize = 10.sp, color = TextMuted)
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))

                val currentMode = filter?.mode ?: modes.first()
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    modes.forEach { m ->
                        ModeChip(
                            label = when (m) {
                                MatchMode.CONTAINS -> "contains"
                                MatchMode.EXACT -> "exact"
                                MatchMode.GREATER_THAN -> ">"
                                MatchMode.LESS_THAN -> "<"
                                MatchMode.HAS_VALUE -> "has"
                                MatchMode.PARAM_KEY -> "key"
                                MatchMode.PARAM_VALUE -> "value"
                            },
                            selected = currentMode == m,
                            onClick = { onFilterChange((filter ?: FieldFilter(fieldName)).copy(mode = m)) },
                        )
                    }
                }

                if (isParams && currentMode == MatchMode.PARAM_KEY) {
                    Spacer(Modifier.height(4.dp))
                    Text("name (e.g. AKA) or numeric id", fontSize = 10.sp, color = TextMuted)
                }
                Spacer(Modifier.height(6.dp))

                SearchField(
                    value = filter?.value ?: "",
                    onValueChange = { v ->
                        if (v.isEmpty()) onFilterChange(null)
                        else onFilterChange(FieldFilter(fieldName, v, currentMode))
                    },
                    placeholder = "filter…",
                    modifier = Modifier.fillMaxWidth(),
                )

                if (hasFilter) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "clear filter", fontSize = 10.sp, color = AccentBlue,
                        modifier = Modifier.clickable { onFilterChange(null) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Column picker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ColumnPickerButton(allFields: List<String>, visibleColumns: List<String>, onToggle: (String, Boolean) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(BgCard)
                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                .clickable { show = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painterResource(Res.drawable.view_column), null, tint = TextSecond, modifier = Modifier.size(14.dp))
            Text("Columns", fontSize = 12.sp, color = TextSecond)
        }
        DropdownMenu(
            expanded = show,
            onDismissRequest = { show = false },
            modifier = Modifier.width(200.dp).background(BgPanel).border(0.5.dp, BorderColor),
        ) {
            Text("Toggle columns", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 6.dp))
            Column(modifier = Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState()).padding(8.dp)) {
                allFields.forEach { field ->
                    val checked = field in visibleColumns
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp))
                            .clickable { onToggle(field, !checked) }.padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked, { onToggle(field, it) }, modifier = Modifier.size(16.dp),
                            colors = CheckboxDefaults.colors(checkedColor = AccentBlue, uncheckedColor = TextMuted, checkmarkColor = Color.White)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(field, fontSize = 12.sp, color = if (checked) TextPrimary else TextSecond, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Result row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ResultRow(
    item: Definition,
    selected: Boolean,
    columns: List<KProperty1<Definition, *>>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) BgSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { idx, prop ->
            val raw = try {
                prop.get(item)
            } catch (_: Exception) {
                null
            }
            val isParams = prop.name == "params"
            val txt = displayValue(raw, resolveParams = isParams)
            val isId = prop.name == "id"
            Box(modifier = Modifier.weight(if (isId) 0.5f else 1f)) {
                Text(
                    txt, fontSize = 12.sp, maxLines = 1,
                    color = when {
                        isId -> AccentBlue; txt == "null" || txt == "-1" -> TextMuted; else -> TextPrimary
                    },
                    fontFamily = if (isId) FontFamily.Monospace else FontFamily.Default
                )
            }
            if (idx < columns.lastIndex) Spacer(Modifier.width(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ParamsDetail(map: Map<*, *>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        map.entries.forEach { (k, v) ->
            val keyInt = k as? Int
            val paramName = keyInt?.let { nameOfParam(it) }
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
                Text(v.toString(), fontSize = 12.sp, color = ParamVal)
            }
        }
    }
}

@Composable
fun IntArrayDetail(arr: IntArray) {
    Column {
        Text("IntArray[${arr.size}]", fontSize = 10.sp, color = WarnAmber.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            arr.take(20).forEach { v ->
                Box(Modifier.background(TagBg, RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                    Text(v.toString(), fontSize = 11.sp, color = TagText, fontFamily = FontFamily.Monospace)
                }
            }
            if (arr.size > 20) Text("…+${arr.size - 20}", fontSize = 11.sp, color = TextMuted)
        }
    }
}

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

// ─────────────────────────────────────────────────────────────────────────────
// Detail panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetailPanel(
    item: Definition,
    properties: List<KProperty1<Definition, *>>,
    fieldLinks: List<FieldLink>,
    onNavigate: (targetLabel: String, filterId: Int) -> Unit,
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
            val nameVal = properties.find { it.name == "name" }?.let {
                try {
                    it.get(item)?.toString()
                } catch (_: Exception) {
                    null
                }
            } ?: ""
            Text(nameVal, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        // Clipboard buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            CopyButton(item.id.toString(), "Copy ID")
            if (item is Parameterized && item.stringId.isNotBlank()) CopyButton(item.stringId, "Copy string ID")
        }

        Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 10.dp))

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
                // Field name — clickable if linkable
                Text(
                    prop.name, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    color = when {
                        canLink -> LinkColor; faded -> TextMuted; else -> TextSecond
                    },
                    modifier = Modifier.weight(0.35f).then(
                        if (canLink) Modifier.clickable { onNavigate(link.targetTabLabel, rawInt) } else Modifier
                    ))
                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(0.65f)) {
                    when {
                        prop.name == "params" && raw is Map<*, *> -> ParamsDetail(raw)
                        raw is Boolean -> Box(
                            Modifier.background(
                                if (raw) SuccessGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.1f),
                                RoundedCornerShape(3.dp)
                            ).padding(horizontal = 6.dp, vertical = 1.dp)
                        ) { Text(raw.toString(), fontSize = 11.sp, color = if (raw) SuccessGreen else TextMuted) }
                        raw is IntArray -> IntArrayDetail(raw)
                        raw is Array<*> -> StringArrayDetail(raw)
                        canLink -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(raw.toString(), fontSize = 12.sp, color = LinkColor, fontFamily = FontFamily.Monospace)
                            Icon(painterResource(Res.drawable.open_in_new), null, tint = LinkColor.copy(alpha = 0.55f), modifier = Modifier.size(11.dp))
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

// ─────────────────────────────────────────────────────────────────────────────
// Tab content
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("UNCHECKED_CAST")
@Composable
fun DefinitionTabContent(state: TabState, onNavigate: (String, Int) -> Unit) {
    val clazz = state.clazz as Class<Definition>
    val properties: List<KProperty1<Definition, *>> = remember(clazz) { getProperties(clazz) }
    val allFieldNames = remember(clazz) { properties.map { it.name } }
    val propsByName = remember(clazz) { properties.associateBy { it.name } }

    // Loading / error states
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(28.dp))
                Text("Loading ${state.label}…", fontSize = 13.sp, color = TextSecond)
            }
        }
        return
    }
    if (state.error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${state.error}", color = Color(0xFFE06C75), fontSize = 13.sp)
        }
        return
    }

    val visibleProps: List<KProperty1<Definition, *>> = remember(state.visibleColumns, propsByName) {
        state.visibleColumns.mapNotNull { propsByName[it] }
    }

    val filteredResults: List<Definition> = remember(state.definitions, state.columnFilters) {
        state.definitions.filter { def ->
            state.columnFilters.values.all { f ->
                if (f.value.isBlank()) return@all true
                val raw = try {
                    propsByName[f.fieldName]?.get(def)
                } catch (_: Exception) {
                    null
                }
                matchesFilter(raw, f)
            }
        }
    }

    val activeFilters = state.columnFilters.values.count { it.value.isNotBlank() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = maxWidth  // 'maxWidth' here is the BoxWithConstraints receiver
        val minPanelWidth = totalWidth * 0.20f
        val maxPanelWidth = totalWidth * 0.80f

        var detailPanelFraction by remember { mutableStateOf(0.30f) }
        val detailPanelWidth = totalWidth * detailPanelFraction

        // Clamp in case window is resized
        val clampedWidth = detailPanelWidth.coerceIn(minPanelWidth, maxPanelWidth)

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Table ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(BgDark)) {

                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().background(BgPanel).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "${filteredResults.size} / ${state.definitions.size}", fontSize = 12.sp,
                        color = if (activeFilters > 0) AccentBlue else TextSecond, fontWeight = FontWeight.Medium
                    )
                    Text("results", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.weight(1f))
                    if (activeFilters > 0) {
                        Text(
                            "Clear $activeFilters filter${if (activeFilters > 1) "s" else ""}", fontSize = 11.sp, color = AccentBlue,
                            modifier = Modifier.clickable { state.columnFilters = emptyMap() })
                    }
                    ColumnPickerButton(
                        allFields = allFieldNames,
                        visibleColumns = state.visibleColumns,
                        onToggle = { field, show ->
                            state.visibleColumns = if (show) allFieldNames.filter { it in state.visibleColumns || it == field }
                            else state.visibleColumns.filter { it != field }
                        },
                    )
                }

                Divider(color = BorderColor, thickness = 0.5.dp)

                // Column headers
                Row(
                    modifier = Modifier.fillMaxWidth().background(BgPanel).padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    visibleProps.forEachIndexed { idx, prop ->
                        ColumnHeader(
                            fieldName = prop.name,
                            typeStr = propertyTypeLabel(prop),
                            filter = state.columnFilters[prop.name],
                            onFilterChange = { updated ->
                                state.columnFilters = if (updated == null) state.columnFilters - prop.name
                                else state.columnFilters + (prop.name to updated)
                            },
                            onRemoveColumn = { state.visibleColumns = state.visibleColumns.filter { it != prop.name } },
                            weight = if (prop.name == "id") 0.5f else 1f,
                        )
                        if (idx < visibleProps.lastIndex) Spacer(Modifier.width(8.dp))
                    }
                }

                Divider(color = BorderColor, thickness = 0.5.dp)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredResults) { item ->
                        ResultRow(item, state.selectedItem == item, visibleProps) {
                            state.selectedItem = if (state.selectedItem == item) null else item
                        }
                        Divider(color = BorderColor.copy(alpha = 0.35f), thickness = 0.5.dp)
                    }
                    if (filteredResults.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (activeFilters > 0) "No results match current filters" else "No definitions loaded",
                                    color = TextMuted, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Drag handle ───────────────────────────────────────────────────
            state.selectedItem?.let {
                var isDragging by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(if (isDragging) AccentBlue.copy(alpha = 0.4f) else Color.Transparent)
                        .hoverable(remember { MutableInteractionSource() })
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Press -> isDragging = true
                                        PointerEventType.Release -> isDragging = false
                                        PointerEventType.Move -> if (isDragging) {
                                            val change = event.changes.first()
                                            change.consume()
                                            val dragDelta = change.position.x - change.previousPosition.x
                                            val deltaDp = dragDelta.toDp()
                                            // Read fraction directly — no stale captured Dp value
                                            val newWidth = (totalWidth * detailPanelFraction - deltaDp)
                                                .coerceIn(minPanelWidth, maxPanelWidth)
                                            detailPanelFraction = (newWidth / totalWidth)
                                                .coerceIn(0.20f, 0.80f)
                                        }
                                    }
                                }
                            }
                        }
                        .pointerHoverIcon(PointerIcon.Hand), // shows resize cursor on hover
                    contentAlignment = Alignment.Center,
                ) {
                    // Subtle grip dots
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            Box(Modifier.size(2.dp).background(if (isDragging) AccentBlue else BorderColor, RoundedCornerShape(1.dp)))
                        }
                    }
                }

                // ── Detail panel ──────────────────────────────────────────────────
                state.selectedItem?.let { item ->
                    Column(
                        modifier = Modifier
                            .width(clampedWidth)
                            .fillMaxHeight()
                            .background(BgPanel)
                            .border(BorderStroke(0.5.dp, BorderColor)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Detail", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painterResource(Res.drawable.close), null, tint = TextSecond,
                                modifier = Modifier.size(16.dp).clickable { state.selectedItem = null })
                        }
                        Divider(color = BorderColor, thickness = 0.5.dp)
                        DetailPanel(item, properties, state.fieldLinks, onNavigate)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Browser shell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DefinitionBrowser(tabs: List<DefinitionTab<*>>) {
    var selectedIdx by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Persistent state per tab — not recreated on tab switch
    val tabStates = remember(tabs) {
        tabs.map { tab ->
            TabState(tab.label, tab.clazz, tab.defaultColumns, tab.fieldLinks)
        }
    }

    // Kick off async loaders once
    LaunchedEffect(tabStates) {
        tabStates.forEachIndexed { idx, state ->
            scope.launch {
                try {
                    @Suppress("UNCHECKED_CAST")
                    state.definitions = (tabs[idx] as DefinitionTab<Definition>).loader()
                } catch (e: Exception) {
                    e.printStackTrace()
                    state.error = e.message ?: "Unknown error"
                } finally {
                    state.loading = false
                }
            }
        }
    }

    // Cross-tab navigation: switch to target tab and inject an id=X filter
    fun navigateTo(targetLabel: String, filterId: Int) {
        val idx = tabStates.indexOfFirst { it.label == targetLabel }
        if (idx == -1) return
        selectedIdx = idx
        tabStates[idx].apply {
            columnFilters = columnFilters + ("id" to FieldFilter("id", filterId.toString(), MatchMode.EXACT))
            if ("id" !in visibleColumns) visibleColumns = listOf("id") + visibleColumns
        }
    }

    MaterialTheme(colors = darkColors(background = BgDark, surface = BgPanel, primary = AccentBlue)) {
        Column(modifier = Modifier.fillMaxSize().background(BgDark)) {

            // Tab bar
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp)
                    .background(BgPanel).border(BorderStroke(0.5.dp, BorderColor)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabStates.forEachIndexed { idx, state ->
                    val isSelected = idx == selectedIdx
                    val hasFilters = state.columnFilters.values.any { it.value.isNotBlank() }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { selectedIdx = idx }
                            .background(if (isSelected) BgDark else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(
                                    BorderStroke(2.dp, AccentBlue),
                                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                ) else Modifier
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                state.label, fontSize = 13.sp,
                                color = if (isSelected) TextPrimary else TextSecond,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                            when {
                                state.loading -> CircularProgressIndicator(
                                    color = TextMuted,
                                    modifier = Modifier.size(8.dp), strokeWidth = 1.5.dp
                                )
                                hasFilters -> Box(Modifier.size(6.dp).background(AccentBlue, RoundedCornerShape(3.dp)))
                            }
                        }
                    }
                }
            }

            // Content — no key{} reset: state persists across tab switches
            DefinitionTabContent(
                state = tabStates[selectedIdx],
                onNavigate = ::navigateTo,
            )
        }
    }
}

@Composable
fun CachePickerScreen(
    initialPath: String?,
    error: String?,
    onDirectorySelected: (String) -> Unit,
) {
    // Immediately open the native directory chooser
    fun openChooser() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select cache directory"
            initialPath?.let { currentDirectory = File(it) }
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            onDirectorySelected(chooser.selectedFile.absolutePath)
        }
    }

    Box(Modifier.fillMaxSize().background(BgDark), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Definition Browser", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Select a cache directory to begin", fontSize = 13.sp, color = TextSecond)

            Spacer(Modifier.height(8.dp))

            // Show last used path as a hint
            if (initialPath != null) {
                Box(
                    Modifier
                        .background(BgCard, RoundedCornerShape(6.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(initialPath, fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                }
            }

            // ── Error banner ──────────────────────────────────────────────
            if (error != null) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .background(Color(0xFF3D1A1A), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF7A2E2E), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("✕", fontSize = 12.sp, color = Color(0xFFE06C75))
                    Text(error, fontSize = 12.sp, color = Color(0xFFE06C75), lineHeight = 18.sp)
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AccentBlue)
                    .clickable { openChooser() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (initialPath != null) "Change directory" else "Load cache directory",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private enum class AppScreen { PICKER, BROWSER }

object AppPrefs {
    private val prefs = Preferences.userRoot().node("world/gregs/voidps/tools/definition-browser")
    private const val KEY_CACHE_DIR = "cacheDir"

    var cacheDir: String?
        get() = prefs.get(KEY_CACHE_DIR, null)
        set(value) {
            if (value != null) prefs.put(KEY_CACHE_DIR, value) else prefs.remove(KEY_CACHE_DIR)
        }
}

private fun buildTabs(path: String): Result<List<DefinitionTab<*>>> = runCatching {
    val file = File(path)
    val cachePath: String
    if (file.resolve("cache").exists()) {
        cachePath = file.resolve("cache").absolutePath
    } else if (file.resolve("main_file_cache.dat2").exists()) {
        cachePath = path
    } else {
        error("No cache found in dir: '$path'")
    }
    var loadConfig = false
    val files = if (file.resolve("dirs.txt").exists()) {
        loadConfig = true
        configFiles(path, "${path}/.temp/modified.dat")
    } else {
        configFiles()
    }
    val cache = CacheDelegate(cachePath)
    listOf(
        DefinitionTab("Items", ItemDefinition::class.java, listOf("id", "stringId", "name")) {
            ItemDefinitions.init(ItemDecoder().load(cache))
            if (loadConfig) {
                ItemDefinitions.load(files.list(Settings["definitions.items"]))
            }
            ItemDefinitions.definitions.toList()
        },
        DefinitionTab(
            "NPCs", NPCDefinition::class.java, listOf("id", "stringId", "name"),
            listOf(
                FieldLink("renderEmote", "Emotes"),
                FieldLink("idleSound", "Sounds"),
                FieldLink("crawlSound", "Sounds"),
                FieldLink("walkSound", "Sounds"),
                FieldLink("runSound", "Sounds"),
                FieldLink("transforms", "NPCs"),
            )
        ) {
            NPCDefinitions.init(NPCDecoder(true).load(cache))
            if (loadConfig) {
                NPCDefinitions.load(files.getValue(Settings["definitions.npcs"]))
            }
            NPCDefinitions.definitions.toList()
        },
        DefinitionTab("Objects", ObjectDefinition::class.java, listOf("id", "stringId", "name", "varbit", "varp")) {
            ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache))
            if (loadConfig) {
                ObjectDefinitions.load(files.getValue(Settings["definitions.objects"]))
            }
            ObjectDefinitions.definitions.toList()
        },
        DefinitionTab("Anims", AnimationDefinition::class.java, listOf("id", "stringId", "priority")) {
            AnimationDefinitions.init(AnimationDecoder().load(cache))
            if (loadConfig) {
                AnimationDefinitions.load(files.getValue(Settings["definitions.animations"]))
            }
            AnimationDefinitions.definitions.toList()
        },
        DefinitionTab("Emotes", RenderAnimationDefinition::class.java, listOf("id", "primaryIdle", "primaryWalk", "run")) {
            RenderAnimationDecoder().load(cache).toList()
        },
        DefinitionTab("Gfx", GraphicDefinition::class.java, listOf("id", "stringId")) {
            GraphicDefinitions.init(GraphicDecoder().load(cache))
            if (loadConfig) {
                GraphicDefinitions.load(files.list(Settings["definitions.graphics"]))
            }
            GraphicDefinitions.definitions.toList()
        },
        DefinitionTab("Sounds", SoundDefinition::class.java, listOf("id", "stringId")) {
            if (loadConfig) {
                SoundDefinitions().load(files.list(Settings["definitions.sounds"])).definitions.toList()
            } else {
                emptyList()
            }
        },
        DefinitionTab("Ifaces", InterfaceDefinition::class.java, listOf("id", "stringId")) {
            // TODO support Components
            InterfaceDefinitions.init(InterfaceDecoder().load(cache))
            if (loadConfig) {
                InterfaceDefinitions.load(files.list(Settings["definitions.interfaces"]), files.find(Settings["definitions.interfaces.types"]))
            }
            InterfaceDefinitions.definitions.toList()
        },
        DefinitionTab("Enums", EnumDefinition::class.java, listOf("id", "stringId")) {
            EnumDefinitions.init(EnumDecoder().load(cache))
            if (loadConfig) {
//                EnumDefinitions.load(files.list(Settings["definitions.enums"]))
            }
            EnumDefinitions.definitions.toList()
        },
        DefinitionTab("Invs", InventoryDefinition::class.java, listOf("id", "stringId")) {
            InventoryDefinitions.init(InventoryDecoder().load(cache))
            if (loadConfig) {
                InventoryDefinitions.load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"]))
            }
            InventoryDefinitions.definitions.toList()
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — wire up your actual loaded arrays here
// ─────────────────────────────────────────────────────────────────────────────

private val paramLookup = mutableMapOf<Int, String>()

private fun nameOfParam(id: Int?): String? = paramLookup[id]

fun main() = application {
    Settings.load()
    Settings.rebase("../")

    val params = getProperties(Params::class.java)
    params.filter { it.isConst }.forEach {
        paramLookup[it.getter.call() as Int] = it.name.lowercase()
    }
    var screen by remember { mutableStateOf(AppScreen.PICKER) }
    var tabs by remember { mutableStateOf<List<DefinitionTab<*>>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }

    fun tryLoad(path: String) {
        buildTabs(path)
            .onSuccess { result ->
                loadError = null
                AppPrefs.cacheDir = path
                tabs = result
                screen = AppScreen.BROWSER
            }
            .onFailure { e ->
                loadError = e.message ?: "Failed to load cache"
                // Don't persist a path that didn't work
            }
    }

    // If we have a saved path, skip straight to the browser
    LaunchedEffect(Unit) {
        AppPrefs.cacheDir?.let { tryLoad(it) }
    }
    val state = rememberWindowState(
        width = 1000.dp,
        height = 600.dp,
    )
    Window(onCloseRequest = ::exitApplication, title = "Definition Browser", state = state) {
        MaterialTheme(colors = darkColors(background = BgDark, surface = BgPanel, primary = AccentBlue)) {
            when (screen) {
                AppScreen.PICKER -> CachePickerScreen(
                    initialPath = AppPrefs.cacheDir,
                    error = loadError,
                    onDirectorySelected = { tryLoad(it) }
                )
                AppScreen.BROWSER -> DefinitionBrowser(tabs = tabs)
            }
        }
    }
}
