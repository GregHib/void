package content.entity.player.equip

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class IcyBreathTest : WorldTest() {

    private val shield = EquipSlot.Shield.index

    @Test
    fun `A fire resistant shield caps a wyvern's icy breath`() {
        val player = createPlayer(emptyTile)
        val wyvern = createNPC("skeletal_wyvern", emptyTile.addY(4))
        player.equipment.set(shield, "elemental_shield")

        val damage = Equipment.shieldDamageReductionModifiers(wyvern, player, "icy_breath", 500)

        assertEquals(100, damage)
    }

    @Test
    fun `A charged dragonfire shield caps a wyvern's icy breath`() {
        val player = createPlayer(emptyTile)
        val wyvern = createNPC("skeletal_wyvern", emptyTile.addY(4))
        player.equipment.set(shield, "dragonfire_shield_charged", 20)

        val damage = Equipment.shieldDamageReductionModifiers(wyvern, player, "icy_breath", 500)

        assertEquals(100, damage)
    }

    @Test
    fun `A capped hit is never raised to the cap`() {
        val player = createPlayer(emptyTile)
        val wyvern = createNPC("skeletal_wyvern", emptyTile.addY(4))
        player.equipment.set(shield, "mind_shield")

        val damage = Equipment.shieldDamageReductionModifiers(wyvern, player, "icy_breath", 40)

        assertEquals(40, damage)
    }

    @Test
    fun `Icy breath is unreduced without a fire resistant shield`() {
        val player = createPlayer(emptyTile)
        val wyvern = createNPC("skeletal_wyvern", emptyTile.addY(4))
        player.equipment.set(shield, "rune_kiteshield")

        val damage = Equipment.shieldDamageReductionModifiers(wyvern, player, "icy_breath", 500)

        assertEquals(500, damage)
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
