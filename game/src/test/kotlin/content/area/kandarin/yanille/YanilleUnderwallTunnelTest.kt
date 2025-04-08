package content.area.kandarin.yanille

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class YanilleUnderwallTunnelTest : WorldTest() {

    @Test
    fun `Climb south under wall`() {
        val player = createPlayer(tile = Tile(2575, 3112))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(2575, 3111), "yanille_underwall_tunnel_hole"]!!

        player.objectOption(monkeyBars, "Climb-into")
        tick(7)

        assertEquals(Tile(2575, 3107), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb north under wall`() {
        val player = createPlayer(tile = Tile(2575, 3107))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(2575, 3108), "yanille_underwall_tunnel_castle_wall"]!!

        player.objectOption(monkeyBars, "Climb-under")
        tick(7)

        assertEquals(Tile(2575, 3112), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb under wall without level`() {
        val player = createPlayer(tile = Tile(2575, 3107))
        val monkeyBars = objects[Tile(2575, 3108), "yanille_underwall_tunnel_castle_wall"]!!

        player.objectOption(monkeyBars, "Climb-under")
        tick(2)

        assertTrue(player.containsMessage("You need at least 15 Agility"))
    }
}