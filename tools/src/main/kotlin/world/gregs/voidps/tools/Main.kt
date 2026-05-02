package world.gregs.voidps.tools

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
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
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.kotlinProperty

// ─────────────────────────────────────────────────────────────────────────────
// Cross-tab link — clicking a field in the detail panel navigates to another tab
// ─────────────────────────────────────────────────────────────────────────────

data class FieldLink(
    val fieldName: String,       // field on source, e.g. "runSound"
    val targetTabLabel: String,  // label of destination tab, e.g. "Sounds"
)

// ─────────────────────────────────────────────────────────────────────────────
// Tab descriptor — dependencies allow ordering async loaders
// ─────────────────────────────────────────────────────────────────────────────

data class DefinitionTab<T : Definition>(
    val label: String,
    val clazz: Class<T>,
    val defaultColumns: List<String>,
    val fieldLinks: List<FieldLink> = emptyList(),
    /** Labels of tabs that must finish loading before this tab starts */
    val dependsOn: List<String> = emptyList(),
    val loader: suspend () -> List<T>,
)

// ─────────────────────────────────────────────────────────────────────────────
// Per-tab mutable state
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
    var selectedItems: List<Definition> by mutableStateOf(emptyList())   // multi-select
    var lastClickedIndex: Int by mutableStateOf(-1)                      // for shift-range
}

// Convenience: single selected item (last in selection)
val TabState.selectedItem: Definition? get() = selectedItems.lastOrNull()

// ─────────────────────────────────────────────────────────────────────────────
// Filter model
// ─────────────────────────────────────────────────────────────────────────────

enum class MatchMode {
    CONTAINS, EXACT, GREATER_THAN, LESS_THAN, HAS_VALUE, PARAM_KEY, PARAM_VALUE, NOT_NULL, NOT_EMPTY
}

data class FieldFilter(
    val fieldName: String,
    val value: String = "",
    val mode: MatchMode = MatchMode.CONTAINS,
)

// ─────────────────────────────────────────────────────────────────────────────
// Reverse-lookup: tabLabel -> (id -> Definition) for resolving IDs to names
// ─────────────────────────────────────────────────────────────────────────────

/** Set once after all tabs finish loading; used by detail panel for name resolution. */
val tabDefinitionIndex: MutableMap<String, Map<Int, Definition>> = mutableMapOf()

fun resolveDefinition(tabLabel: String, id: Int): Definition? =
    tabDefinitionIndex[tabLabel]?.get(id)

fun resolveDisplayName(tabLabel: String, id: Int): String? {
    val def = resolveDefinition(tabLabel, id) ?: return null
    // Try "name" then "stringId" fields via reflection
    return def.javaClass.declaredFields
        .mapNotNull { it.kotlinProperty }
        .firstOrNull { it.name == "name" || it.name == "stringId" }
        ?.let {
            @Suppress("UNCHECKED_CAST")
            (it as? KProperty1<Any, *>)?.get(def)?.toString()?.takeIf { s -> s.isNotBlank() && s != "null" }
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reflection helpers
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("UNCHECKED_CAST")
fun <T : Any> getProperties(clazz: Class<T>): List<KProperty1<T, *>> {
    val companionProperties = clazz.kotlin.companionObject?.declaredMemberProperties?.toSet() ?: emptySet()
    return clazz.declaredFields
        .mapNotNull { it.kotlinProperty as? KProperty1<T, *> }
        .filter { !companionProperties.contains(it) }
}

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
        MatchMode.NOT_NULL -> rawValue != null && rawValue.toString() != "null" && rawValue.toString() != "-1"
        MatchMode.NOT_EMPTY -> when (rawValue) {
            null -> false
            is String -> rawValue.isNotBlank()
            is Array<*> -> rawValue.isNotEmpty()
            is IntArray -> rawValue.isNotEmpty()
            is Map<*, *> -> rawValue.isNotEmpty()
            else -> rawValue.toString().let { it != "null" && it != "-1" && it.isNotBlank() }
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
private val BgMultiSelected = Color(0xFF122840)
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
// Column header
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
        isParams -> listOf(MatchMode.PARAM_KEY, MatchMode.PARAM_VALUE, MatchMode.CONTAINS, MatchMode.NOT_EMPTY)
        isArray -> listOf(MatchMode.HAS_VALUE, MatchMode.CONTAINS, MatchMode.NOT_EMPTY)
        isNumeric -> listOf(MatchMode.EXACT, MatchMode.GREATER_THAN, MatchMode.LESS_THAN, MatchMode.CONTAINS, MatchMode.NOT_NULL)
        else -> listOf(MatchMode.CONTAINS, MatchMode.EXACT, MatchMode.NOT_EMPTY)
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
                val noInputNeeded = currentMode == MatchMode.NOT_NULL || currentMode == MatchMode.NOT_EMPTY
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
                                MatchMode.NOT_NULL -> "not null"
                                MatchMode.NOT_EMPTY -> "not empty"
                            },
                            selected = currentMode == m,
                            onClick = {
                                val sentinel = if (m == MatchMode.NOT_NULL || m == MatchMode.NOT_EMPTY) "*" else filter?.value ?: ""
                                onFilterChange(FieldFilter(fieldName, sentinel, m))
                            },
                        )
                    }
                }

                if (isParams && currentMode == MatchMode.PARAM_KEY) {
                    Spacer(Modifier.height(4.dp))
                    Text("name (e.g. AKA) or numeric id", fontSize = 10.sp, color = TextMuted)
                }
                if (!noInputNeeded) {
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
                }

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
// Result row — right-click context menu, multi-select support
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ResultRow(
    item: Definition,
    isSelected: Boolean,
    isInMultiSelection: Boolean,
    selectedItems: List<Definition>,
    columns: List<KProperty1<Definition, *>>,
    onClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }

    val bgColor = when {
        isSelected -> BgSelected
        isInMultiSelection -> BgMultiSelected
        else -> Color.Transparent
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .pointerInput(showMenu) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press
                                && event.buttons.isSecondaryPressed
                            ) {
                                val pos = event.changes.first().position
                                menuOffset = DpOffset(pos.x.toDp(), 0.dp)  // y=0 so it anchors below the row naturally
                                showMenu = true
                                // consume only the right-click
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
                .clickable(
                    onClick = {
                        // Check shift state via pointer position isn't available here,
                        // so we use a separate hover-tracked shift state (see below)
                        onClick()
                    }
                )
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

        // Right-click context menu — one entry per visible column
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = menuOffset,
            modifier = Modifier.background(BgPanel).border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
        ) {
            val targets = if (selectedItems.size > 1) selectedItems else listOf(item)
            val isMulti = targets.size > 1
            if (isMulti) {
                Text(
                    "${targets.size} rows selected",
                    fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 4.dp)
                )
                Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
                // Copy all selected as TSV (with header)
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.content_copy), null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                            Text("Copy ${targets.size} rows (TSV)", fontSize = 11.sp, color = AccentBlue)
                        }
                    },
                    onClick = {
                        val header = columns.joinToString("\t") { it.name }
                        val rows = targets.joinToString("\n") { row ->
                            columns.joinToString("\t") { prop ->
                                displayValue(
                                    try {
                                        prop.get(row)
                                    } catch (_: Exception) {
                                        null
                                    }, prop.name == "params"
                                )
                            }
                        }
                        copyToClipboard("$header\n$rows")
                        showMenu = false
                    },
                    modifier = Modifier.height(30.dp),
                )
                // Copy a single field across all selected rows
                Text(
                    "Copy column across selection",
                    fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 4.dp)
                )
                Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
            } else {
                Text("Copy value", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 4.dp))
                Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
            }
            columns.forEach { prop ->
                val singleVal = displayValue(
                    try { prop.get(item) } catch (_: Exception) { null },
                    prop.name == "params"
                )
                val copyText = if (isMulti) {
                    targets.joinToString("\n") { row ->
                        displayValue(try { prop.get(row) } catch (_: Exception) { null }, prop.name == "params")
                    }
                } else singleVal

                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                prop.name, fontSize = 11.sp, color = TextSecond,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.widthIn(min = 60.dp)
                            )
                            Text(
                                if (isMulti) "(${targets.size} values)"
                                else singleVal.take(40) + if (singleVal.length > 40) "…" else "",
                                fontSize = 11.sp, color = if (isMulti) TextSecond else TextPrimary
                            )
                        }
                    },
                    onClick = { copyToClipboard(copyText); showMenu = false },
                    modifier = Modifier.height(30.dp),
                )
            }
            if (!isMulti) {
                Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.content_copy), null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                            Text("Copy all fields", fontSize = 11.sp, color = AccentBlue)
                        }
                    },
                    onClick = {
                        val all = columns.joinToString("\t") { prop ->
                            displayValue(try { prop.get(item) } catch (_: Exception) { null }, prop.name == "params")
                        }
                        copyToClipboard(all)
                        showMenu = false
                    },
                    modifier = Modifier.height(30.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail sub-composables
// ─────────────────────────────────────────────────────────────────────────────

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
            val paramName = keyInt?.let { nameOfParam(it) }
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
                        modifier = Modifier.clickable { onNavigate(link!!.targetTabLabel, valueInt!!) }
                    ) {
                        Text(v.toString(), fontSize = 12.sp, color = LinkColor, fontFamily = FontFamily.Monospace)
                        val resolved = resolveDisplayName(link!!.targetTabLabel, valueInt!!)
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

@Composable
fun IntArrayDetail(
    arr: IntArray,
    /** If set, each element is clickable and navigates to this tab */
    linkTargetTab: String? = null,
    onNavigate: ((String, Int) -> Unit)? = null,
) {
    Column {
        Text("IntArray[${arr.size}]", fontSize = 10.sp, color = WarnAmber.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 3.dp))
        // Improvement 8: wrap instead of truncate
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            arr.forEach { v ->
                val canLink = linkTargetTab != null && onNavigate != null && v != -1
                val resolved = if (canLink) resolveDisplayName(linkTargetTab!!, v) else null
                Box(
                    Modifier
                        .background(if (canLink) LinkColor.copy(alpha = 0.1f) else TagBg, RoundedCornerShape(3.dp))
                        .border(0.5.dp, if (canLink) LinkColor.copy(alpha = 0.4f) else BorderColor, RoundedCornerShape(3.dp))
                        .then(if (canLink) Modifier.clickable { onNavigate!!(linkTargetTab!!, v) } else Modifier)
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

@Composable
fun StringArrayDetail(arr: Array<*>) {
    // Improvement 8: wrap long arrays
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
fun resolveLabel(item: Definition, properties: List<KProperty1<Definition, *>>): String {
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
            val label = remember(item) { resolveLabel(item, properties) }
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
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
                // Field name — clickable if linkable (single int)
                Text(
                    prop.name, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    color = when {
                        canLink -> LinkColor; faded -> TextMuted; else -> TextSecond
                    },
                    modifier = Modifier.weight(0.35f).then(
                        if (canLink) Modifier.clickable { onNavigate(link!!.targetTabLabel, rawInt) } else Modifier
                    ))
                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(0.65f)) {
                    when {
                        prop.name == "params" && raw is Map<*, *> ->
                            ParamsDetail(raw, fieldLinks, prop.name, onNavigate)

                        raw is Boolean -> Box(
                            Modifier.background(
                                if (raw) SuccessGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.1f),
                                RoundedCornerShape(3.dp)
                            ).padding(horizontal = 6.dp, vertical = 1.dp)
                        ) { Text(raw.toString(), fontSize = 11.sp, color = if (raw) SuccessGreen else TextMuted) }

                        raw is IntArray -> {
                            // Improvement 9: if there's a FieldLink for this array field, each element is clickable
                            IntArrayDetail(
                                arr = raw,
                                linkTargetTab = link?.targetTabLabel,
                                onNavigate = if (link != null) onNavigate else null,
                            )
                        }

                        raw is Array<*> -> StringArrayDetail(raw)

                        canLink -> {
                            // Single int with link — show id + resolved name
                            val resolved = resolveDisplayName(link!!.targetTabLabel, rawInt)
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.clickable { onNavigate(link.targetTabLabel, rawInt) }
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
    var isShiftHeld by remember { mutableStateOf(false) }

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

    val listState: LazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    // Keep focused so arrow keys work immediately
    LaunchedEffect(state.label) { focusRequester.requestFocus() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val minPanelWidth = totalWidth * 0.20f
        val maxPanelWidth = totalWidth * 0.80f
        var detailPanelFraction by remember { mutableStateOf(0.30f) }
        val clampedWidth = (totalWidth * detailPanelFraction).coerceIn(minPanelWidth, maxPanelWidth)

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Table ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BgDark)
                    // Improvement 3: arrow key navigation
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        isShiftHeld = event.isShiftPressed
                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                        if (event.isCtrlPressed && event.key == Key.C) {
                            if (state.selectedItems.isNotEmpty()) {
                                val rows = state.selectedItems.joinToString("\n") { item ->
                                    visibleProps.joinToString("\t") { prop ->
                                        displayValue(
                                            try {
                                                prop.get(item)
                                            } catch (_: Exception) {
                                                null
                                            }, prop.name == "params"
                                        )
                                    }
                                }
                                copyToClipboard(rows)
                            }
                            return@onKeyEvent true
                        }
                        val currentIndex = state.lastClickedIndex
                        val newIndex = when (event.key) {
                            Key.DirectionDown -> (currentIndex + 1).coerceAtMost(filteredResults.lastIndex)
                            Key.DirectionUp -> (currentIndex - 1).coerceAtLeast(0)
                            else -> return@onKeyEvent false
                        }
                        if (newIndex != currentIndex && filteredResults.isNotEmpty()) {
                            val item = filteredResults[newIndex]
                            if (event.isShiftPressed) {
                                // Extend selection
                                val range = if (newIndex > state.lastClickedIndex)
                                    (state.lastClickedIndex..newIndex)
                                else
                                    (newIndex..state.lastClickedIndex)
                                state.selectedItems = filteredResults.slice(range)
                            } else {
                                state.selectedItems = listOf(item)
                            }
                            state.lastClickedIndex = newIndex
                            scope.launch { listState.scrollToItem(newIndex) }
                            true
                        } else false
                    }
            ) {
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
                    if (state.selectedItems.size > 1) {
                        Text("· ${state.selectedItems.size} selected", fontSize = 12.sp, color = AccentLight)
                    }
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

                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(filteredResults) { item ->
                        val itemIndex = filteredResults.indexOf(item)
                        val isSelected = state.selectedItems.lastOrNull() == item
                        val isInMulti = item in state.selectedItems
                        ResultRow(
                            item = item,
                            isSelected = isSelected,
                            isInMultiSelection = isInMulti && !isSelected,
                            selectedItems = state.selectedItems,
                            columns = visibleProps,
                            onClick = {
                                if (isShiftHeld) {
                                    // Improvement 2: shift-click = range selection
                                    if (state.lastClickedIndex == -1) {
                                        state.selectedItems = listOf(item)
                                        state.lastClickedIndex = itemIndex
                                    } else {
                                        val lo = minOf(state.lastClickedIndex, itemIndex)
                                        val hi = maxOf(state.lastClickedIndex, itemIndex)
                                        state.selectedItems = filteredResults.slice(lo..hi)
                                    }
                                    focusRequester.requestFocus()
                                } else {
                                    // Improvement 2: normal click = single selection
                                    state.selectedItems = listOf(item)
                                    state.lastClickedIndex = itemIndex
                                    focusRequester.requestFocus()
                                }
                            },
                        )
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
                                            val newWidth = (totalWidth * detailPanelFraction - deltaDp)
                                                .coerceIn(minPanelWidth, maxPanelWidth)
                                            detailPanelFraction = (newWidth / totalWidth).coerceIn(0.20f, 0.80f)
                                        }
                                    }
                                }
                            }
                        }
                        .pointerHoverIcon(PointerIcon.Hand),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            Box(Modifier.size(2.dp).background(if (isDragging) AccentBlue else BorderColor, RoundedCornerShape(1.dp)))
                        }
                    }
                }

                // ── Detail panel ───────────────────────────────────────────────
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
                            if (state.selectedItems.size > 1) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier.background(AccentBlue.copy(0.15f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("${state.selectedItems.size} selected", fontSize = 10.sp, color = AccentLight)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painterResource(Res.drawable.close), null, tint = TextSecond,
                                modifier = Modifier.size(16.dp).clickable { state.selectedItems = emptyList() })
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
// Browser shell — Improvement 5: Reload + Load new path buttons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DefinitionBrowser(
    tabs: List<DefinitionTab<*>>,
    onReload: () -> Unit,
    onChangePath: () -> Unit,
) {
    var selectedIdx by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val tabStates = remember(tabs) {
        tabs.map { tab -> TabState(tab.label, tab.clazz, tab.defaultColumns, tab.fieldLinks) }
    }

    // Improvement 4: dependency-aware async loading
    LaunchedEffect(tabStates) {
        // Build label -> index map
        val labelToIdx = tabs.mapIndexed { i, t -> t.label to i }.toMap()
        // Track which tabs have finished
        val finished = mutableSetOf<String>()
        // We need to repeatedly poll until all are done; use a simple launch-when-ready approach
        val pending = tabs.indices.toMutableSet()

        while (pending.isNotEmpty()) {
            val toStart = pending.filter { idx ->
                val deps = tabs[idx].dependsOn
                deps.all { it in finished }
            }
            if (toStart.isEmpty()) break // circular or impossible dep — break to avoid infinite loop
            toStart.forEach { idx ->
                pending.remove(idx)
                val state = tabStates[idx]
                scope.launch {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        state.definitions = (tabs[idx] as DefinitionTab<Definition>).loader()
                        // Update reverse lookup index
                        tabDefinitionIndex[state.label] = state.definitions.associateBy { it.id }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        state.error = e.message ?: "Unknown error"
                    } finally {
                        state.loading = false
                        finished.add(tabs[idx].label)
                    }
                }
            }
            // Yield and wait for at least one to finish before re-checking deps
            // Simple approach: wait a short tick then retry
            kotlinx.coroutines.delay(50)
        }
    }

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

            // Tab bar + action buttons
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
                                    color = TextMuted, modifier = Modifier.size(8.dp), strokeWidth = 1.5.dp
                                )
                                hasFilters -> Box(Modifier.size(6.dp).background(AccentBlue, RoundedCornerShape(3.dp)))
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Improvement 5: Reload + Load new path buttons
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Reload
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgCard)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .clickable { onReload() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painterResource(Res.drawable.refresh), null, tint = TextSecond, modifier = Modifier.size(13.dp))
                        Text("Reload", fontSize = 11.sp, color = TextSecond)
                    }
                    // Load new path
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .border(0.5.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable { onChangePath() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painterResource(Res.drawable.folder_open), null, tint = AccentLight, modifier = Modifier.size(13.dp))
                        Text("Load path…", fontSize = 11.sp, color = AccentLight)
                    }
                }
            }

            DefinitionTabContent(
                state = tabStates[selectedIdx],
                onNavigate = ::navigateTo,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cache picker screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CachePickerScreen(
    initialPath: String?,
    error: String?,
    onDirectorySelected: (String) -> Unit,
) {
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
                    fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// App screen state
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// Tab builder — Improvement 4: dependsOn wiring for Invs -> Items
// ─────────────────────────────────────────────────────────────────────────────

private fun buildTabs(path: String): Result<List<DefinitionTab<*>>> = runCatching {
    val file = File(path)
    val cachePath: String = when {
        file.resolve("cache").exists() -> file.resolve("cache").absolutePath
        file.resolve("main_file_cache.dat2").exists() -> path
        else -> error("No cache found in dir: '$path'")
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
                FieldLink("transforms", "NPCs"),   // IntArray — each element clickable
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
            InterfaceDefinitions.init(InterfaceDecoder().load(cache))
            if (loadConfig) {
                InterfaceDefinitions.load(
                    files.list(Settings["definitions.interfaces"]),
                    files.find(Settings["definitions.interfaces.types"])
                )
            }
            InterfaceDefinitions.definitions.toList()
        },
        DefinitionTab("Enums", EnumDefinition::class.java, listOf("id", "stringId")) {
            EnumDefinitions.init(EnumDecoder().load(cache))
            if (loadConfig) {
                // EnumDefinitions.load(files.list(Settings["definitions.enums"]))
            }
            EnumDefinitions.definitions.toList()
        },
        // Improvement 4: Inventories depend on Items (so item names resolve in detail panel)
        DefinitionTab(
            label = "Invs",
            clazz = InventoryDefinition::class.java,
            defaultColumns = listOf("id", "stringId"),
            dependsOn = listOf("Items"),
            fieldLinks = listOf(FieldLink("ids", "Items"))
        ) {
            InventoryDefinitions.init(InventoryDecoder().load(cache))
            if (loadConfig) {
                InventoryDefinitions.load(
                    files.list(Settings["definitions.inventories"]),
                    files.list(Settings["definitions.shops"])
                )
            }
            InventoryDefinitions.definitions.toList()
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
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
    var currentPath by remember { mutableStateOf<String?>(null) }

    fun tryLoad(path: String) {
        buildTabs(path)
            .onSuccess { result ->
                loadError = null
                AppPrefs.cacheDir = path
                currentPath = path
                tabs = result
                screen = AppScreen.BROWSER
            }
            .onFailure { e ->
                loadError = e.message ?: "Failed to load cache"
            }
    }

    LaunchedEffect(Unit) {
        AppPrefs.cacheDir?.let { tryLoad(it) }
    }

    val state = rememberWindowState(width = 1000.dp, height = 600.dp)

    Window(onCloseRequest = ::exitApplication, title = "Definition Browser", state = state) {
        MaterialTheme(colors = darkColors(background = BgDark, surface = BgPanel, primary = AccentBlue)) {
            when (screen) {
                AppScreen.PICKER -> CachePickerScreen(
                    initialPath = AppPrefs.cacheDir,
                    error = loadError,
                    onDirectorySelected = { tryLoad(it) }
                )
                AppScreen.BROWSER -> DefinitionBrowser(
                    tabs = tabs,
                    // Improvement 5: Reload reloads the same path
                    onReload = { currentPath?.let { tryLoad(it) } },
                    // Improvement 5: Change path goes back to picker
                    onChangePath = { screen = AppScreen.PICKER }
                )
            }
        }
    }
}