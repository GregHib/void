package content.area.karamja.brimhaven.agility_arena

import WorldTest
import containsMessage
import interfaceOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import walk
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrimhavenAgilityArenaTest : WorldTest() {

    private val arena: BrimhavenAgilityArena
        get() = scripts.filterIsInstance<BrimhavenAgilityArena>().first()

    @Test
    fun `Can't climb down without paying the entrance fee`() {
        val player = createPlayer(Tile(2809, 3193))
        val ladder = GameObjects.find(Tile(2809, 3194), "brimhaven_arena_entrance_ladder")

        player.objectOption(ladder, "Climb-Down")
        tick(3)

        assertEquals(0, player.tile.level)
    }

    @Test
    fun `Pay Cap'n Izzy and climb down into the arena`() {
        val player = createPlayer(Tile(2808, 3191))
        player.inventory.add("coins", 300)
        val izzy = createNPC("capn_izzy_no_beard_brimhaven", Tile(2807, 3190))

        player.npcOption(izzy, "Pay")
        tick(2)
        player.skipDialogues()

        assertEquals(100, player.inventory.count("coins"))
        assertTrue(player["brimhaven_arena_paid", false])

        player.objectOption(GameObjects.find(Tile(2809, 3194), "brimhaven_arena_entrance_ladder"), "Climb-Down")
        tick(8)

        assertEquals(Tile(2805, 9589, 3), player.tile)
        assertFalse(player["brimhaven_arena_paid", false])
    }

    @Test
    fun `Karamja gloves 3 let players in for free`() {
        val player = createPlayer(Tile(2809, 3193))
        player.equipment.set(EquipSlot.Hands.index, "karamja_gloves_3")

        player.objectOption(GameObjects.find(Tile(2809, 3194), "brimhaven_arena_entrance_ladder"), "Climb-Down")
        tick(4)

        assertEquals(Tile(2805, 9589, 3), player.tile)
    }

    @Test
    fun `Tagging two dispensers in a row gives a ticket`() {
        val player = createPlayer(Tile(2805, 9580, 3))
        tick()
        arena.active = Tile(2805, 9579, 3)

        player.objectOption(GameObjects.find(Tile(2805, 9579, 3), "brimhaven_arena_ticket_dispenser"), "Tag")
        tick(2)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("agility_arena_ticket"))
        assertTrue(player.containsMessage("tag the next pillar"))
        assertTrue(player["brimhaven_arena_active", false])

        player.tele(2805, 9569, 3)
        tick()
        arena.movePillar()
        arena.active = Tile(2805, 9568, 3)
        player.objectOption(GameObjects.find(Tile(2805, 9568, 3), "brimhaven_arena_ticket_dispenser"), "Tag")
        tick(2)

        assertEquals(1, player.inventory.count("agility_arena_ticket"))
    }

    @Test
    fun `Missing a pillar makes the player inactive`() {
        val player = createPlayer(Tile(2805, 9580, 3))
        tick()
        player["brimhaven_arena_active"] = true

        arena.movePillar()

        assertFalse(player["brimhaven_arena_active", false])
    }

    @Test
    fun `Tagging a dispenser the arrow isn't over gives nothing`() {
        val player = createPlayer(Tile(2805, 9580, 3))
        tick()
        arena.active = Tile(2761, 9546, 3)

        player.objectOption(GameObjects.find(Tile(2805, 9579, 3), "brimhaven_arena_ticket_dispenser"), "Tag")
        tick(2)

        assertTrue(player.containsMessage("Tag the pillar indicated by the yellow arrow"))
        assertFalse(player["brimhaven_arena_tagged", false])
    }

    @Test
    fun `Cross the log balance`() {
        val player = createPlayer(Tile(2805, 9556, 3))
        player.levels.set(Skill.Agility, 99)

        player.objectOption(GameObjects.find(Tile(2805, 9554, 3), "brimhaven_arena_log_balance"), "Walk-on")
        tick(12)

        assertEquals(Tile(2805, 9548, 3), player.tile)
        assertEquals(12.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Planks cross the gap not into the next lane`() {
        val player = createPlayer(Tile(2796, 9589, 3))
        player.levels.set(Skill.Agility, 99)

        player.objectOption(GameObjects.find(Tile(2797, 9589, 3), "brimhaven_arena_plank_2"), "Walk-on")
        tick(10)

        // Across the lane or into the pit below a broken plank, but never north into the next lane
        assertEquals(9589, player.tile.y)
        assertTrue(player.tile.x > 2797)
    }

    @Test
    fun `Climb across the hand holds`() {
        val player = createPlayer(Tile(2793, 9591, 3))
        player.levels.set(Skill.Agility, 99)

        player.objectOption(GameObjects.find(Tile(2792, 9592, 3), "brimhaven_arena_hand_holds"), "Climb-across")
        tick(20)

        assertEquals(Tile(2785, 9592, 3), player.tile)
        assertEquals(22.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Hand holds need 20 agility`() {
        val player = createPlayer(Tile(2793, 9591, 3))

        player.objectOption(GameObjects.find(Tile(2792, 9592, 3), "brimhaven_arena_hand_holds"), "Climb-across")
        tick(3)

        assertTrue(player.containsMessage("You need an Agility of at least 20"))
        assertEquals(Tile(2792, 9592, 3), player.tile)
    }

    @Test
    fun `Pressure pad knocks back players below level 20`() {
        val player = createPlayer(Tile(2798, 9579, 3))

        player.walk(Tile(2801, 9579, 3))
        tick(8)

        assertEquals(Tile(2798, 9579, 3), player.tile)
        assertTrue(player.containsMessage("You were hit by some falling rocks!"))
    }

    @Test
    fun `Exchange tickets for agility experience`() {
        val player = createPlayer(Tile(2810, 3192))
        player.inventory.add("agility_arena_ticket", 10)
        player.open("brimhaven_arena_ticket_exchange")

        player.interfaceOption("brimhaven_arena_ticket_exchange", "experience_10", "Buy")

        assertEquals(0, player.inventory.count("agility_arena_ticket"))
        assertEquals(2480.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Karamja gloves 2 give extra ticket experience but gloves 1 don't`() {
        val player = createPlayer(Tile(2810, 3192))
        player.inventory.add("agility_arena_ticket", 2)
        player.open("brimhaven_arena_ticket_exchange")

        player.equipment.set(EquipSlot.Hands.index, "karamja_gloves_1")
        player.interfaceOption("brimhaven_arena_ticket_exchange", "experience_1", "Buy")
        assertEquals(240.0, player.experience.get(Skill.Agility))

        player.equipment.set(EquipSlot.Hands.index, "karamja_gloves_2")
        player.interfaceOption("brimhaven_arena_ticket_exchange", "experience_1", "Buy")
        assertEquals(504.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Exchange tickets for herbs`() {
        val player = createPlayer(Tile(2810, 3192))
        player.inventory.add("agility_arena_ticket", 5)
        player.open("brimhaven_arena_ticket_exchange")

        player.interfaceOption("brimhaven_arena_ticket_exchange", "toadflax", "Buy")
        player.interfaceOption("brimhaven_arena_ticket_exchange", "snapdragon", "Buy")

        assertEquals(2, player.inventory.count("agility_arena_ticket"))
        assertEquals(1, player.inventory.count("clean_toadflax"))
        assertEquals(0, player.inventory.count("clean_snapdragon"))
        assertTrue(player.containsMessage("Snapdragon costs 10 tickets."))
    }
}
