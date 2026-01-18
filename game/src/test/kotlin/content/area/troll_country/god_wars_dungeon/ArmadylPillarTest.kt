package content.area.troll_country.god_wars_dungeon

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArmadylPillarTest : WorldTest() {

    @Test
    fun `Grapple to armadyl area`() {
        val player = createPlayer(Tile(2872, 5279, 2))
        player.levels.set(Skill.Ranged, 70)
        player.equipment.set(EquipSlot.Weapon.index, "rune_crossbow")
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")

        val pillar = objects.find(Tile(2871, 5270, 2), "armadyl_pillar")

        player.objectOption(pillar, "Grapple")

        tick(7)

        assertEquals(Tile(2872, 5269, 2), player.tile)
    }

    @Test
    fun `Grapple from armadyl area`() {
        val player = createPlayer(Tile(2872, 5269, 2))
        player.levels.set(Skill.Ranged, 70)
        player.equipment.set(EquipSlot.Weapon.index, "rune_crossbow")
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")

        val pillar = objects.find(Tile(2871, 5270, 2), "armadyl_pillar")

        player.objectOption(pillar, "Grapple")

        tick(7)

        assertEquals(Tile(2872, 5279, 2), player.tile)
    }

    @Test
    fun `Can't grapple without crossbow equipped`() {
        val player = createPlayer(Tile(2872, 5279, 2))
        player.levels.set(Skill.Ranged, 70)
        player.inventory.add("rune_crossbow")
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")

        val pillar = objects.find(Tile(2871, 5270, 2), "armadyl_pillar")

        player.objectOption(pillar, "Grapple")

        tick(2)

        assertTrue(player.containsMessage("You need a crossbow"))
        assertEquals(Tile(2872, 5279, 2), player.tile)
    }

    @Test
    fun `Can't grapple without mithril grapple equipped`() {
        val player = createPlayer(Tile(2872, 5269, 2))
        player.levels.set(Skill.Ranged, 70)
        player.equipment.set(EquipSlot.Weapon.index, "rune_crossbow")
        player.inventory.add("mithril_grapple")

        val pillar = objects.find(Tile(2871, 5270, 2), "armadyl_pillar")

        player.objectOption(pillar, "Grapple")

        tick(2)

        assertTrue(player.containsMessage("You need a mithril grapple tipped bolt"))
        assertEquals(Tile(2872, 5269, 2), player.tile)
    }

    @Test
    fun `Can't grapple without 70 ranged`() {
        val player = createPlayer(Tile(2872, 5279, 2))
        player.levels.set(Skill.Ranged, 69)
        player.equipment.set(EquipSlot.Weapon.index, "rune_crossbow")
        player.equipment.set(EquipSlot.Ammo.index, "mithril_grapple")

        val pillar = objects.find(Tile(2871, 5270, 2), "armadyl_pillar")

        player.objectOption(pillar, "Grapple")

        tick(2)

        assertTrue(player.containsMessage("You need to have a Ranged level of 70"))
        assertEquals(Tile(2872, 5279, 2), player.tile)
    }
}
