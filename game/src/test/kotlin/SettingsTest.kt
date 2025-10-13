import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SettingsTest {
    @Test
    fun `Assert important properties are active`() {
        Settings.load()
        assertTrue(Settings["world.npcs.randomWalk", false])
        assertTrue(Settings["world.npcs.aggression", false])
        assertTrue(Settings["world.npcs.collision", false])
        assertEquals(10, Settings["bots.count", 0])
    }
}
