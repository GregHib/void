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
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
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
        val invDefs = mockk<InventoryDefinitions>(relaxed = true)
        every { invDefs.get(any<String>()) } returns InventoryDefinition.EMPTY
        player.inventories.definitions = invDefs
        player.inventories.normalStack = ItemDependentStack
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        resetCurrentTime()
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 1
        })
    }

    @Test
    fun `Start returns remaining ticks until next minute`() {
        // 12:08:28
        setCurrentTime { TimeUnit.MINUTES.toMillis(12 * 60 + 8) + TimeUnit.SECONDS.toMillis(28) }

        player["farming_offset_mins"] = 2

        player.timers.start("farming_tick", restart = false)
        val timer = player.timers.queue.first { it.name == "farming_tick" }

        val ticks = timer.nextTick - GameLoop.tick

        assertEquals(TimeUnit.SECONDS.toTicks(32), ticks)
    }

    @Test
    fun `Weeds grow correctly through stages`() {
        player["farming_veg_patch_falador_nw"] = "weeds_0"
        player["farming_offset_mins"] = 0

        farming.grow(player, 10)

        val next = player["farming_veg_patch_falador_nw", ""]
        assertEquals("weeds_1", next)
    }

    @Test
    fun `Weeds eventually grow fully`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        player["farming_veg_patch_falador_nw"] = "weeds_3"
        player["farming_offset_mins"] = 0

        // simulate several growth cycles
        repeat(10) {
            farming.grow(player, it * 5)
        }

        val next = player["farming_veg_patch_falador_nw", ""]
        assertEquals("weeds_life1", next)
    }

    @Test
    fun `Crop progresses through growth stages`() {
        player["farming_veg_patch_falador_nw"] = "potato_0"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "farming_veg_patch_falador_nw"
        setDefinition(listOf("potato_0", "potato_1", "potato_2", "potato_3", "potato_life3"))

        for (i in 0 until 3) {
            farming.grow(player, i * 10)
            assertEquals("potato_${i + 1}", player["farming_veg_patch_falador_nw", ""])
        }

        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Watered crop dries up after next growth stage`() {
        player["farming_veg_patch_falador_nw"] = "potato_watered_0"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "farming_veg_patch_falador_nw"
        setDefinition(listOf("potato_0", "potato_1", "potato_life3", "potato_watered_0", "potato_watered_1"))

        farming.grow(player, 10)

        assertEquals("potato_1", player["farming_veg_patch_falador_nw", ""])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Crop has a chance of becoming diseased`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 4
        })
        player["farming_veg_patch_falador_nw"] = "magic_tree_1"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "farming_veg_patch_falador_nw"
        farmingDefinitions.diseaseChances["magic_tree"] = 9
        val array = Array(70) { "" }
        array[0] = "magic_tree_0"
        array[1] = "magic_tree_1"
        array[2] = "magic_tree_none"
        array[64] = "magic_tree_diseased_0"
        array[65] = "magic_tree_diseased_1"
        setDefinition(array.toList())

        farming.grow(player, 10)

        assertEquals("magic_tree_diseased_1", player["farming_veg_patch_falador_nw", ""])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Crop stage without disease won't become diseased`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        player["farming_veg_patch_falador_nw"] = "potato_0"
        player["farming_offset_mins"] = 0
        val array = Array(70) { "" }
        array[0] = "potato_0"
        array[1] = "potato_1"
        array[2] = "potato_life3"
        setDefinition(array.toList())

        farming.grow(player, 10)

        val next = player["farming_veg_patch_falador_nw", ""]
        assertEquals("potato_1", next)
    }

    @Test
    fun `Diseased crop becomes dead next farming tick`() {
        player["farming_veg_patch_falador_nw"] = "potato_diseased_2"
        player["farming_offset_mins"] = 0
        player["amulet_of_farming_patch"] = "farming_veg_patch_falador_nw"

        farming.grow(player, 10)
        assertEquals("potato_dead_2", player["farming_veg_patch_falador_nw"])
        assertTrue(player.containsMessage("A low hum resonates"))
    }

    @Test
    fun `Completed crop uses compost state`() {
        player["farming_veg_patch_falador_nw"] = "potato_3"
        player.addVarbit("patch_super_compost", "farming_veg_patch_falador_nw")
        player["farming_offset_mins"] = 0
        setDefinition(listOf("potato_3", "potato_life3", "potato_life2", "potato_life1"))

        farming.grow(player, 10)

        val next = player["farming_veg_patch_falador_nw", ""]
        assertEquals("potato_life1", next)
    }

    @Test
    fun `Compost bins rot correctly through stages`() {
        player["compost_bin_falador"] = "compostable_rotting_0"
        player["farming_offset_mins"] = 0

        farming.grow(player, 2)

        assertEquals("compostable_rotting_1", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Compost ready after 30 rot stages`() {
        player["compost_bin_falador"] = "compostable_rotting_30"
        player["farming_offset_mins"] = 0

        farming.grow(player, 4)

        assertEquals("compostable_rotting_ready", player["compost_bin_falador", "empty"])
    }

    private fun setDefinition(list: List<String>) {
        val map = list.mapIndexed { index, s -> s to index }.toMap()
        definitions["farming_veg_patch_falador_nw"] = VariableDefinition.VarbitDefinition(
            id = -1,
            values = MapValues(map as Map<Any, Int>),
            default = null,
            persistent = false,
            transmit = false,
        )
    }
}
