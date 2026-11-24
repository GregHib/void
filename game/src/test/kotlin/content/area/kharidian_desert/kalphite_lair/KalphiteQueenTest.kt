package content.area.kharidian_desert.kalphite_lair

import WorldTest
import content.entity.effect.transform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects

internal class KalphiteQueenTest : WorldTest() {
    @Test
    fun `Kalphite Queen returns to original form after respawning`() {
        val queen = createNPC("kalphite_queen", emptyTile)
        queen.transform("kalphite_queen_airborne")

        queen.respawn(1)
        tick(2)

        assertEquals("", queen.transform)
        assertEquals("kalphite_queen", queen.transformId)
    }

    @Test
    fun `Kalphite Queen emerges when first form is defeated`() {
        val queen = createNPC("kalphite_queen", emptyTile)

        queen.levels.set(Skill.Constitution, 0)
        tick(2)

        assertNotNull(GameObjects.findOrNull(emptyTile, "kalphite_queen_emerging_legs"))
        assertEquals("kalphite_queen_airborne", queen.transform)
        assertEquals(2550, queen.levels.get(Skill.Constitution))
    }

    @Test
    fun `Dead Kalphite Queen doesn't emerge again while waiting to respawn`() {
        val queen = createNPC("kalphite_queen", emptyTile)
        queen.transform("kalphite_queen_airborne")

        queen.levels.set(Skill.Constitution, 0)
        repeat(50) {
            tick()
            assertNull(GameObjects.findOrNull(emptyTile, "kalphite_queen_emerging_legs"))
        }

        assertEquals(0, queen.levels.get(Skill.Constitution))
        assertEquals("", queen.transform)
    }

    @Test
    fun `Dead Kalphite Queen doesn't emerge again when hit after death`() {
        val queen = createNPC("kalphite_queen", emptyTile)
        queen.transform("kalphite_queen_airborne")

        queen.levels.set(Skill.Constitution, 0)
        repeat(50) {
            tick()
            queen.levels.drain(Skill.Constitution, 50)
            assertNull(GameObjects.findOrNull(emptyTile, "kalphite_queen_emerging_legs"))
        }

        assertEquals("", queen.transform)
    }
}
