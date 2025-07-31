package content.area.troll_country.god_wars_dungeon

import WorldTest
import content.entity.player.dialogue.continueDialogue
import content.entity.player.effect.energy.runEnergy
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SirGerryTest : WorldTest() {

    @Test
    fun `Sir gerry gives notes`() {
        val player = createPlayer(Tile(2912, 3747))
        val gerry = createNPC("knight_sir_gerry", Tile(2912, 3748))
        println(gerry.def.stringId)

        player.npcOption(gerry, "Talk-to")
        tick()
        repeat(5) {
            player.continueDialogue()
        }
        assertTrue(player.inventory.contains("knights_notes"))
    }

    @Test
    fun `Sir gerry dies when you leave the area`() {
        val player = createPlayer(Tile(2912, 3747))
        player.inventory.add("knights_notes_opened")

        assertFalse(player["godwars_knights_notes", false])
        player.tele(2899, 3711)
        tick(2)
        assertTrue(player["godwars_knights_notes", false])
    }

    @Test
    fun `Enter godwars dungeon`() {
        val player = createPlayer(Tile(2916, 3747))
        player["godwars_knights_notes"] = true
        player.inventory.add("rope")

        val hole = objects[Tile(2917, 3745), "godwars_hole_base"]!!

        player.objectOption(hole, optionIndex = 0) // Tie-rope
        tick()
        assertFalse(player.inventory.contains("rope"))
        assertTrue(player["godwars_entrance_rope", false])
        player.objectOption(hole, optionIndex = 0) // Climb-down
        tick(3)
        assertEquals(Tile(2881, 5310, 2), player.tile)
    }

    @Test
    fun `Windchill drains stats`() {
        val player = createPlayer(Tile(2916, 3747))
        player.levels.set(Skill.Strength, 10)

        tick(10)

        assertTrue(player.runEnergy < 100)
        assertEquals(9, player.levels.get(Skill.Strength))
        assertEquals(90, player.levels.get(Skill.Constitution))
    }
}
