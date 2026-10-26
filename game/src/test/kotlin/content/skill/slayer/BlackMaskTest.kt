package content.skill.slayer

import FakeRandom
import WorldTest
import itemOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class BlackMaskTest : WorldTest() {

    @Test
    fun `Charged black mask lowers defence when starting combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int): Int = from
        })
        val player = createPlayer(Tile(3200, 3200))
        player.levels.set(Skill.Attack, 1)
        player.equipment.set(EquipSlot.Hat.index, "black_mask_10")
        val npc = createNPC("hill_giant", player.tile.addY(1))
        val defence = npc.levels.get(Skill.Defence)

        player.npcOption(npc, "Attack")
        tick(2)

        assertEquals(defence - 3, npc.levels.get(Skill.Defence))
        assertEquals("black_mask_9", player.equipment[EquipSlot.Hat.index].id)

        tick(10)

        assertEquals("black_mask_9", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Uncharged black mask doesn't lower defence`() {
        val player = createPlayer(Tile(3200, 3200))
        player.equipment.set(EquipSlot.Hat.index, "black_mask")
        val npc = createNPC("hill_giant", player.tile.addY(1))
        val defence = npc.levels.get(Skill.Defence)

        player.npcOption(npc, "Attack")
        tick(2)

        assertEquals(defence, npc.levels.get(Skill.Defence))
    }

    @Test
    fun `Remove the charges from a black mask`() {
        val player = createPlayer(Tile(3200, 3200))
        player.inventory.add("black_mask_7")

        player.itemOption("Remove-charge", "black_mask_7")

        assertEquals(1, player.inventory.count("black_mask"))
        assertEquals(0, player.inventory.count("black_mask_7"))
    }
}
