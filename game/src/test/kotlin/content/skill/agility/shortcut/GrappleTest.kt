package content.skill.agility.shortcut

import WorldTest
import containsMessage
import content.entity.player.equip.EquipTest
import messages
import objectOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GrappleTest : WorldTest() {

    @Test
    fun `Can't grapple to al kharid without mithril grapple`() {
        val player = createPlayer(tile = Tile(3246, 3179))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a mithril grapple"))
    }

    @Test
    fun `Can't grapple to al kharid without crossbow`() {
        val player = createPlayer(tile = Tile(3246, 3179))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a crossbow"))
    }

    @Test
    fun `Can't grapple to al kharid without levels`() {
        val player = createPlayer(tile = Tile(3246, 3179))
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertEquals("dialogue_message1", player.dialogue)
    }

    @Test
    fun `Can't grapple to al kharid too far away`() {
        val player = createPlayer(tile = Tile(3246, 3181))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("I can't do that from here"))
    }

    @Test
    fun `Can grapple to al kharid`() {
        val player = createPlayer(tile = Tile(3246, 3180))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick(24)

        assertEquals(Tile(3259, 3180), player.tile)
    }

    @Test
    fun `Can't grapple to lumbridge too far away`() {
        val player = createPlayer(tile = Tile(3259, 3178))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("I can't do that from here"))
    }

    @Test
    fun `Can grapple to lumbridge`() {
        val player = createPlayer(tile = Tile(3259, 3180))
        player.levels.set(Skill.Agility, 8)
        player.levels.set(Skill.Ranged, 38)
        player.levels.set(Skill.Strength, 17)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3252, 3179), "lumbridge_broken_raft"]!!
        player.objectOption(obj, "Grapple")

        tick(24)

        assertEquals(Tile(3246, 3179), player.tile)
    }
}