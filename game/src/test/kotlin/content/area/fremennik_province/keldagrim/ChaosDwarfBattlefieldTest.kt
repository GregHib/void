package content.area.fremennik_province.keldagrim

import WorldTest
import content.area.wilderness.inMultiCombat
import npcOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class ChaosDwarfBattlefieldTest : WorldTest() {

    @BeforeEach
    fun setup() {
        NPCs.clear()
    }

    private fun NPC.fighting(target: Character) = mode.let { it is CombatMovement && it.target == target }

    @Test
    fun `Black Guard and chaos dwarves fight each other`() {
        val guard = createNPC("black_guard_chaos_dwarf_battlefield", Tile(1510, 4704))
        val dwarf = createNPC("chaos_dwarf_chaos_dwarf_battlefield", Tile(1506, 4704))

        tick(10)

        assertTrue(guard.fighting(dwarf) || dwarf.fighting(guard))
    }

    @Test
    fun `Attacking a Black Guard turns the whole Black Guard side on the player`() {
        val player = createPlayer(Tile(1515, 4704))
        val guard = createNPC("black_guard_berserker_chaos_dwarf_battlefield", Tile(1516, 4704))
        val crossbow = createNPC("black_guard_crossbowdwarf_chaos_dwarf_battlefield", Tile(1515, 4710))
        val dwarf = createNPC("chaos_dwarf_chaos_dwarf_battlefield_2", Tile(1520, 4710))

        player.npcOption(guard, "Attack")
        tick(4)

        assertTrue(crossbow.fighting(player))
        assertFalse(dwarf.fighting(player))
    }

    @Test
    fun `Attacking a chaos dwarf turns the whole chaos side on the player`() {
        val player = createPlayer(Tile(1515, 4704))
        val dwarf = createNPC("chaos_dwarf_chaos_dwarf_battlefield", Tile(1516, 4704))
        val cannoneer = createNPC("chaos_dwarf_hand_cannoneer_chaos_dwarf_battlefield", Tile(1515, 4710))

        player.npcOption(dwarf, "Attack")
        tick(4)

        assertTrue(cannoneer.fighting(player))
    }

    @Test
    fun `Attacking a dwogre leaves the rest of the chaos side alone`() {
        val player = createPlayer(Tile(1515, 4704))
        val dwogre = createNPC("chaos_dwogre_chaos_dwarf_battlefield", Tile(1516, 4704))
        val dwarf = createNPC("chaos_dwarf_chaos_dwarf_battlefield", Tile(1515, 4710))

        player.npcOption(dwogre, "Attack")
        tick(4)

        assertFalse(dwarf.fighting(player))
    }

    @Test
    fun `Battlefield is multi-combat`() {
        val player = createPlayer(Tile(1521, 4710))
        player.walkTo(Tile(1515, 4710))
        tick(8)

        assertTrue(player.inMultiCombat)
    }
}
