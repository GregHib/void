package content.skill.melee.armour

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
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
        val player = createPlayer(emptyTile)
        val npc = createNPC("greater_demon", emptyTile.addY(4))

        player.experience.set(Skill.Defence, Level.experience(99))
        player.levels.set(Skill.Defence, 99)
        player.experience.set(Skill.Strength, Level.experience(99))
        player.levels.set(Skill.Strength, 99)
        player.experience.set(Skill.Attack, Level.experience(99))
        player.levels.set(Skill.Attack, 99)

        player.equipment.set(EquipSlot.Weapon.index, "chaotic_rapier", 30000)
        player.equipment.set(EquipSlot.Chest.index, "dharoks_platebody")
        player.equipment.set(EquipSlot.Legs.index, "statiuss_platelegs")
        player["binding_necklace_charges"] = 16
        player.equipment.set(EquipSlot.Amulet.index, "binding_necklace")
        player.equipment.set(EquipSlot.Ring.index, "ring_of_duelling_8")
        player.equipment.set(EquipSlot.Hands.index, "combat_bracelet_4")

        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }

        assertEquals("chaotic_rapier", player.equipped(EquipSlot.Weapon).id)
        assertTrue(player.equipment.charges(player, EquipSlot.Weapon.index) < 30000)
        assertEquals("dharoks_platebody_100", player.equipped(EquipSlot.Chest).id)
        assertEquals(22500, player.equipment.charges(player, EquipSlot.Chest.index))
        assertEquals("statiuss_platelegs_degraded", player.equipped(EquipSlot.Legs).id)
        assertEquals(6000, player.equipment.charges(player, EquipSlot.Legs.index))
        assertEquals("binding_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals(16, player.equipment.charges(player, EquipSlot.Amulet.index))
        assertEquals("ring_of_duelling_8", player.equipped(EquipSlot.Ring).id)
        assertEquals(1, player.equipment.charges(player, EquipSlot.Ring.index))
        assertEquals("combat_bracelet_4", player.equipped(EquipSlot.Hands).id)
        assertEquals(1, player.equipment.charges(player, EquipSlot.Hands.index))
    }

    @Test
    fun `Equipped durability waits between automatic drains`() {
        val player = createPlayer()
        val slot = EquipSlot.Hat.index
        player.equipment.set(slot, "corrupt_dragon_helm_degraded", 1500)

        tick()
        assertEquals(1499, player.equipment.charges(player, slot))
        tick(99)
        assertEquals(1499, player.equipment.charges(player, slot))
        tick()
        assertEquals(1498, player.equipment.charges(player, slot))
    }

    @Test
    fun `Equipment is destroyed when charges run out`() {
        val player = createPlayer(emptyTile)
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

        player.npcOption(npc, "Attack")
        tick(2)

        assertEquals("chaotic_rapier_broken", player.equipped(EquipSlot.Weapon).id)
        assertEquals(0, player.equipment.charges(player, EquipSlot.Weapon.index))
        assertEquals("dharoks_platebody_75", player.equipped(EquipSlot.Chest).id)
        assertNotEquals(0, player.equipment.charges(player, EquipSlot.Chest.index))
        assertTrue(player.equipped(EquipSlot.Legs).isEmpty())
        assertEquals(0, player.equipment.charges(player, EquipSlot.Legs.index))
    }
}
