package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.npcOption
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CombatDegradeTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 1111
        })
    }

    @Test
    fun `Equipped items degrade during combat`() {
        val player = createPlayer("player", emptyTile)
        val npc = createNPC("greater_demon", emptyTile.addY(4))

        player.experience.set(Skill.Defence, Level.experience(99))
        player.levels.set(Skill.Defence, 99)
        player.experience.set(Skill.Strength, Level.experience(99))
        player.levels.set(Skill.Strength, 99)
        player.experience.set(Skill.Attack, Level.experience(99))
        player.levels.set(Skill.Attack, 99)

        player.equipment.set(EquipSlot.Weapon.index, "chaotic_rapier")
        player.equipment.set(EquipSlot.Chest.index, "dharoks_platebody")
        player.equipment.set(EquipSlot.Legs.index, "statiuss_platelegs")
        player.equipment.set(EquipSlot.Amulet.index, "binding_necklace")
        player.equipment.set(EquipSlot.Ring.index, "ring_of_duelling_8")
        player.equipment.set(EquipSlot.Hands.index, "combat_bracelet_4")

        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }

        assertEquals("chaotic_rapier", player.equipped(EquipSlot.Weapon).id)
        assertTrue(Degrade.charges(player, player.equipment, EquipSlot.Weapon.index) < 30000)
        assertEquals("dharoks_platebody_100", player.equipped(EquipSlot.Chest).id)
        assertTrue(Degrade.charges(player, player.equipment, EquipSlot.Chest.index) < 22500)
        assertEquals("statiuss_platelegs_degraded", player.equipped(EquipSlot.Legs).id)
        assertTrue(Degrade.charges(player, player.equipment, EquipSlot.Legs.index) < 6000)
        assertEquals("binding_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals(16, Degrade.charges(player, player.equipment, EquipSlot.Amulet.index))
        assertEquals("ring_of_duelling_8", player.equipped(EquipSlot.Ring).id)
        assertEquals(1, Degrade.charges(player, player.equipment, EquipSlot.Ring.index))
        assertEquals("combat_bracelet_4", player.equipped(EquipSlot.Hands).id)
        assertEquals(1, Degrade.charges(player, player.equipment, EquipSlot.Hands.index))
    }

    @Test
    fun `Equipment is destroyed when charges run out`() {
        val player = createPlayer("player", emptyTile)
        val npc = createNPC("rat", emptyTile.addY(4))

        player.experience.set(Skill.Defence, Level.experience(99))
        player.levels.set(Skill.Defence, 99)
        player.experience.set(Skill.Strength, Level.experience(99))
        player.levels.set(Skill.Strength, 99)
        player.experience.set(Skill.Attack, Level.experience(99))
        player.levels.set(Skill.Attack, 99)

        player.equipment.set(EquipSlot.Weapon.index, "chaotic_rapier")
        player.equipment.set(EquipSlot.Chest.index, "dharoks_platebody_100")
        player.equipment.set(EquipSlot.Legs.index, "statiuss_platelegs_degraded")

        player[Degrade.variable(player.equipment.id, EquipSlot.Weapon.index)] = 1
        player[Degrade.variable(player.equipment.id, EquipSlot.Chest.index)] = 1
        player[Degrade.variable(player.equipment.id, EquipSlot.Legs.index)] = 1

        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }

        assertEquals("chaotic_rapier_broken", player.equipped(EquipSlot.Weapon).id)
        assertEquals(0, Degrade.charges(player, player.equipment, EquipSlot.Weapon.index))
        assertEquals("dharoks_platebody_75", player.equipped(EquipSlot.Chest).id)
        assertNotEquals(0, Degrade.charges(player, player.equipment, EquipSlot.Chest.index))
        assertTrue(player.equipped(EquipSlot.Legs).isEmpty())
        assertEquals(0, Degrade.charges(player, player.equipment, EquipSlot.Legs.index))
    }
}