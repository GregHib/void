package content.quest.free.ernest_the_chicken

import WorldTest
import content.entity.effect.stunned
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import skipDialogues
import walk
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
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
        p.dialogueOption(1)
        p.skipDialogues()
        p.dialogueOption(1)
        p.skipDialogues()
        assertEquals("parts", p.quest(ERNEST))
        assertEquals(2, p.questStage(ERNEST))
    }

    @Test
    fun `Demanding Ernest be changed back also opens the parts stage`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "started"
        p.talk("professor_oddenstein")
        p.dialogueOption(1)
        p.skipDialogues()
        p.dialogueOption(2)
        p.skipDialogues()
        assertEquals("parts", p.quest(ERNEST))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6])
    fun `Every incomplete parts conversation leaves the carried parts untouched`(mask: Int) {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "parts"
        val parts = listOf("pressure_gauge", "rubber_tube", "oil_can")
        for ((i, part) in parts.withIndex()) if (mask and (1 shl i) != 0) p.inventory.add(part)
        p.talk("professor_oddenstein")
        assertEquals("parts", p.quest(ERNEST))
        for ((i, part) in parts.withIndex()) assertEquals(if (mask and (1 shl i) != 0) 1 else 0, p.inventory.count(part))
        assertEquals(0, p["quest_points", 0])
        assertNull(p.get<kotlinx.coroutines.Job>("ernest_restore_job"))
    }

    @Test
    fun `Veronica discusses the transformation without changing the parts stage`() {
        val p = createPlayer(Tile(3110, 3330))
        p[ERNEST] = "parts"
        p.talk("veronica")
        assertEquals("parts", p.quest(ERNEST))
        assertNull(p.dialogue)
    }

    @Test
    fun `Professor ordinary conversations remain available before the quest`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        for (option in 1..2) {
            p.talk("professor_oddenstein")
            p.dialogueOption(option)
            p.skipDialogues()
            assertEquals("unstarted", p.quest(ERNEST))
            assertNull(p.dialogue)
        }
    }

    @Test
    fun `Declining Veronica leaves the quest unstarted`() {
        val p = createPlayer(Tile(3110, 3330))
        p.talk("veronica")
        p.dialogueOption(2)
        p.skipDialogues()
        assertEquals("unstarted", p.quest(ERNEST))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Poisoned fish food recipe handles both item orders and makes one portion without a menu`(reverse: Boolean) {
        val p = createPlayer()
        p.inventory.add("fish_food", 2)
        p.inventory.add("poison", 2)
        p.inventory.add("logs", 24)
        p.itemOnItem(if (reverse) "fish_food" else "poison", if (reverse) "poison" else "fish_food")
        tick(3)
        assertEquals(1, p.inventory.count("poisoned_fish_food"))
        assertEquals(1, p.inventory.count("fish_food"))
        assertEquals(1, p.inventory.count("poison"))
        assertEquals(24, p.inventory.count("logs"))
        assertEquals(1, p.inventory.spaces)
        assertNull(p.dialogue)
    }

    @Test
    fun `Poisoned fountain is personal and replaces lost gauges without duplicates`() {
        val p = createPlayer(Tile(3086, 3335))
        val other = createPlayer(Tile(3086, 3335))
        val f = GameObjects.find(Tile(3087, 3334), "fountain")
        p.objectOption(f, "Search")
        tick(3)
        p.skipDialogues()
        assertFalse(p.inventory.contains("pressure_gauge"))
        p.inventory.add("fish_food")
        p.inventory.add("poison")
        // Let the pending area-achievement action finish before the weak queued recipe starts.
        tick()
        p.itemOnItem("fish_food", "poison")
        tick(3)
        assertTrue(p.inventory.contains("poisoned_fish_food"))
        p.itemOnObject(f, p.inventory.indexOf("poisoned_fish_food"))
        tick(3)
        assertTrue(p["ernest_fountain_poisoned", false])
        assertFalse(other["ernest_fountain_poisoned", false])
        p.objectOption(f, "Search")
        tick(3)
        p.skipDialogues()
        assertEquals(1, p.inventory.count("pressure_gauge"))
        p.objectOption(f, "Search")
        tick(3)
        p.skipDialogues()
        assertEquals(1, p.inventory.count("pressure_gauge"))
        p.inventory.remove("pressure_gauge")
        p.objectOption(f, "Search")
        tick(3)
        p.skipDialogues()
        assertEquals(1, p.inventory.count("pressure_gauge"))
    }

    @Test
    fun `Manor fountain bites item users and refuses poisoned water without filling containers`() {
        val p = createPlayer(Tile(3086, 3335))
        val fountain = GameObjects.find(Tile(3087, 3334), "fountain")
        p.inventory.add("bucket")
        p.itemOnObject(fountain, p.inventory.indexOf("bucket"))
        tick(3)
        p.skipDialogues()
        assertTrue(p.messages.contains("Something in the water bites you."))
        assertTrue(p.inventory.contains("bucket"))
        assertFalse(p.inventory.contains("bucket_of_water"))
        p["ernest_fountain_poisoned"] = true
        p.itemOnObject(fountain, p.inventory.indexOf("bucket"))
        tick(3)
        assertNotNull(p.dialogue)
        p.skipDialogues()
        assertTrue(p.inventory.contains("bucket"))
        assertFalse(p.inventory.contains("bucket_of_water"))
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
    fun `Candle sconce and inner lever open the walls westward and close behind the player`() {
        val p = createPlayer(Tile(3098, 3360))
        fun cross(trigger: Tile, id: String, option: String, destination: Tile) {
            p.objectOption(GameObjects.find(trigger, id), option)
            var sawOpen = false
            var sawCloseAfterCrossing = false
            val passageSteps = mutableListOf(p.tile)
            repeat(16) {
                val previousTile = p.tile
                tick()
                if (passageSteps.lastOrNull() != p.tile) passageSteps.add(p.tile)
                val south = GameObjects.findOrNull(Tile(3096, 3358), "ernest_secret_wall_south_opened")
                val north = GameObjects.findOrNull(Tile(3096, 3359), "ernest_secret_wall_north_opened")
                if (south != null || north != null) {
                    if (!sawOpen) assertEquals(Tile(if (option == "Search") 3097 else 3096, 3359), p.tile, "Line up before the wall opens")
                    sawOpen = true
                    assertNotNull(south)
                    assertNotNull(north)
                    assertEquals(3, south.rotation)
                    assertEquals(1, north.rotation)
                    assertNull(GameObjects.findOrNull(Tile(3097, 3358), "ernest_secret_wall_south_closed"))
                    assertNull(GameObjects.findOrNull(Tile(3097, 3359), "ernest_secret_wall_north_closed"))
                } else if (sawOpen && !sawCloseAfterCrossing) {
                    sawCloseAfterCrossing = true
                    assertEquals(destination, p.tile, "Close only after crossing")
                    assertEquals(destination, previousTile, "Allow the client a tick to finish the last step before closing")
                }
            }
            assertTrue(sawCloseAfterCrossing, "Close behind the player in both directions")
            assertTrue(sawOpen, "Both triggers should visibly open the wall into the western room")
            if (passageSteps.lastOrNull() != p.tile) passageSteps.add(p.tile)
            for ((from, to) in passageSteps.zipWithNext()) {
                assertTrue(from.x == to.x || from.y == to.y, "Passage movement must turn before crossing in either direction: $passageSteps")
            }
            assertTrue(passageSteps.any { it.y == 3359 && it.x != destination.x }, "Visit the tile in front of the opening first: $passageSteps")
            assertEquals(destination, p.tile, p.messages.joinToString())
            assertNotNull(GameObjects.findOrNull(Tile(3097, 3358), "ernest_secret_wall_south_closed"))
            assertNotNull(GameObjects.findOrNull(Tile(3097, 3359), "ernest_secret_wall_north_closed"))
            assertNull(GameObjects.findOrNull(Tile(3096, 3358), "ernest_secret_wall_south_opened"))
            assertNull(GameObjects.findOrNull(Tile(3096, 3359), "ernest_secret_wall_north_opened"))
        }
        cross(Tile(3097, 3360), "ernest_candle_sconce", "Search", Tile(3096, 3359))
        cross(Tile(3096, 3357), "ernest_secret_exit_lever", "Pull", Tile(3097, 3359))
    }

    @Test
    fun `Interrupted passage closes and can be used again`() {
        val p = createPlayer(Tile(3098, 3360))
        val sconce = GameObjects.find(Tile(3097, 3360), "ernest_candle_sconce")
        p.objectOption(sconce, "Search")
        tickIf(12) { GameObjects.findOrNull(Tile(3096, 3358), "ernest_secret_wall_south_opened") == null }
        assertNotNull(GameObjects.findOrNull(Tile(3096, 3358), "ernest_secret_wall_south_opened"))
        p.walk(p.tile.addY(1))
        tick(14)
        assertNotNull(GameObjects.findOrNull(Tile(3097, 3358), "ernest_secret_wall_south_closed"))
        assertNotNull(GameObjects.findOrNull(Tile(3097, 3359), "ernest_secret_wall_north_closed"))
        p.tele(3098, 3360)
        p.objectOption(sconce, "Search")
        tick(12)
        assertEquals(Tile(3096, 3359), p.tile, p.messages.joinToString())
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

    @Test
    fun `Restoration uses one existing chicken and only transforms it for the quest player`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        val other = createPlayer(Tile(3109, 3366, 2))
        p[ERNEST] = "parts"
        other[ERNEST] = "parts"
        for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) p.inventory.add(part)
        val professor = NPCs.find(p.tile.regionLevel, "professor_oddenstein")
        professor.tele(3110, 3367, 2)
        val chicken = NPCs.find(p.tile.regionLevel, "chicken_draynor")
        val count = NPCs.count()
        p.npcOption(professor, "Talk-to")
        tickIf(30) { p.dialogue == null }
        p.skipDialogues()
        for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) assertFalse(p.inventory.contains(part))
        val graphics = mutableListOf<Int>()
        var placed = false
        var sawHuman = false
        var ticks = 0
        while (p.dialogue == null && ticks++ < 40) {
            for (graphic in listOf(chicken.visuals.secondaryGraphic.id, chicken.visuals.primaryGraphic.id)) {
                if (graphic >= 0 && graphics.lastOrNull() != graphic) graphics.add(graphic)
            }
            if (professor.visuals.animation.stand == AnimationDefinitions.get("take").id) {
                placed = true
                assertEquals(Tile(3111, 3367, 2), professor.tile)
            }
            if (chicken.def(p).stringId == "ernest_human") sawHuman = true
            assertNotEquals("ernest_human", chicken.def(other).stringId)
            assertFalse(chicken.stunned)
            assertEquals(count, NPCs.count())
            assertEquals("parts", p.quest(ERNEST))
            tick()
        }
        assertTrue(placed)
        assertTrue(sawHuman || chicken.def(p).stringId == "ernest_human")
        val impact = graphics.indexOf(GraphicDefinitions.get("weaken_impact").id)
        val birds = graphics.indexOf(GraphicDefinitions.get("stun_long").id)
        assertTrue(impact >= 0 && birds > impact, graphics.toString())
        assertTrue(chicken.tile.distanceTo(p.tile) <= 1, "Ernest should approach the player")
        assertNotNull(p.dialogue)
        p.skipDialogues()
        assertEquals("completed", p.quest(ERNEST))
        assertEquals(4, p["quest_points", 0])
        assertTrue(p.viewport!!.npcForms.isEmpty())
        assertEquals(count, NPCs.count())
    }

    @Test
    fun `Walking away during hand-in refunds the parts without spawning actors`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "parts"
        for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) p.inventory.add(part)
        val count = NPCs.count()
        p.npcOption(NPCs.find(p.tile.regionLevel, "professor_oddenstein"), "Talk-to")
        tickIf(30) { p.dialogue == null }
        p.skipDialogues()
        p.walk(p.tile.addX(-1))
        tick(3)
        assertEquals(count, NPCs.count())
        assertTrue(p.viewport!!.npcForms.isEmpty())
        assertEquals("parts", p.quest(ERNEST))
        for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) assertTrue(p.inventory.contains(part))
        assertEquals(0, p["quest_points", 0])
    }

    @Test
    fun `Repeatedly dismissing human Ernest restores the chicken and never duplicates rewards or NPCs`() {
        val p = createPlayer(Tile(3110, 3366, 2))
        p[ERNEST] = "parts"
        for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) p.inventory.add(part)
        val professor = NPCs.find(p.tile.regionLevel, "professor_oddenstein")
        val chicken = NPCs.find(p.tile.regionLevel, "chicken_draynor")
        val count = NPCs.count()
        repeat(3) {
            p.npcOption(professor, "Talk-to")
            tickIf(30) { p.dialogue == null }
            p.skipDialogues()
            tickIf(40) { p.dialogue == null }
            assertEquals("ernest_human", chicken.def(p).stringId)
            p.closeInterfaces()
            tick(3)
            assertTrue(p.viewport!!.npcForms.isEmpty())
            assertNotEquals("ernest_human", chicken.def(p).stringId)
            assertEquals(count, NPCs.count())
            assertEquals("parts", p.quest(ERNEST))
            for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) assertEquals(1, p.inventory.count(part))
            assertEquals(0, p["quest_points", 0])
        }
    }

    private fun Player.talk(id: String) {
        npcOption(NPCs.find(tile.regionLevel, id), "Talk-to")
        tickIf(30) { dialogue == null }
        repeat(60) {
            if (dialogue != null) {
                skipDialogues()
                if (dialogue != null) return
            }
            if (suspension == null) return
            tick()
        }
        fail("Dialogue or timed quest sequence did not settle")
    }
}
