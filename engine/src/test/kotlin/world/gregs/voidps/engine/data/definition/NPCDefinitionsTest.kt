package world.gregs.voidps.engine.data.definition

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.variable.IntValues
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NPCDefinitionsTest : DefinitionsDecoderTest<NPCDefinition, NPCDecoder, NPCDefinitions>() {

    override var decoder: NPCDecoder = NPCDecoder(member = true)
    override lateinit var definitions: Array<NPCDefinition>
    override val id: String = "hans"
    override val intId: Int = 0

    override fun expected(): NPCDefinition = NPCDefinition(
        intId,
        stringId = id,
        params = mapOf(
            Params.CATEGORIES to setOf("human"),
            Params.EXAMINE to "Servant of the Duke of Lumbridge.",
        ),
        hitpoints = 40,
    )

    override fun empty(): NPCDefinition = NPCDefinition(-1)

    override fun definitions(): NPCDefinitions = NPCDefinitions.init(definitions)

    override fun load(definitions: NPCDefinitions) {
        definitions.definitions[0].stringId = "0" // To by-pass loading checks
        val uri = NPCDefinitionsTest::class.java.getResource("test-npc.toml")!!
        definitions.load(listOf(uri.path))
    }

    @Test
    fun `Custom fields are loaded and inherited by clones`(@TempDir dir: File) {
        val file = dir.resolve("custom.npcs.toml")
        file.writeText(
            """
            [dragon]
            id = 0
            hitpoints = 500
            allowed_under = true
            solid = false
            blocks_players = true

            [baby_dragon]
            id = 1
            clone = "dragon"

            [early_clone]
            id = 2
            clone = "late_dragon"
            solid = false
            blocks_players = false

            [late_dragon]
            id = 3
            hitpoints = 300
            allowed_under = true
            solid = true
            blocks_players = true

            [plain]
            id = 4
            """.trimIndent(),
        )
        val definitions = NPCDefinitions.init(Array(5) { NPCDefinition(it, stringId = it.toString()) })
        definitions.load(listOf(file.path))

        val dragon = definitions.get("dragon")
        assertTrue(dragon.allowedUnder)
        assertFalse(dragon.solid)
        assertTrue(dragon.blocksPlayers)
        val baby = definitions.get("baby_dragon")
        assertTrue(baby.allowedUnder)
        assertFalse(baby.solid)
        assertTrue(baby.blocksPlayers)
        assertEquals(500, baby.hitpoints)
        val early = definitions.get("early_clone")
        assertEquals(300, early.hitpoints)
        assertTrue(early.allowedUnder)
        assertFalse(early.solid)
        assertTrue(early.blocksPlayers)
        val plain = definitions.get("plain")
        assertFalse(plain.allowedUnder)
        assertTrue(plain.solid)
        assertFalse(plain.blocksPlayers)
    }

    @AfterEach
    fun teardown() {
        VariableDefinitions.clear()
    }

    @Test
    fun `Transforms fall back to the default when unset, out of range or -1`() {
        val base = NPCDefinition(0, stringId = "base", varbit = 1, transforms = intArrayOf(-1, 1, 2))
        val definitions = NPCDefinitions.init(arrayOf(base, NPCDefinition(1, stringId = "first"), NPCDefinition(2, stringId = "default")))
        val variable: VariableDefinition = mockk(relaxed = true)
        every { variable.values } returns IntValues
        VariableDefinitions.set(mapOf("var" to variable), varbits = mapOf(1 to "var"))
        val player: Player = mockk(relaxed = true)
        var value: Int? = null
        every { player.variables.get<Any>("var") } answers { value }

        assertEquals("default", definitions.resolve(base, player).stringId)
        value = 0
        assertEquals("default", definitions.resolve(base, player).stringId)
        value = 1
        assertEquals("first", definitions.resolve(base, player).stringId)
        value = 5
        assertEquals("default", definitions.resolve(base, player).stringId)
        value = -2
        assertEquals("default", definitions.resolve(base, player).stringId)
    }
}
