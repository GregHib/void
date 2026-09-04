package content.entity.player

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.entity.World
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoSaveTest : WorldTest() {

    @Test
    fun `Disabling auto save at runtime clears the queue`() {
        settings["storage.autoSave.minutes"] = "5"
        SettingsReload.now()
        assertTrue(World.containsQueue("auto_save"), "Auto save wasn't queued")

        settings["storage.autoSave.minutes"] = "0"
        SettingsReload.now()

        assertFalse(World.containsQueue("auto_save"), "Disabling auto save left the queue running")
    }
}
