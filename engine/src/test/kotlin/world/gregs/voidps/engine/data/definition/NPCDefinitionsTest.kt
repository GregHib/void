package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import java.io.File
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
        val early = definitions.get("early_clone")
        assertTrue(early.allowedUnder)
        assertFalse(early.solid)
        assertTrue(early.blocksPlayers)
        val plain = definitions.get("plain")
        assertFalse(plain.allowedUnder)
        assertTrue(plain.solid)
        assertFalse(plain.blocksPlayers)
    }
}
