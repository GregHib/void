package content.minigame.pest_control

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

internal class PestControlTest : WorldTest() {

    @Test
    fun `Low combat players can't board the novice lander`() {
        val gangplank = createObject("novice_lander_gangplank", Tile(2658, 2639))
        val player = createPlayer(Tile(2657, 2639), "weak_knight")
        player.combatLevel = 3

        player.objectOption(gangplank, "Cross")
        tick(2)

        assertEquals(Tile(2657, 2639), player.tile)
        assertTrue(player.containsMessage("You need a combat level of 40 or higher to board this lander."))
    }

    @Test
    fun `Board and leave the novice lander`() {
        val gangplank = createObject("novice_lander_gangplank", Tile(2658, 2639))
        val player = createPlayer(Tile(2657, 2639), "boarder")
        player.combatLevel = 50

        player.objectOption(gangplank, "Cross")
        tick(2)

        assertEquals(Tile(2661, 2639), player.tile)
        assertEquals("novice", player["pest_control_lander", ""])

        val ladder = createObject("novice_lander_ladder", Tile(2662, 2640))
        player.objectOption(ladder, "Climb")
        tick(2)

        assertEquals(Tile(2657, 2639), player.tile)
        assertEquals("", player["pest_control_lander", ""])
    }

    @Test
    fun `Lander departs to the island`() {
        settings["pestControl.departureTicks"] = "2"
        settings["pestControl.minimumPlayers"] = "1"
        World.timers.start("pest_control")
        val gangplank = createObject("veteran_lander_gangplank", Tile(2637, 2653))
        val player = createPlayer(Tile(2638, 2653), "defender")
        player.combatLevel = 126

        player.objectOption(gangplank, "Cross")
        tick(5)

        assertTrue(player.contains("pest_control_game"))
        assertNotEquals(Tile(2634, 2653), player.tile)
        assertTrue(NPCs.any { it.id == "void_knight" })
        assertEquals(4, NPCs.count { it.id.startsWith("portal_shield_") })
    }

    @Test
    fun `Commendation point experience`() {
        assertEquals(560, VoidKnightRewards.experience(Skill.Attack, 99, 1))
        assertEquals(5_656, VoidKnightRewards.experience(Skill.Strength, 99, 10))
        assertEquals(31_680, VoidKnightRewards.experience(Skill.Prayer, 99, 100))
        assertEquals(0, VoidKnightRewards.experience(Skill.Magic, 18, 1))
    }
}
