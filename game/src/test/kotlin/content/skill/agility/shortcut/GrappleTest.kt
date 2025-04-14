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

    @Test
    fun `Grapple falador wall south`() {
        val player = createPlayer(tile = Tile(3005, 3393))
        player.levels.set(Skill.Agility, 11)
        player.levels.set(Skill.Ranged, 19)
        player.levels.set(Skill.Strength, 37)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3005, 3393), "falador_wall_south"]!!
        player.objectOption(obj, "Grapple")

        tick(13)

        assertEquals(Tile(3005, 3394, 1), player.tile)
    }

    @Test
    fun `Grapple falador wall north`() {
        val player = createPlayer(tile = Tile(3006, 3395))
        player.levels.set(Skill.Agility, 11)
        player.levels.set(Skill.Ranged, 19)
        player.levels.set(Skill.Strength, 37)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(3006, 3395), "falador_wall_north"]!!
        player.objectOption(obj, "Grapple")

        tick(13)

        assertEquals(Tile(3006, 3394, 1), player.tile)
    }

    @Test
    fun `Can't jump down falador wall south`() {
        val player = createPlayer(tile = Tile(3005, 3394, 1))
        val obj = objects[Tile(3005, 3393, 1), "falador_wall_jump_south"]!!
        player.objectOption(obj, "Jump")

        tick(2)

        assertTrue(player.containsMessage("You need an agility level of at least 4"))
    }

    @Test
    fun `Jump down falador wall south`() {
        val player = createPlayer(tile = Tile(3005, 3394, 1))
        player.levels.set(Skill.Agility, 4)
        val obj = objects[Tile(3005, 3393, 1), "falador_wall_jump_south"]!!
        player.objectOption(obj, "Jump")

        tick(2)

        assertEquals(Tile(3005, 3393), player.tile)
    }

    @Test
    fun `Jump down falador wall north`() {
        val player = createPlayer(tile = Tile(3006, 3394, 1))
        player.levels.set(Skill.Agility, 4)
        val obj = objects[Tile(3006, 3395, 1), "falador_wall_jump_north"]!!
        player.objectOption(obj, "Jump")

        tick(2)

        assertEquals(Tile(3006, 3395), player.tile)
    }

    @Test
    fun `Can't jump down falador wall north`() {
        val player = createPlayer(tile = Tile(3006, 3394, 1))
        val obj = objects[Tile(3006, 3395, 1), "falador_wall_jump_north"]!!
        player.objectOption(obj, "Jump")

        tick(2)

        assertTrue(player.containsMessage("You need an agility level of at least 4"))
    }

    @Test
    fun `Can grapple from water obelisk to catherby`() {
        val player = createPlayer(tile = Tile(2841, 3425))
        player.levels.set(Skill.Agility, 36)
        player.levels.set(Skill.Ranged, 39)
        player.levels.set(Skill.Strength, 22)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2841, 3434), "catherby_crossbow_tree"]!!
        player.objectOption(obj, "Grapple")

        tick(12)

        assertEquals(Tile(2841, 3432), player.tile)
    }

    @Test
    fun `Can't grapple to catherby wrong side of island`() {
        val player = createPlayer(tile = Tile(2843, 3432))
        player.levels.set(Skill.Agility, 36)
        player.levels.set(Skill.Ranged, 39)
        player.levels.set(Skill.Strength, 22)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2841, 3434), "catherby_crossbow_tree"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("I can't do that from here"))
    }

    @Test
    fun `Can't grapple to catherby without mithril grapple`() {
        val player = createPlayer(tile = Tile(2841, 3425))
        player.levels.set(Skill.Agility, 36)
        player.levels.set(Skill.Ranged, 39)
        player.levels.set(Skill.Strength, 22)
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2841, 3434), "catherby_crossbow_tree"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a mithril grapple"))
    }

    @Test
    fun `Can't grapple to catherby without crossbow`() {
        val player = createPlayer(tile = Tile(2841, 3425))
        player.levels.set(Skill.Agility, 36)
        player.levels.set(Skill.Ranged, 39)
        player.levels.set(Skill.Strength, 22)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        val obj = objects[Tile(2841, 3434), "catherby_crossbow_tree"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a crossbow"))
    }

    @Test
    fun `Can't grapple to catherby without levels`() {
        val player = createPlayer(tile = Tile(2841, 3425))
        player.levels.set(Skill.Agility, 35)
        player.levels.set(Skill.Ranged, 39)
        player.levels.set(Skill.Strength, 22)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2841, 3434), "catherby_crossbow_tree"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertEquals("dialogue_message2", player.dialogue)
    }

    @Test
    fun `Can grapple to white wolf mountain`() {
        val player = createPlayer(tile = Tile(2866, 3429))
        player.levels.set(Skill.Agility, 32)
        player.levels.set(Skill.Ranged, 35)
        player.levels.set(Skill.Strength, 35)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2869, 3429), "catherby_rocks"]!!
        player.objectOption(obj, "Grapple")

        tick(12)

        assertEquals(Tile(2869, 3430), player.tile)
    }

    @Test
    fun `Can't grapple from wrong side of white wolf mountain`() {
        val player = createPlayer(tile = Tile(2869, 3428))
        player.levels.set(Skill.Agility, 32)
        player.levels.set(Skill.Ranged, 35)
        player.levels.set(Skill.Strength, 35)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2869, 3429), "catherby_rocks"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("I can't do that from here"))
    }

    @Test
    fun `Can't grapple to white wolf mountain without mithril grapple`() {
        val player = createPlayer(tile = Tile(2866, 3429))
        player.levels.set(Skill.Agility, 32)
        player.levels.set(Skill.Ranged, 35)
        player.levels.set(Skill.Strength, 35)
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2869, 3429), "catherby_rocks"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a mithril grapple"))
    }

    @Test
    fun `Can't grapple to white wolf mountain without crossbow`() {
        val player = createPlayer(tile = Tile(2866, 3429))
        player.levels.set(Skill.Agility, 32)
        player.levels.set(Skill.Ranged, 35)
        player.levels.set(Skill.Strength, 35)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        val obj = objects[Tile(2869, 3429), "catherby_rocks"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertTrue(player.containsMessage("You need a crossbow"))
    }

    @Test
    fun `Can't grapple to white wolf mountain without levels`() {
        val player = createPlayer(tile = Tile(2866, 3429))
        player.levels.set(Skill.Agility, 31)
        player.levels.set(Skill.Ranged, 35)
        player.levels.set(Skill.Strength, 35)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_crossbow")
        val obj = objects[Tile(2869, 3429), "catherby_rocks"]!!
        player.objectOption(obj, "Grapple")

        tick()

        assertEquals("dialogue_message2", player.dialogue)
    }

}