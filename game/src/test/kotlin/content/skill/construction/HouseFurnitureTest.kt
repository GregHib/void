package content.skill.construction

import FakeRandom
import WorldTest
import containsMessage
import content.activity.shooting_star.ShootingStarHandler
import content.activity.shooting_star.StarLocationData
import content.skill.construction.HouseFurniture.Companion.clockTime
import content.skill.summoning.follower
import content.skill.summoning.pet.pet
import dialogueContinue
import dialogueOption
import intEntry
import interfaceOption
import itemOnObject
import objectOption
import org.junit.jupiter.api.Test
import skillCreation
import walk
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
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
        assertTrue(player.containsMessage("You take an egg."))
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
    fun `Make teleport tablets at a lectern`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Magic, 25)
        player.inventory.add("soft_clay", 2)
        player.inventory.add("law_rune", 2)
        player.inventory.add("air_rune", 6)
        player.inventory.add("fire_rune", 2)
        val lectern = createObject("oak_lectern", emptyTile.addY(1))

        player.objectOption(lectern, "Study")
        tickIf { !player.hasOpen("teleport_tablets") }
        assertEquals(1, player.get<Int>("house_lectern_tier"))
        player.interfaceOption("teleport_tablets", "varrock_teleport", "Make-All")
        tick(8)

        assertEquals(2, player.inventory.count("varrock_teleport"))
        assertEquals(0, player.inventory.count("soft_clay"))
        assertEquals(0, player.inventory.count("law_rune"))
        assertEquals(70.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't make tablets a lectern doesn't support`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("soft_clay")
        player.inventory.add("law_rune", 10)
        player.inventory.add("earth_rune", 10)
        player.inventory.add("air_rune", 10)
        val lectern = createObject("demon_lectern", emptyTile.addY(1))

        player.objectOption(lectern, "Study")
        tickIf { !player.hasOpen("teleport_tablets") }
        player.interfaceOption("teleport_tablets", "teleport_to_house", "Make")
        tick()

        assertEquals(0, player.inventory.count("teleport_to_house"))
        assertEquals(1, player.inventory.count("soft_clay"))
    }

    @Test
    fun `Can't make tablets without runes`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("soft_clay")
        val lectern = createObject("mahogany_demon_lectern", emptyTile.addY(1))

        player.objectOption(lectern, "Study")
        tickIf { !player.hasOpen("teleport_tablets") }
        player.interfaceOption("teleport_tablets", "bones_to_peaches", "Make")
        tick()

        assertEquals(0, player.inventory.count("bones_to_peaches"))
        assertEquals(1, player.inventory.count("soft_clay"))
    }

    @Test
    fun `Look for shooting stars through a telescope`() {
        val player = createPlayer(emptyTile)
        val telescope = createObject("mahogany_telescope", emptyTile.addY(1))
        ShootingStarHandler.nextLocation = StarLocationData.CRAFTING_GUILD
        ShootingStarHandler.nextTier = 4
        ShootingStarHandler.nextStarTick = GameLoop.tick + 1000

        player.objectOption(telescope, "Observe")
        tickIf { !player.hasOpen("star_telescope_meteor") }
        ShootingStarHandler.nextLocation = null

        assertTrue(player.dialogue!!.startsWith("dialogue_message"))
        assertTrue(player.hasOpen("star_telescope_meteor"))
    }

    @Test
    fun `Make clockwork at a clockmaker's bench`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Crafting, 10)
        player.inventory.add("steel_bar", 2)
        val bench = createObject("crafting_table_2", emptyTile.addY(1))

        player.objectOption(bench, "Craft")
        tickIf { !player.hasOpen("dialogue_skill_creation") }
        player.skillCreation("Clockwork", 2)
        tick(5)

        assertEquals(2, player.inventory.count("clockwork"))
        assertEquals(30.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Paint a family crest on a helmet`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Crafting, 38)
        player["heraldry_crest"] = 15
        player.inventory.add("steel_full_helm")
        val stand = createObject("pluming_stand", emptyTile.addY(1))

        player.objectOption(stand, "Make-helmet")
        tickIf { !player.inventory.contains("steel_heraldic_helm_zamorak") }

        assertEquals(0, player.inventory.count("steel_full_helm"))
    }

    @Test
    fun `Can't paint without a family crest`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("plank", "bolt_of_cloth")
        val easel = createObject("banner_easel", emptyTile.addY(1))

        player.objectOption(easel, "Use")
        tickIf { !player.containsMessage("family crest") }

        assertEquals(1, player.inventory.count("plank"))
    }

    @Test
    fun `Store a pet in a pet house`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.inventory.add("pet_kitten")
        val house = createObject("oak_pet_house", emptyTile.addY(1))

        player.objectOption(house, "Store")
        tickIf { !player.hasOpen("pet_house") }
        player.interfaceOption("summoning_side", "inventory", "Store-1", item = Item("pet_kitten"), slot = 0)
        tick()

        assertEquals(0, player.inventory.count("pet_kitten"))
        assertEquals(1, player.inventories.inventory("pet_house").count("pet_kitten"))
    }

    @Test
    fun `Take a pet from a pet house`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.inventories.inventory("pet_house").add("pet_kitten")
        val house = createObject("teak_pet_house", emptyTile.addY(1))

        player.objectOption(house, "Store")
        tickIf { !player.hasOpen("pet_house") }
        player.interfaceOption("pet_house", "items", "Take", item = Item("pet_kitten"), slot = 0)
        tick()

        assertEquals(1, player.inventory.count("pet_kitten"))
        assertEquals(0, player.inventories.inventory("pet_house").count("pet_kitten"))
    }

    @Test
    fun `Feed all fills the hunger of the following pet`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.pet = createNPC("pet_cat_baby", emptyTile)
        player["pet_active_item"] = "pet_kitten"
        player["pet_cat_hunger"] = 5000
        player["pet_cat_warn"] = 1
        val feeder = createObject("oak_pet_feeder", emptyTile.addY(1))

        player.objectOption(feeder, "Feed-all")
        tickIf { player.get("pet_cat_hunger", 0) != 0 }

        assertEquals(0, player["pet_cat_hunger", 0])
        assertEquals(0, player["pet_cat_warn", 0])
    }

    @Test
    fun `Pet feeders ignore players without a following pet`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.inventories.inventory("pet_house").add("pet_kitten")
        player["pet_cat_hunger"] = 5000
        val feeder = createObject("oak_pet_feeder", emptyTile.addY(1))

        player.objectOption(feeder, "Feed-all")
        tick(2)

        assertEquals(5000, player["pet_cat_hunger", 0])
        assertTrue(player.containsMessage("no pet following"))
    }

    @Test
    fun `Throw darts at a dartboard`() {
        val player = createPlayer(emptyTile)
        player.equipment.set(EquipSlot.Weapon.index, "bronze_dart", 10)
        val board = createObject("dartboard", emptyTile.addY(1))

        player.objectOption(board, "Throw-at")
        tickIf { !player.containsMessage("Your dart") }

        assertEquals(10, player.equipment.count("bronze_dart"))
        assertTrue(player.hasOpen("poh_ranging"))
        assertEquals(1, player["ranging_shots", 0])
    }

    @Test
    fun `Can't shoot at an archery target without a bow`() {
        val player = createPlayer(emptyTile)
        val target = createObject("house_archery_target", emptyTile.addY(1))

        player.objectOption(target, "Shoot-at")
        tickIf { !player.containsMessage("bow and arrows") }
    }

    @Test
    fun `Owner adds prize money to the prize chest up to its capacity`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.inventory.add("coins", 30000)
        val chest = createObject("oak_prize_chest", emptyTile.addY(1))

        player.objectOption(chest, "Open")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")
        player.intEntry(30000)
        tick()

        assertEquals(20000, player["house_prize_money", 0])
        assertEquals(10000, player.inventory.count("coins"))
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
    fun `Scrying pool views a location unseen and walking returns the player`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        val observer = createPlayer(emptyTile)
        val pool = createObject("scrying_pool", emptyTile.addX(1))

        player.objectOption(pool, "Scry")
        tickIf {
            player["house_owner"] = player.accountName // Walking up to the furniture leaves the house
            player.dialogue == null
        }
        player.dialogueOption("line1")
        player.dialogueContinue()
        tickIf { !player.hasOpen("poh_scrying_pool") }
        val back = player.get<Tile>("scrying_return")!!

        assertTrue(player.appearance.hidden)
        assertTrue(player.contains("house_owner"))
        assertTrue(player.tile != back)

        player.walk(back)
        tick(2)

        assertFalse(player.appearance.hidden)
        assertFalse(player.hasOpen("poh_scrying_pool"))
        assertEquals(back, player.tile)
        assertFalse(observer.appearance.hidden)
    }

    @Test
    fun `Can't scry with a follower`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.follower = createNPC("spirit_wolf_familiar", emptyTile.addY(2))
        val pool = createObject("scrying_pool", emptyTile.addX(1))

        player.objectOption(pool, "Scry")
        tickIf { !player.containsMessage("cannot scry while you have a follower") }
    }

    @Test
    fun `Bookcase shows books unlocked by completed quests`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player["dwarf_cannon"] = "completed"
        val book = "instruction_manual"
        val bookcase = createObject("oak_bookcase", emptyTile.addX(1))

        player.objectOption(bookcase, "Search")
        tickIf {
            player["house_owner"] = player.accountName // Walking up to the furniture leaves the house
            !player.hasOpen("poh_bookcase")
        }
        val slot = 3
        assertEquals(1 shl slot, player["house_books_1", 0] and (1 shl slot))
        // The Shield of Arrav (slot 27) isn't unlocked
        assertEquals(0, player["house_books_1", 0] and (1 shl 27))
        // Books without a quest are always unlocked
        assertEquals(1, player["house_books_1", 0] and 1)

        player.interfaceOption("poh_bookcase", "books", "Take", item = Item(book), slot = slot)
        tick()

        assertEquals(1, player.inventory.count(book))
    }

    @Test
    fun `Store and take an item from a magic wardrobe`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        player.inventory.add("runecrafter_hat")
        val wardrobe = createObject("oak_magic_wardrobe", emptyTile.addX(1))

        player.objectOption(wardrobe, "Open")
        tickIf {
            player["house_owner"] = player.accountName // Walking up to the furniture leaves the house
            !player.hasOpen("poh_costume_room")
        }
        val row = player.get<List<String>>("house_costume_items")!!.indexOf("runecrafter_hat") + 1
        player.interfaceOption("poh_costume_room", "select_$row", "Select")
        tick()

        assertEquals(0, player.inventory.count("runecrafter_hat"))
        player.interfaceOption("poh_costume_room", "select_$row", "Select")
        tick()

        assertEquals(1, player.inventory.count("runecrafter_hat"))
    }

    @Test
    fun `Treasure chest asks which level of treasure trail reward to take`() {
        val player = createPlayer(emptyTile)
        player["house_owner"] = player.accountName
        val chest = createObject("oak_treasure_chest", emptyTile.addX(1))

        player.objectOption(chest, "Open")
        tickIf {
            player["house_owner"] = player.accountName // Walking up to the furniture leaves the house
            player.dialogue == null
        }
        player.dialogueOption("line1")
        tickIf { !player.hasOpen("poh_costume_room") }

        assertEquals(30, player.get<List<String>>("house_costume_items")!!.size)
    }

    @Test
    fun `Hangman selects letters and guesses them`() {
        val player = createPlayer(emptyTile)
        val game = createObject("hangman_game", emptyTile.addY(1))

        player.objectOption(game, "Activate")
        tickIf { !player.hasOpen("poh_hangman") }
        tick()
        player["hangman_word"] = "WIZARD"
        player.interfaceOption("poh_hangman", "w", "Select")
        player.interfaceOption("poh_hangman", "q", "Select")
        player.interfaceOption("poh_hangman", "guess", "Guess")
        tick()

        assertEquals("WQ", player.get<String>("hangman_guessed"))
        assertEquals(1, player.get<Int>("hangman_wrong"))
        assertEquals("3945", player.get<NPC>("hangman_npc")?.transformId)
    }

    @Test
    fun `Crystal of power changes a staff's element`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("staff_of_air")
        player.inventory.add("mystic_air_staff")
        player.inventory.add("water_rune", 1000)
        val crystal = createObject("crystal_of_power", emptyTile.addY(1))

        player.itemOnObject(crystal, 0)
        tick(2)
        player.dialogueOption("line1")
        tick()

        assertEquals(1, player.inventory.count("staff_of_water"))

        player.itemOnObject(crystal, 1)
        tick(2)
        player.dialogueOption("line1")
        tick()

        assertEquals(1, player.inventory.count("mystic_water_staff"))
        assertEquals(0, player.inventory.count("water_rune"))
    }

    @Test
    fun `Crystal of power needs runes for a battlestaff`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("air_battlestaff")
        player.inventory.add("water_rune", 99)
        val crystal = createObject("crystal_of_power", emptyTile.addY(1))

        player.itemOnObject(crystal, 0)
        tick(2)
        player.dialogueOption("line1")
        tick()

        assertEquals(1, player.inventory.count("air_battlestaff"))
        assertEquals(99, player.inventory.count("water_rune"))
    }
}
