package content.skill.melee.armour

import WorldTest
import content.entity.combat.hit.directHit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.charge
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertTrue

internal class RingOfRecoilTest : WorldTest() {

    @Test
    fun `Ring of recoil deflects damage`() {
        val player = createPlayer()
        val npc = createNPC("rat")

        player.equipment.set(EquipSlot.Ring.index, "ring_of_recoil")
        assertTrue(player.equipment.charge(player, EquipSlot.Ring.index, 400))

        assertEquals(400, player.equipment.charges(player, EquipSlot.Ring.index))

        player.directHit(npc, 11)

        assertEquals(389, player.equipment.charges(player, EquipSlot.Ring.index))
        assertEquals(89, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Ring of recoil charges are shared`() {
        val player = createPlayer()
        val npc = createNPC("rat")

        player.equipment.set(EquipSlot.Ring.index, "ring_of_recoil")
        player.inventory.set(0, "ring_of_recoil")
        assertTrue(player.equipment.charge(player, EquipSlot.Ring.index, 400))

        assertEquals(400, player.equipment.charges(player, EquipSlot.Ring.index))
        assertEquals(400, player.inventory.charges(player, 0))

        player.directHit(npc, 11)

        assertEquals(389, player.equipment.charges(player, EquipSlot.Ring.index))
        assertEquals(389, player.inventory.charges(player, 0))
    }
}
