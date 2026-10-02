package content.skill.construction

import FakeRandom
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
import world.gregs.voidps.type.setRandom
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
    fun `Light torches with marrentill`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 30)
        player.inventory.add("tinderbox", "clean_marrentill")
        val torches = createObject("steel_torches", emptyTile.addY(1))

        player.objectOption(torches, "Light")
        tick()

        assertEquals(0, player.inventory.count("clean_marrentill"))
        assertNotNull(GameObjects.findOrNull(torches.tile, "steel_torches_lit"))
    }

    @Test
    fun `Can't light torches without marrentill`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 30)
        player.inventory.add("tinderbox")
        val torches = createObject("steel_candlesticks", emptyTile.addY(1))

        player.objectOption(torches, "Light")
        tick()

        assertNull(GameObjects.findOrNull(torches.tile, "steel_candlesticks_lit"))
    }

    @Test
    fun `Can't light torches without the firemaking level`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 29)
        player.inventory.add("tinderbox")
        val torches = createObject("gold_candlesticks", emptyTile.addY(1))

        player.objectOption(torches, "Light")
        tick()

        assertNull(GameObjects.findOrNull(torches.tile, "gold_candlesticks_lit"))
        assertTrue(player.containsMessage("Firemaking level of 30"))
    }

    @Test
    fun `Light incense burners with marrentill`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 30)
        player.inventory.add("tinderbox", "clean_marrentill")
        val burners = createObject("incense_burners", emptyTile.addY(1))

        player.itemOnObject(burners, 1)
        tick()

        assertEquals(0, player.inventory.count("clean_marrentill"))
        assertNotNull(GameObjects.findOrNull(burners.tile, "incense_burners_lit"))
    }

    @Test
    fun `Can't light incense burners without marrentill`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 30)
        player.inventory.add("tinderbox")
        val burners = createObject("mahogany_burners", emptyTile.addY(1))

        player.objectOption(burners, "Light")
        tick()

        assertNull(GameObjects.findOrNull(burners.tile, "mahogany_burners_lit"))
    }

    @Test
    fun `Incense burners burn out depending on firemaking level`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Firemaking, 50)
        player.inventory.add("tinderbox", "clean_marrentill")
        val burners = createObject("marble_burners", emptyTile.addY(1))

        player.objectOption(burners, "Light")
        tick()
        // 200 + level 50 + random 49
        tick(298)
        assertNotNull(GameObjects.findOrNull(burners.tile, "marble_burners_lit"))
        tick()

        assertNull(GameObjects.findOrNull(burners.tile, "marble_burners_lit"))
        assertNotNull(GameObjects.findOrNull(burners.tile, "marble_burners"))
    }

    @Test
    fun `Offer bones on a house altar`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("bones")
        val altar = createObject("prayer_altar_teak_saradomin", emptyTile.addY(1))

        player.itemOnObject(altar, 0)
        tick(2)

        assertEquals(0, player.inventory.count("bones"))
        assertEquals(4.9, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Lit incense burners increase house altar experience`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("bones")
        val altar = createObject("prayer_altar_marble_zamorak", emptyTile.addY(1))
        val zone = altar.tile.zone.tile
        createObject("marble_burners_lit", zone.add(7, 7))
        createObject("marble_burners_lit", zone.add(6, 7))

        player.itemOnObject(altar, 0)
        tick(2)

        assertEquals(13.5, player.experience.get(Skill.Prayer))
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
