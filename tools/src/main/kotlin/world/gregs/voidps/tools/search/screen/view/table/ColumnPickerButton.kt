package world.gregs.voidps.tools.search.screen.view.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.view_column
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond

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