package content.skill.magic.book.modern

import WorldTest
import interfaceOnFloorItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class TelekineticGrabTest : WorldTest() {

    @Test
    fun `Tele grab from afar`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")

        val item = createFloorItem("coins", player.tile.addX(4), 100)
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", item)
        tick(5)

        assertEquals(0, player.inventory.count("law_rune"))
        assertEquals(0, player.inventory.count("air_rune"))
        assertEquals(100, player.inventory.count("coins"))
        assertEquals(43.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Tele grab item under`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")

        val item = createFloorItem("bronze_sword", player.tile)
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", item)
        tick(5)

        assertEquals(1, player.inventory.count("bronze_sword"))
        assertEquals(43.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't tele grab without level`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 32)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")

        val item = createFloorItem("bones", player.tile.addY(1))
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", item)
        tick(5)

        assertEquals(1, player.inventory.count("law_rune"))
        assertEquals(1, player.inventory.count("air_rune"))
        assertEquals(0, player.inventory.count("bones"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't tele grab without line of sight`() {
        val player = createPlayer(Tile(3191, 9825))
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")

        val item = createFloorItem("ruby_ring", Tile(3196, 9822))
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", item)
        tick(6)

        assertEquals(0, player.inventory.count("ruby_ring"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
