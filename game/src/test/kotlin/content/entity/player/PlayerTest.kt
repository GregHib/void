package content.entity.player

import WorldTest
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.Walk

internal class PlayerTest : WorldTest() {

    @Test
    fun `Walk to location`() {
        val start = emptyTile
        val player = createPlayer(start)
        val handler: InstructionHandlers = get()

        handler.walk(Walk(emptyTile.x, emptyTile.y + 10), player)
        tick(5)

        assertEquals(emptyTile.addY(5), player.tile)
    }

    @Test
    fun `Run to location`() {
        val start = emptyTile
        val player = createPlayer(start)
        val handler: InstructionHandlers = get()

        player.interfaceOption("energy_orb", "run_background", "Turn Run mode on")
        handler.walk(Walk(emptyTile.x, emptyTile.y + 10), player)
        tick(5)

        assertEquals(emptyTile.addY(10), player.tile)
    }

    @Test
    fun `Restore energy over time`() {
        val start = emptyTile
        val player = createPlayer(start)
        player.runEnergy = 0

        tick(5)

        assertTrue(player.runEnergy > 0)
    }

    @Test
    fun `Restore energy faster when resting`() {
        val start = emptyTile
        val player = createPlayer(start)
        player.runEnergy = 0
        tick(5)
        val energy = player.runEnergy
        player.runEnergy = 0

        player.interfaceOption("energy_orb", "run_background", "Rest")
        tick(5)

        assertTrue(player.runEnergy > energy)
    }

    @Test
    fun `Energy doesn't restore while running`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Agility, 99)
        val handler: InstructionHandlers = get()

        player.interfaceOption("energy_orb", "run_background", "Turn Run mode on")
        handler.walk(Walk(emptyTile.x, emptyTile.y + 20), player)
        tick(5)

        assertEquals(MAX_RUN_ENERGY - 67 * 5, player.runEnergy)
    }

    @Test
    fun `Energy restores after running stops`() {
        val player = createPlayer(emptyTile)
        val handler: InstructionHandlers = get()

        player.interfaceOption("energy_orb", "run_background", "Turn Run mode on")
        handler.walk(Walk(emptyTile.x, emptyTile.y + 4), player)
        tick(3)
        val energy = player.runEnergy

        tick(3)

        assertTrue(player.runEnergy > energy)
    }

    @Test
    fun `Weight increases energy drain`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("iron_ore", 28)
        val handler: InstructionHandlers = get()

        player.interfaceOption("energy_orb", "run_background", "Turn Run mode on")
        handler.walk(Walk(emptyTile.x, emptyTile.y + 20), player)
        tick(5)

        assertEquals(MAX_RUN_ENERGY - 132 * 5, player.runEnergy)
    }
}
