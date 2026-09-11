package content.skill.ranged.ammo

import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CrystalBowTest : WorldTest() {

    @Test
    fun `Crystal bow doesn't drop itself as ammo`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))
        npc.huntMode = ""
        player.levels.set(Skill.Ranged, 70)
        player.equipment.set(EquipSlot.Weapon.index, "new_crystal_bow")

        player.npcOption(npc, "Attack")
        tick(10)

        // The new bow degrades into crystal_bow_full on its first shot but stays equipped
        assertEquals("crystal_bow_full", player.equipment[EquipSlot.Weapon.index].id)
        assertNull(FloorItems.firstOrNull(npc.tile, "new_crystal_bow"))
    }
}
