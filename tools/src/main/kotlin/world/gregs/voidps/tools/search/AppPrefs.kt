package world.gregs.voidps.tools.search

import java.util.prefs.Preferences

object AppPrefs {
    private val prefs = Preferences.userRoot().node("world/gregs/voidps/tools/search/definition-browser")
    private const val KEY_CACHE_DIR = "cacheDir"

    var cacheDir: String?
        get() = prefs.get(KEY_CACHE_DIR, null)
        set(value) {
            if (value != null) prefs.put(KEY_CACHE_DIR, value) else prefs.remove(KEY_CACHE_DIR)
        }
}