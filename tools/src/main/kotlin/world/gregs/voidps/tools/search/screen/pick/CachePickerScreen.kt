package world.gregs.voidps.tools.search.screen.pick

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BgDark
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

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