package content.quest.member.waterfall_quest

import WorldTest
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.list.QuickPrayers
import content.skill.prayer.praying
import dialogueOption
import interfaceOption
import itemOnObject
import itemOption
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WaterfallQuestTest : WorldTest() {
    override var loadNpcs = true

    private val questScript get() = scripts.filterIsInstance<WaterfallQuest>().single()

    private fun finishDialogue(player: Player) {
        repeat(12) {
            if (player.dialogue != null) player.skipDialogues()
            tick()
        }
    }

    @Test
    fun `Read tombstone opens stone inscription and close dismisses it`() {
        val player = createPlayer(Tile(2558, 3443))
        player.objectOption(GameObjects.find(Tile(2558, 3444), "glarials_tombstone"), "Read")
        tick(3)
        assertTrue(player.hasOpen("glarials_tombstone_inscription"))
        player.interfaceOption("glarials_tombstone_inscription", "close", "Close")
        tick()
        assertFalse(player.hasOpen("glarials_tombstone_inscription"))
    }

    @Test
    fun `Hudon cannot hear from bank and Gerald waits for Talk-to after swimming downstream`() {
        val player = createPlayer(Tile(2511, 3483))
        player["god_mode"] = true
        player[WaterfallQuest.QUEST] = "started"
        player.npcOption(NPCs.find(player.tile.regionLevel, "hudon_baxtorian_falls"), "Talk-to")
        tick(2)
        assertEquals(null, player.dialogue)
        assertEquals("started", player[WaterfallQuest.QUEST, "unstarted"])
        assertTrue(player.messages.any { it.contains("Hudon can't hear you") })
        player.tele(2511, 3481)
        player[WaterfallQuest.QUEST] = "found_hudon"
        player.objectOption(GameObjects.find(Tile(2512, 3468), "waterfall_rock"), "Swim to")
        tick(6)
        assertEquals(Tile(2527, 3413), player.tile)
        assertEquals(null, player.dialogue)
        player.npcOption(NPCs.find(player.tile.regionLevel, "gerald_baxtorian_falls"), "Talk-to")
        tick(2)
        assertTrue(player.dialogue != null)
        finishDialogue(player)
        assertEquals(null, player.dialogue)
        assertTrue(player.messages.any { it.contains("washing you downstream") })
        assertEquals("found_hudon", player[WaterfallQuest.QUEST, "unstarted"])
    }

    @Test
    fun `Almera and Hudon return visits preserve quest progress`() {
        val player = createPlayer(Tile(2522, 3497))
        val almera = NPCs.find(player.tile.regionLevel, "almera_baxtorian_falls")
        for (stage in listOf("started", "found_hudon", "read_book", "found_amulet", "entered_dungeon", "completed")) {
            player.tele(2522, 3497)
            player[WaterfallQuest.QUEST] = stage
            player.npcOption(almera, "Talk-to")
            tick(2)
            player.skipDialogues()
            assertEquals(stage, player[WaterfallQuest.QUEST, "unstarted"])
            player.tele(2511, 3481)
            player.npcOption(NPCs.find(player.tile.regionLevel, "hudon_baxtorian_falls"), "Talk-to")
            tick(2)
            player.skipDialogues()
            assertEquals(if (stage == "started") "found_hudon" else stage, player[WaterfallQuest.QUEST, "unstarted"])
        }
    }

    @Test
    fun `Waterfall entrance requires wearing amulet`() {
        val player = createPlayer(Tile(2511, 3463))
        player[WaterfallQuest.QUEST] = "found_urn"
        player["god_mode"] = true
        player.inventory.add("glarials_amulet")
        val entrance = GameObjects.find(Tile(2511, 3464), "waterfall_entrance")
        player.objectOption(entrance, "Enter")
        tick(6)
        assertEquals(Tile(2527, 3413), player.tile)
        player.inventory.remove("glarials_amulet")
        player.equipment.add("glarials_amulet")
        player.tele(2511, 3463)
        player.objectOption(entrance, "Enter")
        tick(3)
        assertEquals(Tile(2575, 9861), player.tile)
    }

    @Test
    fun `Declining start leaves quest unstarted and Hadley topics return to choices`() {
        val player = createPlayer(Tile(2522, 3497))
        player.npcOption(NPCs.find(player.tile.regionLevel, "almera_baxtorian_falls"), "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals("unstarted", player[WaterfallQuest.QUEST, "unstarted"])
        assertEquals(null, player.dialogue)
        player.npcOption(NPCs.find(player.tile.regionLevel, "almera_baxtorian_falls"), "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        assertEquals("started", player[WaterfallQuest.QUEST, "unstarted"])
        player.tele(2520, 3427)
        player.npcOption(NPCs.find(player.tile.regionLevel, "hadley_baxtorian_falls"), "Talk-to")
        tick(8)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        player.dialogueOption(3)
        player.skipDialogues()
        player.dialogueOption(4)
        player.skipDialogues()
        assertEquals(null, player.dialogue)
    }

    @Test
    fun `Accept quest and talk to Hudon across river from crash island`() {
        val player = createPlayer(Tile(2522, 3497))
        player.npcOption(NPCs.find(player.tile.regionLevel, "almera_baxtorian_falls"), "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        assertEquals("started", player[WaterfallQuest.QUEST, "unstarted"])
        player.tele(2510, 3493)
        player.objectOption(GameObjects.find(Tile(2509, 3493), "waterfall_raft"), "Board")
        tick(4)
        val crashIsland = Tile(2511, 3481)
        assertEquals(crashIsland, player.tile)
        player.npcOption(NPCs.find(player.tile.regionLevel, "hudon_baxtorian_falls"), "Talk-to")
        tick(2)
        player.skipDialogues()
        assertEquals("found_hudon", player[WaterfallQuest.QUEST, "unstarted"])
        assertEquals(crashIsland, player.tile)
    }

    @Test
    fun `Read history free Golrie and recover lost pebble`() {
        val player = createPlayer(Tile(2520, 3429, 1))
        player[WaterfallQuest.QUEST] = "found_hudon"
        player.objectOption(GameObjects.find(Tile(2520, 3430, 1), "waterfall_bookcase"), "Search")
        tick(6)
        assertTrue(player.inventory.contains("book_on_baxtorian"), "${player.tile}: ${player.messages}")
        assertTrue(player.messages.any { it == "You search the bookcase." })
        assertTrue(player.messages.any { it.contains("You find a book named 'Book on Baxtorian'") })
        player.objectOption(GameObjects.find(Tile(2520, 3430, 1), "waterfall_bookcase"), "Search")
        tick(6)
        assertEquals(1, player.inventory.count("book_on_baxtorian"))
        player.itemOption("Read", "book_on_baxtorian")
        tick(2)
        assertEquals("book_on_baxtorian", player["book", ""])
        assertEquals("read_book", player[WaterfallQuest.QUEST, "unstarted"])
        player.tele(2548, 9564, 0)
        player.objectOption(GameObjects.find(Tile(2548, 9565), "crate_10"), "Search")
        tick(12)
        assertTrue(player.inventory.contains("a_key"), "${player.tile}: ${player.messages}")
        player.tele(2515, 9574)
        player.itemOnObject(GameObjects.find(Tile(2515, 9575), "gate_33_closed"), player.inventory.indexOf("a_key"))
        tick(6)
        assertTrue(player["waterfall_golrie_freed", false])
        assertEquals(Tile(2515, 9576), player.tile)
        val golrie = NPCs.find(player.tile.regionLevel, "golrie")
        player.npcOption(golrie, "Talk-to")
        tick(8)
        finishDialogue(player)
        assertEquals("found_pebble", player[WaterfallQuest.QUEST, "unstarted"])
        assertTrue(player.inventory.contains("glarials_pebble"))
        assertFalse(player.inventory.contains("a_key"))
        assertTrue(player["waterfall_golrie_key_given", false])
        player.inventory.remove("glarials_pebble")
        player.npcOption(golrie, "Talk-to")
        tick(2)
        finishDialogue(player)
        assertEquals(1, player.inventory.count("glarials_pebble"))
        player.tele(2515, 9576)
        player.objectOption(GameObjects.find(Tile(2515, 9575), "gate_33_closed"), "Open")
        tick(6)
        assertEquals(Tile(2515, 9574), player.tile)
    }

    @Test
    fun `Future Glouphrie dialogue gives missing items and returns to its choices`() {
        val player = createPlayer(Tile(2515, 9576))
        player["path_of_glouphrie"] = "completed"
        player.npcOption(NPCs.find(player.tile.regionLevel, "golrie"), "Talk-to")
        tick(2)
        finishDialogue(player)
        player.dialogueOption(1)
        finishDialogue(player)
        assertTrue(player.inventory.contains("cadarn_lineage"))
        player.dialogueOption(3)
        finishDialogue(player)
        assertTrue(player.inventory.contains("a_key"))
        player.dialogueOption(2)
        finishDialogue(player)
        assertTrue(player.inventory.contains("glarials_pebble"))
        player.dialogueOption(1)
        finishDialogue(player)
        assertTrue(player["golrie_device_explained", false])
        player.dialogueOption(2)
        finishDialogue(player)
        player.dialogueOption(3)
        finishDialogue(player)
        assertEquals(null, player.dialogue)
    }

    @Test
    fun `Golrie gate visibly opens closes and always requires key`() {
        val player = createPlayer(Tile(2515, 9575))
        player[WaterfallQuest.QUEST] = "started"
        val gate = GameObjects.find(Tile(2515, 9575), "gate_33_closed")
        player.objectOption(gate, "Open")
        tick(2)
        assertEquals(Tile(2515, 9575), player.tile)
        assertFalse(player["waterfall_golrie_freed", false])
        assertTrue(player.dialogue != null)
        player.skipDialogues()
        assertEquals(gate, GameObjects.find(gate.tile, "gate_33_closed"))
        player.inventory.add("a_key")
        player.objectOption(gate, "Open")
        tick(1)
        assertEquals(Tile(2515, 9575), player.tile)
        assertEquals(null, GameObjects.findOrNull(gate.tile, "gate_33_closed"))
        tick(5)
        assertEquals(Tile(2515, 9576), player.tile)
        assertTrue(player["waterfall_golrie_freed", false])
        assertEquals(gate, GameObjects.find(gate.tile, "gate_33_closed"))
        player.inventory.remove("a_key")
        player.objectOption(gate, "Open")
        tick(2)
        assertTrue(player.tile.y > gate.tile.y)
        player.skipDialogues()
        assertEquals(gate, GameObjects.find(gate.tile, "gate_33_closed"))
        player.inventory.add("a_key")
        player.objectOption(gate, "Open")
        tick(6)
        assertEquals(Tile(2515, 9574), player.tile)
        assertEquals(gate, GameObjects.find(gate.tile, "gate_33_closed"))
    }

    @Test
    fun `Golrie warns before quest and locks himself in after completion`() {
        val player = createPlayer(Tile(2515, 9575))
        val gate = GameObjects.find(Tile(2515, 9575), "gate_33_closed")
        player.inventory.add("a_key")
        player.objectOption(gate, "Open")
        tick(2)
        finishDialogue(player)
        assertFalse(player["waterfall_golrie_freed", false])
        assertTrue(player.inventory.contains("a_key"))
        player.inventory.remove("a_key")
        player[WaterfallQuest.QUEST] = "completed"
        player["waterfall_golrie_key_given"] = true
        player.objectOption(gate, "Open")
        tick(2)
        assertTrue(player.messages.contains("Golrie has locked himself in."))
        assertEquals(gate, GameObjects.find(gate.tile, "gate_33_closed"))
    }

    @Test
    fun `Golrie gives pebble before book reading without skipping book progress`() {
        val player = createPlayer(Tile(2515, 9576))
        player[WaterfallQuest.QUEST] = "found_hudon"
        player["waterfall_golrie_freed"] = true
        player.inventory.add("a_key")
        val golrie = NPCs.find(player.tile.regionLevel, "golrie")
        player.npcOption(golrie, "Talk-to")
        tick(2)
        finishDialogue(player)
        assertTrue(player.inventory.contains("glarials_pebble"))
        assertEquals("found_hudon", player[WaterfallQuest.QUEST, "unstarted"])
        player.npcOption(golrie, "Talk-to")
        tick(2)
        finishDialogue(player)
        assertEquals(1, player.inventory.count("glarials_pebble"))
        player.inventory.add("book_on_baxtorian")
        player.itemOption("Read", "book_on_baxtorian")
        tick(2)
        assertEquals("found_pebble", player[WaterfallQuest.QUEST, "unstarted"])
    }

    @Test
    fun `Rope route enters waterfall and exit returns to ledge`() {
        val player = createPlayer(Tile(2510, 3493))
        player[WaterfallQuest.QUEST] = "found_urn"
        player.inventory.add("rope")
        player.equipment.add("glarials_amulet")
        player.objectOption(GameObjects.find(Tile(2509, 3493), "waterfall_raft"), "Board")
        tick(4)
        assertEquals(Tile(2511, 3481), player.tile)
        player.itemOnObject(GameObjects.find(Tile(2512, 3468), "waterfall_rock"), player.inventory.indexOf("rope"))
        val animations = mutableSetOf<Int>()
        var ropeVisible = false
        var throwEffectVisible = false
        var pullStartedAt = -1
        repeat(20) {
            tick()
            animations.add(player.visuals.animation.force)
            if (player.visuals.secondaryGraphic.id == 67) {
                throwEffectVisible = true
                assertEquals(65, player.visuals.secondaryGraphic.delay)
            }
            if (player.visuals.animation.force == 776 && pullStartedAt == -1) {
                pullStartedAt = it
                assertEquals(72, player.visuals.exactMovement.endDelay)
            }
            if (pullStartedAt != -1 && it < pullStartedAt + 3) {
                assertEquals(Tile(2512, 3469), player.tile)
            }
            if (GameObjects.findOrNull(Tile(2512, 3468), "waterfall_rock_rope") != null) {
                ropeVisible = true
                for (y in 3469..3475) {
                    assertTrue(GameObjects.findOrNull(Tile(2512, y), "waterfall_crossing_rope") != null)
                }
            }
        }
        assertTrue(ropeVisible)
        assertTrue(throwEffectVisible)
        assertTrue(2910 in animations)
        assertTrue(776 in animations)
        assertTrue(GameObjects.findOrNull(Tile(2512, 3468), "waterfall_rock") != null)
        for (y in 3469..3475) {
            assertTrue(GameObjects.findOrNull(Tile(2512, y), "waterfall_crossing_rope") == null)
        }
        assertEquals(Tile(2513, 3468), player.tile)
        assertTrue(player.inventory.contains("rope"))
        player.itemOnObject(GameObjects.find(Tile(2512, 3465), "dead_tree_13"), player.inventory.indexOf("rope"))
        tick(3)
        assertEquals(Tile(2511, 3463), player.tile)
        player.objectOption(GameObjects.find(Tile(2511, 3464), "waterfall_entrance"), "Enter")
        tick(3)
        assertEquals(Tile(2575, 9861), player.tile)
        player.objectOption(GameObjects.find(Tile(2574, 9860), "waterfall_exit"), "Open")
        tick(3)
        assertEquals(Tile(2511, 3463), player.tile)
    }

    @Test
    fun `All eighteen rune placements persist and duplicates cost nothing`() {
        val player = createPlayer(Tile(2565, 9910))
        player[WaterfallQuest.QUEST] = "entered_dungeon"
        WaterfallQuest.RUNES.forEach { player.inventory.add(it, 6) }
        with(questScript) {
            assertFalse(player.placeRune(Tile(2600, 9909), "air_rune"))
            for (pillar in WaterfallQuest.PILLARS) {
                for (rune in WaterfallQuest.RUNES) {
                    assertTrue(player.placeRune(pillar, rune))
                    assertFalse(player.placeRune(pillar, rune))
                }
            }
        }
        assertEquals(WaterfallQuest.ALL_RUNES, player["waterfall_pillars", 0])
        WaterfallQuest.RUNES.forEach { assertEquals(0, player.inventory.count(it)) }
    }

    @Test
    fun `Locked door stays locked without dungeon key`() {
        val player = createPlayer(Tile(2566, 9900))
        val door = GameObjects.find(Tile(2566, 9901), "door_29_closed")
        player.objectOption(door, "Open")
        tick(3)
        assertTrue(player.tile.y <= 9901)
        assertTrue(GameObjects.contains(door))
        player.inventory.add("a_key_waterfall_dungeon")
        player.objectOption(door, "Open")
        tick(3)
        assertEquals(Tile(2566, 9902), player.tile)
    }

    @Test
    fun `West path door opens walks through and closes in both directions`() {
        val player = createPlayer(Tile(2568, 9893))
        player.inventory.add("a_key_waterfall_dungeon")
        val door = GameObjects.find(Tile(2568, 9893), "door_29_closed")
        player.objectOption(door, "Open")
        tick(1)
        assertEquals(null, GameObjects.findOrNull(door.tile, "door_29_closed"))
        assertEquals(Tile(2568, 9893), player.tile)
        tick(5)
        assertEquals(Tile(2568, 9894), player.tile)
        assertEquals(door, GameObjects.find(door.tile, "door_29_closed"))
        player.itemOnObject(door, player.inventory.indexOf("a_key_waterfall_dungeon"))
        tick(6)
        assertEquals(Tile(2568, 9892), player.tile)
        assertEquals(door, GameObjects.find(door.tile, "door_29_closed"))
    }

    @Test
    fun `Tomb rejects weapons equipment and runes but permits food`() {
        val player = createPlayer(Tile(2558, 3444))
        player[WaterfallQuest.QUEST] = "found_pebble"
        player.inventory.add("glarials_pebble")
        player.inventory.add("lobster")
        with(questScript) {
            assertTrue(player.canEnterTomb())
            player.inventory.add("air_rune")
            assertFalse(player.canEnterTomb())
            player.inventory.remove("air_rune")
            player.inventory.add("bronze_sword")
            assertFalse(player.canEnterTomb())
            player.inventory.remove("bronze_sword")
            player.equipment.add("bronze_sword")
            assertFalse(player.canEnterTomb())
        }
    }

    @Test
    fun `Enter tomb collect relics and leave by ladder`() {
        val player = createPlayer(Tile(2558, 3443))
        player[WaterfallQuest.QUEST] = "found_pebble"
        player["god_mode"] = true
        player.inventory.add("glarials_pebble")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "thick_skin")
        player.itemOnObject(GameObjects.find(Tile(2558, 3444), "glarials_tombstone"), player.inventory.indexOf("glarials_pebble"))
        tick(10)
        assertEquals(Tile(2555, 9844), player.tile)
        assertFalse(player.praying("thick_skin"))
        with(scripts.filterIsInstance<QuickPrayers>().single()) {
            player.togglePrayer(0, PrayerConfigs.ACTIVE_PRAYERS, false)
        }
        assertFalse(player.praying("thick_skin"))
        player.tele(2529, 9844)
        player.objectOption(GameObjects.find(Tile(2530, 9844), "glarials_chest_closed"), "Open")
        tick(8)
        assertTrue(player.inventory.contains("glarials_amulet"), "${player.tile}: ${player.messages}")
        assertTrue(player.inventory.contains("glarials_amulet"))
        player.tele(2542, 9812)
        player.objectOption(GameObjects.find(Tile(2542, 9811), "glarials_tomb"), "Search")
        tick(8)
        assertTrue(player.inventory.contains("glarials_urn"))
        assertEquals("found_urn", player[WaterfallQuest.QUEST, "unstarted"])
        player.tele(2555, 9844)
        player.objectOption(GameObjects.find(Tile(2556, 9844)) { it.intId == 1757 }, "Climb-up")
        tick(12)
        assertEquals(Tile(2559, 3444), player.tile)
    }

    @Test
    fun `Statue transports to raised chamber and urn awards treasure once`() {
        val player = createPlayer(Tile(2565, 9915))
        player[WaterfallQuest.QUEST] = "entered_dungeon"
        player["waterfall_pillars"] = WaterfallQuest.ALL_RUNES
        player.inventory.add("glarials_amulet")
        player.inventory.add("glarials_urn")
        val statue = GameObjects.find(Tile(2565, 9916), "waterfall_glarial_statue")
        player.itemOnObject(statue, player.inventory.indexOf("glarials_amulet"))
        tick(8)
        assertEquals("restored_amulet", player[WaterfallQuest.QUEST, "unstarted"])
        assertEquals(Tile(2603, 9914), player.tile)
        assertEquals(null, GameObjects.findOrNull(player.tile, "waterfall_glarial_statue"))
        val chalice = GameObjects.find(Tile(2603, 9910), "waterfall_chalice")
        player.itemOnObject(chalice, player.inventory.indexOf("glarials_urn"))
        tick(12)
        assertEquals("completed", player[WaterfallQuest.QUEST, "unstarted"])
        assertEquals(1, player["quest_points", 0])
        assertEquals(2, player.inventory.count("gold_bar"))
        assertEquals(2, player.inventory.count("diamond"))
        assertEquals(40, player.inventory.count("mithril_seeds"))
        assertTrue(player.inventory.contains("glarials_urn_empty"))
        assertEquals(13750.0, player.experience.get(Skill.Attack))
        assertEquals(13750.0, player.experience.get(Skill.Strength))
        with(questScript) { assertFalse(player.completeQuest()) }
        assertEquals(1, player["quest_points", 0])
    }

    @Test
    fun `Full inventory preserves urn and rewards for retry`() {
        val player = createPlayer(Tile(2603, 9915))
        player[WaterfallQuest.QUEST] = "restored_amulet"
        player.inventory.add("glarials_urn")
        player.inventory.add("lobster", 23)
        with(questScript) { assertFalse(player.completeQuest()) }
        assertEquals("restored_amulet", player[WaterfallQuest.QUEST, "unstarted"])
        assertTrue(player.inventory.contains("glarials_urn"))
        player.inventory.remove("lobster")
        with(questScript) { assertTrue(player.completeQuest()) }
        assertEquals("completed", player[WaterfallQuest.QUEST, "unstarted"])
    }
}
