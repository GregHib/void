package content.quest.free.ernest_the_chicken

import WorldTest
import content.quest.quest
import content.quest.questStage
import dialogueOption
import itemOnItem
import itemOnObject
import kotlinx.coroutines.runBlocking
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import kotlin.test.*

class ErnestTheChickenTest : WorldTest() {
    override var loadNpcs = true

    @Test
    fun `Veronica acceptance and professor explanation advance the quest`() {
        val p = createPlayer(Tile(3110, 3330))
        p.talk("veronica")
        assertEquals("unstarted", p.quest(ERNEST))
        p.dialogueOption(1)
        p.skipDialogues()
        assertEquals("started", p.quest(ERNEST))
        assertEquals(1, p.questStage(ERNEST))
        p.tele(3110, 3366, 2)
        p.talk("professor_oddenstein")
        assertEquals("parts", p.quest(ERNEST))
        assertEquals(2, p.questStage(ERNEST))
    }

    @Test
    fun `Declining Veronica leaves the quest unstarted`() {
        val p = createPlayer(Tile(3110, 3330))
        p.talk("veronica")
        p.dialogueOption(2)
        assertEquals("unstarted", p.quest(ERNEST))
    }

    @Test
    fun `Poisoned fountain is personal and replaces lost gauges without duplicates`() {
        val p = createPlayer(Tile(3086, 3335))
        val other = createPlayer(Tile(3086, 3335))
        val f = GameObjects.find(Tile(3087, 3334), "fountain")
        p.objectOption(f, "Search")
        tick(3)
        assertFalse(p.inventory.contains("pressure_gauge"))
        p.inventory.add("fish_food")
        p.inventory.add("poison")
        p.itemOnItem("fish_food", "poison")
        tick(3)
        assertTrue(p.inventory.contains("poisoned_fish_food"))
        p.itemOnObject(f, p.inventory.indexOf("poisoned_fish_food"))
        tick(3)
        assertTrue(p["ernest_fountain_poisoned", false])
        assertFalse(other["ernest_fountain_poisoned", false])
        p.objectOption(f, "Search")
        tick(3)
        assertEquals(1, p.inventory.count("pressure_gauge"))
        p.objectOption(f, "Search")
        tick(3)
        assertEquals(1, p.inventory.count("pressure_gauge"))
        p.inventory.remove("pressure_gauge")
        p.objectOption(f, "Search")
        tick(3)
        assertEquals(1, p.inventory.count("pressure_gauge"))
    }

    @Test
    fun `Compost requires spade and space and key opens the tube room`() {
        val p = createPlayer(Tile(3086, 3361))
        val heap = GameObjects.find(Tile(3084, 3360), "ernest_compost")
        p.objectOption(heap, "Search")
        tick(8)
        assertFalse(p.inventory.contains("grimy_key"))
        p.inventory.add("spade")
        p.inventory.add("logs", 27)
        p.objectOption(heap, "Search")
        tick(8)
        assertFalse(p.inventory.contains("grimy_key"))
        p.inventory.remove("logs")
        p.objectOption(heap, "Search")
        tick(8)
        assertTrue(p.inventory.contains("grimy_key"), p.messages.joinToString())
        p.tele(3107, 3367)
        val door = GameObjects.find(Tile(3107, 3368), "door_11_closed")
        p.inventory.remove("grimy_key")
        p.objectOption(door, "Open")
        tick(8)
        assertFalse(p["ernest_tube_unlocked", false])
        p.inventory.add("grimy_key")
        p.objectOption(door, "Open")
        tick(8)
        assertTrue(p["ernest_tube_unlocked", false])
        assertTrue(p.tile.y >= 3368)
    }

    @Test
    fun `Full inventory exchanges three parts for stacking rewards only once`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "parts"
        p.inventory.add("pressure_gauge")
        p.inventory.add("rubber_tube")
        p.inventory.add("oil_can")
        p.inventory.add("logs", 25)
        p.talk("professor_oddenstein")
        assertEquals("completed", p.quest(ERNEST))
        assertEquals(3, p.questStage(ERNEST))
        assertEquals(4, p["quest_points", 0])
        assertEquals(3000, p.inventory.count("coins"))
        assertEquals(10, p.inventory.count("egg_noted"))
        assertEquals(300, p.inventory.count("feather"))
        assertEquals(0, p.inventory.spaces)
        assertFalse(p.inventory.contains("pressure_gauge"))
        assertFalse(p.inventory.contains("rubber_tube"))
        assertFalse(p.inventory.contains("oil_can"))
        p.tele(3110, 3330, 0)
        p.talk("veronica")
        assertEquals(4, p["quest_points", 0])
        assertEquals(3000, p.inventory.count("coins"))
    }

    @Test
    fun `Missing part cannot complete or consume supplied parts`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "parts"
        p.inventory.add("rubber_tube")
        p.inventory.add("pressure_gauge")
        p.talk("professor_oddenstein")
        assertEquals("parts", p.quest(ERNEST))
        assertTrue(p.inventory.contains("rubber_tube"))
        assertEquals(0, p["quest_points", 0])
    }

    @Test
    fun `Standard lever solution opens every required passage without changing another player`() {
        val p = createPlayer(Tile(3108, 9746))
        val other = createPlayer(Tile(3110, 9752))
        val levers = listOf(Tile(3108, 9745), Tile(3118, 9752), Tile(3112, 9760), Tile(3108, 9767), Tile(3097, 9767), Tile(3096, 9765))
        val doors = listOf(Tile(3105, 9765), Tile(3100, 9765), Tile(3105, 9760), Tile(3100, 9760), Tile(3100, 9755), Tile(3102, 9763), Tile(3097, 9763), Tile(3108, 9758), Tile(3102, 9758))
        fun pull(i: Int, at: Tile) {
            p.tele(at)
            val before = p["ernest_levers", 0]
            p.objectOption(GameObjects.find(levers[i], "ernest_lever_${'a' + i}"), "Pull")
            tick(6)
            assertEquals(before xor (1 shl (i + 1)), p["ernest_levers", 0], p.messages.joinToString())
        }
        fun pass(i: Int, from: Tile) {
            p.tele(from)
            p.objectOption(GameObjects.find(doors[i], "ernest_puzzle_door_${i + 1}"), "Open")
            tick(8)
            assertEquals(if (i >= 5) Tile(from.x, 2 * doors[i].y - from.y) else Tile(2 * doors[i].x - from.x, from.y), p.tile, "Door ${i + 1} should allow passage; mask=${p["ernest_levers", 0]}, doors=${p["ernest_doors", 0]}, feedback=${p.messages}")
        }
        assertEquals(0, ernestDoorMask(0))
        pull(0, Tile(3108, 9746))
        pull(1, Tile(3118, 9753))
        pass(7, Tile(3108, 9757))
        pull(3, Tile(3108, 9766))
        pass(2, Tile(3106, 9760))
        pass(8, Tile(3102, 9759))
        pull(0, Tile(3108, 9746))
        pull(1, Tile(3118, 9753))
        pass(8, Tile(3102, 9757))
        pass(3, Tile(3101, 9760))
        pass(6, Tile(3097, 9762))
        pull(4, Tile(3097, 9766))
        pull(5, Tile(3097, 9765))
        pass(1, Tile(3099, 9765))
        pass(0, Tile(3104, 9765))
        pull(2, Tile(3112, 9761))
        pass(0, Tile(3106, 9765))
        pass(1, Tile(3101, 9765))
        pull(4, Tile(3097, 9766))
        pass(1, Tile(3099, 9765))
        pass(5, Tile(3102, 9764))
        pass(8, Tile(3102, 9759))
        pass(4, Tile(3101, 9755))
        assertTrue(p.tile.x < 3100)
        assertEquals(0, other["ernest_levers", 0])
        assertEquals(0, other["ernest_doors", 0])
    }

    @Test
    fun `Quest item pickup refuses telegrab and an unopened tube room`() = runBlocking {
        val p = createPlayer(Tile(3101, 9755))
        val oil = FloorItems.add(Tile(3092, 9755), "oil_can")
        assertNull(Items.takeable(p, oil, telegrab = true))
        assertNull(Items.takeable(p, oil))
        p.tele(3093, 9755)
        assertEquals("oil_can", Items.takeable(p, oil))
        val tube = FloorItems.add(Tile(3112, 3368), "rubber_tube")
        p.tele(3112, 3368)
        assertNull(Items.takeable(p, tube))
        p["ernest_tube_unlocked"] = true
        assertNull(Items.takeable(p, tube, telegrab = true))
        assertEquals("rubber_tube", Items.takeable(p, tube))
    }

    @Test
    fun `Cellar transit resets only the travelling player's puzzle and returns to the secret room`() {
        val p = createPlayer(Tile(3108, 9746))
        p.objectOption(GameObjects.find(Tile(3108, 9745), "ernest_lever_a"), "Pull")
        tick(6)
        assertEquals(2, p["ernest_levers", 0])
        assertEquals(1, p["ernest_lever_a_raised", 0])
        p.tele(3117, 9753)
        p.objectOption(GameObjects.find(Tile(3117, 9754), "basic_ladder_up"), "Climb-up")
        tick(6)
        assertEquals(Tile(3092, 3361), p.tile)
        assertEquals(0, p["ernest_levers", 0])
        assertEquals(0, p["ernest_lever_a_raised", 0])
        p.objectOption(GameObjects.find(Tile(3092, 3362), "ernest_cellar_ladder_down"), "Climb-down")
        tick(6)
        assertEquals(Tile(3117, 9753), p.tile)
    }

    @Test
    fun `Candle sconce and inner lever pass between the manor and secret room`() {
        val p = createPlayer(Tile(3098, 3360))
        p.objectOption(GameObjects.find(Tile(3097, 3360), "ernest_candle_sconce"), "Search")
        tick(12)
        assertEquals(Tile(3095, 3359), p.tile, p.messages.joinToString())
        p.objectOption(GameObjects.find(Tile(3096, 3357), "ernest_secret_exit_lever"), "Pull")
        tick(12)
        assertEquals(Tile(3100, 3359), p.tile, p.messages.joinToString())
    }

    @Test
    fun `Non-cellar basic ladders retain their configured teleport without resetting the puzzle`() {
        val p = createPlayer(Tile(3104, 9576))
        p["ernest_levers"] = 22
        p.objectOption(GameObjects.find(Tile(3103, 9576), "basic_ladder_up"), "Climb-up")
        tick(10)
        assertEquals(Tile(3104, 3161), p.tile, p.messages.joinToString())
        assertEquals(22, p["ernest_levers", 0])
    }

    @Test
    fun `One distant click approaches and crosses an unlocked puzzle door from either side`() {
        val p = createPlayer(Tile(3108, 9766))
        p.objectOption(GameObjects.find(Tile(3108, 9767), "ernest_lever_d"), "Pull")
        tick(6)
        val door = GameObjects.find(Tile(3100, 9760), "ernest_puzzle_door_4")
        for ((from, to) in listOf(Tile(3103, 9760) to Tile(3099, 9760), Tile(3097, 9760) to Tile(3101, 9760))) {
            p.tele(from)
            p.objectOption(door, "Open")
            tick(15)
            assertEquals(to, p.tile, "A single click from $from should cross: ${p.messages}")
        }
    }

    private fun Player.talk(id: String) {
        npcOption(NPCs.find(tile.regionLevel, id), "Talk-to")
        tickIf(30) { dialogue == null }
        skipDialogues()
    }
}
