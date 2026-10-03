package content.skill.construction

import FakeRandom
import WorldTest
import containsMessage
import content.skill.construction.HouseFurniture.Companion.clockTime
import dialogueOption
import interfaceOption
import itemOnObject
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `Change clothes at a wardrobe`() {
        val player = createPlayer(emptyTile)
        val wardrobe = createObject("oak_wardrobe", emptyTile.addY(1))

        player.objectOption(wardrobe, "Change-clothes")
        tick()
        player.dialogueOption("line1")
        tick(2)

        assertTrue(player.hasOpen("thessalias_makeovers"))
    }

    @Test
    fun `Can't change clothes wearing armour`() {
        val player = createPlayer(emptyTile)
        player.equipment.set(EquipSlot.Chest.index, "bronze_platebody")
        val wardrobe = createObject("teak_drawers", emptyTile.addY(1))

        player.objectOption(wardrobe, "Change-clothes")
        tick()
        player.dialogueOption("line1")
        tick(2)

        assertFalse(player.hasOpen("thessalias_makeovers"))
    }

    @Test
    fun `Change shoes with a shoe box`() {
        val player = createPlayer(emptyTile)
        val box = createObject("shoe_box", emptyTile.addY(1))

        player.objectOption(box, "Change-clothes")
        tick(3)

        assertTrue(player.hasOpen("yrsas_shoe_store"))
    }

    @Test
    fun `Change hairstyle at a dresser`() {
        val player = createPlayer(emptyTile)
        val dresser = createObject("gilded_dresser", emptyTile.addY(1))

        player.objectOption(dresser, "Preen")
        tick(3)

        assertTrue(player.hasOpen("hairdressers_salon"))
    }

    @Test
    fun `Confirming a makeover in a house doesn't talk to the shopkeeper`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        val dresser = createObject("oak_dresser", emptyTile.addY(1))
        player.objectOption(dresser, "Preen")
        tick(3)

        player.interfaceOption("hairdressers_salon", "confirm", "Confirm")
        tick()

        assertFalse(player.hasOpen("hairdressers_salon"))
        assertNull(player.dialogue)
    }

    @Test
    fun `Talk to a mounted head`() {
        val player = createPlayer(emptyTile)
        val head = createObject("kbd_heads_trophy", emptyTile.addY(1))

        player.objectOption(head, "Talk-to")
        tick()

        assertNotNull(player.dialogue)
    }

    @Test
    fun `Teleport with a mounted amulet of glory`() {
        val player = createPlayer(emptyTile)
        val glory = createObject("amulet_of_glory_mounted", emptyTile.addY(1))

        player.objectOption(glory, "Rub")
        tick()
        player.dialogueOption("line1")
        tick(5)

        assertTrue(player.tile in Areas["edgeville_teleport"])
    }

    @Test
    fun `Ringing a bell-pull without a servant`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        val bell = createObject("posh_bell_pull", emptyTile.addY(1))

        player.objectOption(bell, "Ring")
        tick()

        assertTrue(player.containsMessage("no servant"))
    }

    @Test
    fun `Every furniture option has an interaction`() {
        val missing = mutableListOf<String>()
        for (row in Tables.get("house_furniture").rows()) {
            for (id in Tables.objList("house_furniture.${row.rowId}.objects")) {
                val options = ObjectDefinitions.get(id).options ?: continue
                for (option in options.filterNotNull()) {
                    if (option == "Examine" || option == "Remove") {
                        continue
                    }
                    if (!Operation.playerObject.containsKey("$option:$id") && !Operation.playerObject.containsKey("$option:*")) {
                        missing.add("$option:$id")
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), missing.toString())
    }

    @Test
    fun `Unimplemented furniture sends a message`() {
        val player = createPlayer(emptyTile)
        val pool = createObject("scrying_pool", emptyTile.addY(1))

        player.objectOption(pool, "Scry")
        tick()

        assertTrue(player.containsMessage("Not yet implemented."))
    }
}
