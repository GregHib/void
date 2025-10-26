package content.skill.farming

import FakeRandom
import KoinMock
import containsMessage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.resetCurrentTime
import world.gregs.voidps.engine.timer.setCurrentTime
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.setRandom
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class FarmingTest : KoinMock() {

    private lateinit var definitions: MutableMap<String, VariableDefinition>
    private lateinit var player: Player
    private lateinit var farming: Farming
    private lateinit var farmingDefinitions: FarmingDefinitions

    @BeforeEach
    fun setup() {
        declare {
            val sounds = mockk<SoundDefinitions>()
            every { sounds.getOrNull("farming_amulet") } returns SoundDefinition.EMPTY
            sounds
        }
        declare {
            val jingles = mockk<JingleDefinitions>()
            every { jingles.getOrNull("farming_amulet_alert") } returns JingleDefinition.EMPTY
            jingles
        }
        declare {
            val fonts = mockk<FontDefinitions>()
            every { fonts.get(any<String>()) } returns FontDefinition(glyphWidths = ByteArray(255))
            fonts
        }
        definitions = mutableMapOf()
        val variableDefinitions = VariableDefinitions()
        variableDefinitions.definitions = definitions
        farmingDefinitions = FarmingDefinitions()
        farming = Farming(variableDefinitions, farmingDefinitions)
        player = Player()
        resetCurrentTime()
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 1
        })
    }

    @Test
    fun `Start returns remaining ticks until next cycle`() {
        // 12:07
        setCurrentTime { TimeUnit.MINUTES.toMillis(12 * 60 + 7) }

        player["farming_offset_mins"] = 0
        val ticks = farming.start(player, "farming_tick", restart = false)

        assertEquals(TimeUnit.MINUTES.toTicks(3), ticks)
    }

    @Test
    fun `Start adjusts for player farming offset`() {
        // 12:08
        setCurrentTime { TimeUnit.MINUTES.toMillis(12 * 60 + 8) }

        player["farming_offset_mins"] = 2

        val ticks = farming.start(player, "farming_tick", restart = false)

        assertEquals(TimeUnit.MINUTES.toTicks(4), ticks)
    }

    @Test
    fun `Weeds grow correctly through stages`() {
        player["patch_falador_nw_allotment"] = "weeds_0"
        player["farming_offset_mins"] = 0

        farming.grow(player, 10)

        val next = player["patch_falador_nw_allotment", ""]
        assertEquals("weeds_1", next)
    }

    @Test
    fun `Weeds eventually grow fully`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        player["patch_falador_nw_allotment"] = "weeds_3"
        player["farming_offset_mins"] = 0

        // simulate several growth cycles
        repeat(10) {
            farming.grow(player, it * 5)
        }

        val next = player["patch_falador_nw_allotment", ""]
        assertEquals("weeds_none", next)
    }

    @Test
    fun `Crop progresses through growth stages`() {
        player["patch_falador_nw_allotment"] = "potato_0"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "patch_falador_nw_allotment"
        setDefinition(listOf("potato_0", "potato_1", "potato_2", "potato_3", "potato_none"))

        for (i in 0 until 3) {
            farming.grow(player, i * 10)
            assertEquals("potato_${i + 1}", player["patch_falador_nw_allotment", ""])
        }

        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Watered crop dries up after next growth stage`() {
        player["patch_falador_nw_allotment"] = "potato_watered_0"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "patch_falador_nw_allotment"
        setDefinition(listOf("potato_0", "potato_1", "potato_none", "potato_watered_0", "potato_watered_1"))

        farming.grow(player, 10)

        assertEquals("potato_1", player["patch_falador_nw_allotment", ""])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Crop has a chance of becoming diseased`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 4
        })
        player["patch_falador_nw_allotment"] = "magic_tree_1"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "patch_falador_nw_allotment"
        farmingDefinitions.diseaseChances["magic_tree"] = 9
        val array = Array(70) { "" }
        array[0] = "magic_tree_0"
        array[1] = "magic_tree_1"
        array[2] = "magic_tree_none"
        array[64] = "magic_tree_diseased_0"
        array[65] = "magic_tree_diseased_1"
        setDefinition(array.toList())

        farming.grow(player, 10)

        assertEquals("magic_tree_diseased_1", player["patch_falador_nw_allotment", ""])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Crop stage without disease won't become diseased`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        player["patch_falador_nw_allotment"] = "potato_0"
        player["farming_offset_mins"] = 0
        val array = Array(70) { "" }
        array[0] = "potato_0"
        array[1] = "potato_1"
        array[2] = "potato_none"
        setDefinition(array.toList())

        farming.grow(player, 10)

        val next = player["patch_falador_nw_allotment", ""]
        assertEquals("potato_1", next)
    }

    @Test
    fun `Diseased crop becomes dead next farming tick`() {
        player["patch_falador_nw_allotment"] = "potato_diseased_2"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "patch_falador_nw_allotment"

        farming.grow(player, 10)
        assertEquals("potato_dead_2", player["patch_falador_nw_allotment"])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Completed crop uses compost state`() {
        player["patch_falador_nw_allotment"] = "potato_3"
        player["patch_falador_nw_allotment_compost"] = "super"
        player["farming_offset_mins"] = 0
        setDefinition(listOf("potato_3", "potato_none", "potato_compost", "potato_super"))

        farming.grow(player, 10)

        val next = player["patch_falador_nw_allotment", ""]
        assertEquals("potato_super", next)
    }

    private fun setDefinition(list: List<String>) {
        definitions["patch_falador_nw_allotment"] = VariableDefinition.VarbitDefinition(
            id = -1,
            values = ListValues(list),
            default = null,
            persistent = false,
            transmit = false
        )
    }
}
