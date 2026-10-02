package content.skill.construction

import WorldTest
import containsMessage
import content.skill.construction.HouseFurniture.Companion.clockTime
import dialogueOption
import itemOnObject
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseFurnitureTest : WorldTest() {

    @Test
    fun `Take an item from a larder`() {
        val player = createPlayer(emptyTile)
        val larder = createObject("oak_larder", emptyTile.addY(1))

        player.objectOption(larder, "Search")
        tick()
        player.dialogueOption("line3")

        assertEquals(1, player.inventory.count("egg"))
    }

    @Test
    fun `Take an item from the second page of shelves`() {
        val player = createPlayer(emptyTile)
        val shelves = createObject("teak_shelves_2", emptyTile.addY(1))

        player.objectOption(shelves, "Search")
        tick()
        player.dialogueOption("line5")
        player.dialogueOption("line5")

        assertEquals(1, player.inventory.count("chefs_hat"))
    }

    @Test
    fun `Can't take items with a full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("coins")
        player.inventory.add("shark", 27)
        val store = createObject("tool_store_1", emptyTile.addY(1))

        player.objectOption(store, "Search")
        tick()
        player.dialogueOption("line2")

        assertEquals(0, player.inventory.count("hammer"))
        assertTrue(player.containsMessage("inventory space"))
    }

    @Test
    fun `Fill a beer glass from a barrel`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("beer_glass")
        val barrel = createObject("dragon_bitter_barrel", emptyTile.addY(1))

        player.itemOnObject(barrel, 0)
        tick()

        assertEquals(0, player.inventory.count("beer_glass"))
        assertEquals(1, player.inventory.count("dragon_bitter"))
    }

    @Test
    fun `Light a fireplace`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("tinderbox", "logs")
        val fireplace = createObject("clay_fireplace", emptyTile.addX(-1))

        player.objectOption(fireplace, "Light")
        tick()

        assertEquals(0, player.inventory.count("logs"))
        assertNotNull(GameObjects.findOrNull(fireplace.tile, "clay_fireplace_lit"))
        assertEquals(80.0, player.experience.get(Skill.Firemaking))
    }

    @Test
    fun `Can't light a fireplace in building mode`() {
        val player = createPlayer(emptyTile)
        player["house_build_mode"] = true
        player.inventory.add("tinderbox", "logs")
        val fireplace = createObject("clay_fireplace", emptyTile.addX(-1))

        player.objectOption(fireplace, "Light")
        tick()

        assertEquals(1, player.inventory.count("logs"))
        assertNull(GameObjects.findOrNull(fireplace.tile, "clay_fireplace_lit"))
    }

    @Test
    fun `Can't light a fireplace without logs`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("tinderbox")
        val fireplace = createObject("marble_fireplace", emptyTile.addX(-1))

        player.objectOption(fireplace, "Light")
        tick()

        assertNull(GameObjects.findOrNull(fireplace.tile, "marble_fireplace_lit"))
        assertEquals(0.0, player.experience.get(Skill.Firemaking))
    }

    @Test
    fun `Fill a bucket from a pump and tub`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("bucket")
        val pump = createObject("pump_and_tub", emptyTile.addX(-1))

        player.itemOnObject(pump, 0)
        tick(2)

        assertEquals(1, player.inventory.count("bucket_of_water"))
    }

    @Test
    fun `Read a clock`() {
        val player = createPlayer(emptyTile)
        val clock = createObject("oak_clock", emptyTile.addY(1))

        player.objectOption(clock, "Read")
        tick()

        assertTrue(player.containsMessage("Rune"))
    }

    @Test
    fun `Clock time rounds to the nearest five minutes`() {
        assertEquals("It's Rune o'clock.", clockTime(1))
        assertEquals("It's a quarter past Rune.", clockTime(15))
        assertEquals("It's half past Rune.", clockTime(32))
        assertEquals("It's twenty to Rune.", clockTime(39))
        assertEquals("It's five to Rune.", clockTime(55))
        assertEquals("It's Rune o'clock.", clockTime(58))
    }
}
