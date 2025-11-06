package content.area.morytania.slayer_tower

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals

class AberrantSpectreTest : WorldTest() {

    @Test
    fun `Aberrant spectre drains stats without nose peg`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 60)

        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 50)
        player.levels.set(Skill.Defence, 50)
        player.levels.set(Skill.Ranged, 50)
        player.levels.set(Skill.Magic, 50)
        player.levels.set(Skill.Prayer, 50)
        player.levels.set(Skill.Agility, 50)
        val spectre = npcs.add("aberrant_spectre", emptyTile.addY(1))
        tick()

        player.npcOption(spectre, "Attack")

        tick(2)

        assertEquals(10, player.levels.get(Skill.Attack))
        assertEquals(10, player.levels.get(Skill.Strength))
        assertEquals(20, player.levels.get(Skill.Defence))
        assertEquals(10, player.levels.get(Skill.Ranged))
        assertEquals(10, player.levels.get(Skill.Magic))
        assertEquals(25, player.levels.get(Skill.Prayer))
        assertEquals(20, player.levels.get(Skill.Agility))
    }

    @Test
    fun `Aberrant spectre doesn't drain stats with nose peg`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 60)

        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 50)
        player.levels.set(Skill.Defence, 50)
        player.levels.set(Skill.Ranged, 50)
        player.levels.set(Skill.Magic, 50)
        player.levels.set(Skill.Prayer, 50)
        player.levels.set(Skill.Agility, 50)
        player.equipment.set(EquipSlot.Hat.index, "nose_peg")
        val spectre = npcs.add("aberrant_spectre", emptyTile.addY(1))
        tick()

        player.npcOption(spectre, "Attack")

        tick(3)

        assertEquals(50, player.levels.get(Skill.Attack))
        assertEquals(50, player.levels.get(Skill.Strength))
        assertEquals(50, player.levels.get(Skill.Defence))
        assertEquals(50, player.levels.get(Skill.Ranged))
        assertEquals(50, player.levels.get(Skill.Magic))
        assertEquals(50, player.levels.get(Skill.Prayer))
        assertEquals(50, player.levels.get(Skill.Agility))
    }
}
