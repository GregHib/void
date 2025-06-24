package content.area.morytania.slayer_tower

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals

class BansheeTest : WorldTest() {

    @Test
    fun `Banshee drains stats without earmuffs`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 15)

        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 50)
        player.levels.set(Skill.Defence, 50)
        player.levels.set(Skill.Ranged, 50)
        player.levels.set(Skill.Magic, 50)
        player.levels.set(Skill.Prayer, 50)
        player.levels.set(Skill.Agility, 50)
        val banshee = npcs.add("banshee", emptyTile.addY(1))
        tick()

        player.npcOption(banshee, "Attack")

        tick(3)

        assertEquals(40, player.levels.get(Skill.Attack))
        assertEquals(40, player.levels.get(Skill.Strength))
        assertEquals(40, player.levels.get(Skill.Defence))
        assertEquals(40, player.levels.get(Skill.Ranged))
        assertEquals(40, player.levels.get(Skill.Magic))
        assertEquals(45, player.levels.get(Skill.Prayer))
        assertEquals(45, player.levels.get(Skill.Agility))
    }

    @Test
    fun `Banshee doesn't drain stats with earmuffs`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 15)

        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 50)
        player.levels.set(Skill.Defence, 50)
        player.levels.set(Skill.Ranged, 50)
        player.levels.set(Skill.Magic, 50)
        player.levels.set(Skill.Prayer, 50)
        player.levels.set(Skill.Agility, 50)
        player.equipment.set(EquipSlot.Hat.index, "earmuffs")
        val banshee = npcs.add("banshee", emptyTile.addY(1))
        tick()

        player.npcOption(banshee, "Attack")

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
