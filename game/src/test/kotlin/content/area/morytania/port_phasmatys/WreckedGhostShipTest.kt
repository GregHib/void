package content.area.morytania.port_phasmatys

import FakeRandom
import WorldTest
import containsMessage
import content.entity.player.effect.energy.runEnergy
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import java.sql.SQLOutput
import kotlin.test.assertTrue

class WreckedGhostShipTest : WorldTest() {

    @Test
    fun `Jump to the end`() {
        val player = createPlayer(Tile(3605, 3550))
        player.stop("energy_restore")
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 10_000

        val rock1 = Tile(3602, 3550)
        var rocks = objects[rock1, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(6)
        assertEquals(rock1, player.tile)

        val rock2 = Tile(3597, 3552)
        rocks = objects[rock2, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(rock2, player.tile)

        val rock3 = Tile(3595, 3556)
        rocks = objects[rock3, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(12)
        assertEquals(rock3, player.tile)

        val rock4 = Tile(3597, 3561)
        rocks = objects[rock4, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(rock4, player.tile)

        val shore = Tile(3601, 3564)
        rocks = objects[shore, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(50.0, player.experience.get(Skill.Agility))
        assertTrue(player.runEnergy < 10_000)
        assertEquals(shore, player.tile)
    }

    @Test
    fun `Jump to the ship`() {
        val player = createPlayer(Tile(3602, 3564))
        player.stop("energy_restore")
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 10_000

        val rock1 = Tile(3599, 3564)
        var rocks = objects[rock1, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(20)
        assertEquals(rock1, player.tile)

        val rock2 = Tile(3597, 3559)
        rocks = objects[rock2, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(rock2, player.tile)

        val rock3 = Tile(3595, 3554)
        rocks = objects[rock3, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(12)
        assertEquals(rock3, player.tile)

        val rock4 = Tile(3599, 3552)
        rocks = objects[rock4, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(rock4, player.tile)

        val shore = Tile(3604, 3550)
        rocks = objects[shore, "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(50.0, player.experience.get(Skill.Agility))
        assertTrue(player.runEnergy < 10_000)
        assertEquals(shore, player.tile)
    }

    @Test
    fun `Jump to the end clicking nearest`() {
        val player = createPlayer(Tile(3604, 3550))
        player.stop("energy_restore")
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 10_000

        var rocks = objects[Tile(3604, 3550), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(5)
        assertEquals(Tile(3602, 3550), player.tile)

        rocks = objects[Tile(3599, 3552), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(Tile(3597, 3552), player.tile)

        rocks = objects[Tile(3595, 3554), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(10)
        assertEquals(Tile(3595, 3556), player.tile)

        rocks = objects[Tile(3597, 3559), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(Tile(3597, 3561), player.tile)

        rocks = objects[Tile(3599, 3564), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(50.0, player.experience.get(Skill.Agility))
        assertTrue(player.runEnergy < 10_000)
        assertEquals(Tile(3601, 3564), player.tile)
    }

    @Test
    fun `Jump to the ship clicking nearest`() {
        val player = createPlayer(Tile(3601, 3564))
        player.stop("energy_restore")
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 10_000

        val shore = objects[Tile(3601, 3564), "wrecked_ghost_ship_rock"]!!
        player.objectOption(shore, "Jump-to")
        tick(5)
        assertEquals(Tile(3599, 3564), player.tile)

        var rocks = objects[Tile(3597, 3561), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(Tile(3597, 3559), player.tile)

        rocks = objects[Tile(3595, 3556), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(Tile(3595, 3554), player.tile)

        rocks = objects[Tile(3597, 3552), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(8)
        assertEquals(Tile(3599, 3552), player.tile)

        rocks = objects[Tile(3602, 3550), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(9)
        assertEquals(50.0, player.experience.get(Skill.Agility))
        assertTrue(player.runEnergy < 10_000)
        assertEquals(Tile(3604, 3550), player.tile)
    }

    @Test
    fun `Fail jump`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(Tile(3599, 3552))
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 10_000

        val rocks = objects[Tile(3599, 3552), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(6)
        assertEquals(Tile(3597, 3552), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.runEnergy < 10_000)
        assertEquals(90, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Can't jump rocks without agility level`() {
        val player = createPlayer(Tile(3599, 3552))
        player.levels.set(Skill.Agility, 24)
        player.runEnergy = 10_000

        val rocks = objects[Tile(3599, 3552), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(2)
        assertTrue(player.containsMessage("You need level 25 agility"))
        assertEquals(Tile(3599, 3552), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(10_000, player.runEnergy)
    }

    @Test
    fun `Can't jump rocks without energy`() {
        val player = createPlayer(Tile(3599, 3552))
        player.levels.set(Skill.Agility, 25)
        player.runEnergy = 0

        val rocks = objects[Tile(3599, 3552), "wrecked_ghost_ship_rock"]!!
        player.objectOption(rocks, "Jump-to")
        tick(2)
        assertTrue(player.containsMessage("You don't have enough energy"))
        assertEquals(Tile(3599, 3552), player.tile)
    }

    @Test
    fun `Cross gangplank off of ship`() {
        val player = createPlayer(Tile(3605, 3545, 1))

        val rocks = objects[Tile(3605, 3546, 1), "wrecked_ghost_ship_gangplank"]!!
        player.objectOption(rocks, "Cross")
        tick(3)
        assertEquals(Tile(3605, 3548), player.tile)
    }

    @Test
    fun `Cross gangplank onto ship`() {
        val player = createPlayer(Tile(3605, 3548))

        val rocks = objects[Tile(3605, 3547), "wrecked_ghost_ship_gangplank_end"]!!
        player.objectOption(rocks, "Cross")
        tick(3)
        assertEquals(Tile(3605, 3545, 1), player.tile)
    }
}