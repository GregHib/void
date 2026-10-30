package content.entity.combat.hit

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

class NPCMaxHitTest : WorldTest() {

    @Test
    fun `Melee styles use the npcs general melee max hit`() {
        val player = createPlayer(Tile(3222, 3218))
        val guard = createNPC("black_guard_keldagrim_market", Tile(3222, 3219))

        assertEquals(80, Damage.maximum(guard, player, "crush", Item.EMPTY, range = 0..20))
        assertEquals(80, Damage.maximum(guard, player, "stab", Item.EMPTY, range = 0..20))
    }

    @Test
    fun `Style specific max hits are used`() {
        val player = createPlayer(Tile(3222, 3218))
        val npc = createNPC("chaos_dwarf_hand_cannoneer_chaos_dwarf_battlefield", Tile(3222, 3219))

        assertEquals(110, Damage.maximum(npc, player, "crush", Item.EMPTY, range = 0..20))
        assertEquals(380, Damage.maximum(npc, player, "range", Item.EMPTY, range = 0..20))
    }

    @Test
    fun `Non-melee styles don't use the general melee max hit`() {
        val player = createPlayer(Tile(3222, 3218))
        val guard = createNPC("black_guard_keldagrim_market", Tile(3222, 3219))

        assertEquals(20, Damage.maximum(guard, player, "magic", Item.EMPTY, range = 0..20))
    }
}
