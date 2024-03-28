package world.gregs.voidps.world.interact.entity.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.script.WorldTest

internal class RingOfRecoilTest : WorldTest() {

    @Test
    fun `Ring of recoil deflects damage`() {
        val player = createPlayer("player")
        val npc = createNPC("rat")

        player.equipment.set(EquipSlot.Ring.index, "ring_of_recoil")

        assertEquals(400, Degrade.charges(player, "worn_equipment", EquipSlot.Ring.index))

        player.directHit(npc, 11)

        assertEquals(389, Degrade.charges(player, "worn_equipment", EquipSlot.Ring.index))
        assertEquals(89, player.levels.get(Skill.Constitution))
    }

}