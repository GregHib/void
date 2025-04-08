package content.skill.agility.shortcut

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class UnderWallTunnelsTest : WorldTest() {

    @Test
    fun `Climb south under yanille wall`() {
        val player = createPlayer(tile = Tile(2575, 3112))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(2575, 3111), "yanille_underwall_tunnel_hole"]!!

        player.objectOption(monkeyBars, "Climb-into")
        tick(8)

        assertEquals(Tile(2575, 3107), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb north under yanille wall`() {
        val player = createPlayer(tile = Tile(2575, 3107))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(2575, 3108), "yanille_underwall_tunnel_castle_wall"]!!

        player.objectOption(monkeyBars, "Climb-under")
        tick(8)

        assertEquals(Tile(2575, 3112), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb under yanille wall without level`() {
        val player = createPlayer(tile = Tile(2575, 3107))
        val monkeyBars = objects[Tile(2575, 3108), "yanille_underwall_tunnel_castle_wall"]!!

        player.objectOption(monkeyBars, "Climb-under")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 15"))
    }

    @Test
    fun `Climb west under edgeville wall`() {
        val player = createPlayer(tile = Tile(3144, 3514))
        player.levels.set(Skill.Agility, 21)
        val monkeyBars = objects[Tile(3143, 3514), "grand_exchange_underwall_tunnel"]!!

        player.objectOption(monkeyBars, "Climb-into")
        tick(8)

        assertEquals(Tile(3138, 3516), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb east under edgeville wall`() {
        val player = createPlayer(tile = Tile(3138, 3516))
        player.levels.set(Skill.Agility, 21)
        val monkeyBars = objects[Tile(3139, 3516), "edgeville_underwall_tunnel"]!!

        player.objectOption(monkeyBars, "Climb-into")
        tick(8)

        assertEquals(Tile(3144, 3514), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb under edgeville wall without level`() {
        val player = createPlayer(tile = Tile(3138, 3516))
        player.levels.set(Skill.Agility, 20)
        val monkeyBars = objects[Tile(3139, 3516), "edgeville_underwall_tunnel"]!!

        player.objectOption(monkeyBars, "Climb-into")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 21"))
    }
}