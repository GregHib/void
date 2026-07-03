package content.skill.hunter

import FakeRandom
import WorldTest
import itemOption
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class ButterflyTest : WorldTest() {
    @Test
    fun `Catch butterfly with net`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 15)
        player.inventory.add("butterfly_jar")
        player.equipment.set(EquipSlot.Weapon.index, "butterfly_net")
        val butterfly = createNPC("ruby_harvest", player.tile.addY(1))

        player.npcOption(butterfly, "Catch")
        tick(3)

        assertEquals(0, player.inventory.count("butterfly_jar"))
        assertEquals(1, player.inventory.count("ruby_harvest"))
        assertEquals(24.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Catch butterfly with bare-hands`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 80)
        player.inventory.add("butterfly_jar")
        val butterfly = createNPC("ruby_harvest", player.tile.addY(1))

        player.npcOption(butterfly, "Catch")
        tick(3)

        assertEquals(0, player.inventory.count("butterfly_jar"))
        assertEquals(1, player.inventory.count("ruby_harvest"))
        assertEquals(350.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Fail to catch butterfly`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 15)
        player.inventory.add("butterfly_jar")
        player.equipment.set(EquipSlot.Weapon.index, "butterfly_net")
        val butterfly = createNPC("ruby_harvest", player.tile.addY(1))

        player.npcOption(butterfly, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("butterfly_jar"))
        assertEquals(0, player.inventory.count("ruby_harvest"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't catch butterfly without hunter level`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 14)
        player.inventory.add("butterfly_jar")
        player.equipment.set(EquipSlot.Weapon.index, "butterfly_net")
        val butterfly = createNPC("ruby_harvest", player.tile.addY(1))

        player.npcOption(butterfly, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("butterfly_jar"))
        assertEquals(0, player.inventory.count("ruby_harvest"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't catch butterfly with bare hands without hunter level`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 79)
        player.inventory.add("butterfly_jar")
        val butterfly = createNPC("ruby_harvest", player.tile.addY(1))

        player.npcOption(butterfly, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("butterfly_jar"))
        assertEquals(0, player.inventory.count("ruby_harvest"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Release butterfly`() {
        val player = createPlayer()
        player.inventory.add("ruby_harvest")

        player.itemOption("Release", "ruby_harvest")

        assertEquals(5, player.levels.get(Skill.Attack))
        assertEquals(0, player.inventory.count("ruby_harvest"))
        assertEquals(1, player.inventory.count("butterfly_jar"))
    }

}