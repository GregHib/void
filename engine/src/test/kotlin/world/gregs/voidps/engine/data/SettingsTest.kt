package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SettingsTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setUp() {
        settings = Settings()
    }

    private val example = """
        server.name=Void
        network.port=43594
        world.experienceRate=10.0
        server.live=true
    """.trimIndent()

    @Test
    fun `Load settings`() {
        settings.load(example)
        assertEquals("Void", settings["server.name"])
        assertEquals(43594, settings["network.port", -1])
        assertEquals(10.0, settings["world.experienceRate", -10.0])
        assertEquals(true, settings["server.live", false])
    }

    @Test
    fun `Get missing setting throws exception settings`() {
        settings.load(example)
        assertThrows<NullPointerException> {
            assertEquals("Nothing", settings["missing"])
        }
    }

    @Test
    fun `Get a missing setting returns default`() {
        assertEquals("missing", settings["unknown", "missing"])
    }

    @Test
    fun `Get an incorrect format setting returns default`() {
        settings.load(example)
        assertEquals(1234, settings["server.name", 1234])
    }

    @Test
    fun `Override settings`() {
        settings.load(example)
        settings.load("server.name=Void 2")

        assertEquals("Void 2", settings["server.name"])
        assertEquals(43594, settings["network.port", -1])
    }

    private fun Settings.load(string: String) = load(string.byteInputStream(Charsets.UTF_8))

}