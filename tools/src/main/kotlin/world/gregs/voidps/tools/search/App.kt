package world.gregs.voidps.tools.search

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.search.screen.AppScreen
import world.gregs.voidps.tools.search.screen.pick.CachePickerScreen
import world.gregs.voidps.tools.search.screen.view.DefinitionBrowser
import world.gregs.voidps.tools.search.screen.view.detail.ParamLookup
import world.gregs.voidps.tools.search.screen.view.tab.DefinitionTab
import world.gregs.voidps.tools.search.screen.view.tab.buildTabs

fun main() = application {
    Settings.load()
    Settings.rebase("../")
    ParamLookup.load()

    /*
        Improvements:
            - Filter nested array fields
            - Scroll to top/bottom button
            - Fix filtering by null not working
            - Select/Copy from details panel
            - Support for enum replacements e.g. component.type/contentType, anims.replayMode etc...
            - Reverse lookup, e.g. all npcs with render emote X
     */
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
                    onReload = { currentPath?.let { tryLoad(it) } },
                    onChangePath = { screen = AppScreen.PICKER }
                )
            }
        }
    }
}
