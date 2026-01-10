package content.entity.combat

import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

internal class CombatMovementTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(Random)
    }

    @Test
    fun `Player intercepts moving target`() {
        val player = createPlayer(emptyTile)
        player.running = false
        val npc = createNPC("goblin_light_grey_bald", emptyTile.addX(1))
        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, MAX_EXP)
        player.experience.set(Skill.Strength, MAX_EXP)
        player.experience.set(Skill.Defence, MAX_EXP)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        npc.walkTo(emptyTile.add(1, -5))
        tick()
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(7) // npc death

        assertEquals(emptyTile.add(1, -1), player.tile)
        assertEquals(emptyTile.add(1, -2), tile)
    }

    @Test
    fun `Npc retreats if hit by target outside aggression range`() {
        val npc = createNPC("guard_falador", Tile(3036, 3355))
        npc.tele(3031, 3350)
        val player = createPlayer(tile = Tile(3030, 3350))
        player.interactNpc(npc, "Attack")
        tick()
        assertTrue(npc.mode is Retreat)
    }

    @Test
    fun `Npc stops attacking if target leaves aggression range`() {
        val npc = createNPC("guard_falador", Tile(3036, 3355))
        npc.tele(3032, 3351)
        val player = createPlayer(tile = Tile(3032, 3350))
        npc.interactPlayer(player, "Attack")
        tick()
        assertTrue(npc.mode is CombatMovement)
        player.walkTo(Tile(3031, 3350))
        tick(2)
        assert(npc.mode !is CombatMovement)
    }

    @Test
    fun `Ranged npc doesn't stop on corners of aggression range`() {
        val npc = createNPC("guard_falador_2", Tile(3036, 3355))
        npc.tele(3032, 3351)
        val player = createPlayer(tile = Tile(3032, 3350))
        npc.interactPlayer(player, "Attack")
        tick()
        assertTrue(npc.mode is CombatMovement)
        player.walkTo(Tile(3031, 3350))
        tick(2)
        assertTrue(npc.mode is CombatMovement)
    }

    @Test
    fun `Ranged npc has steps queued after target has left aggression range`() {
        val npc = createNPC("guard_falador_2", Tile(3036, 3355))
        npc.tele(3042, 3357)
        val player = createPlayer(tile = Tile(3042, 3363))
        npc.interactPlayer(player, "Attack")
        tick(2)
        assertTrue(npc.mode is CombatMovement)
        assertEquals(Tile(3042, 3358), npc.tile)
        player.walkTo(Tile(3042, 3365))
        tick(4)
        assertEquals(Tile(3042, 3363), npc.steps.peek()?.id?.let { Tile(it) })
        assertTrue(npc.mode is EmptyMode)
        assertEquals(Tile(3042, 3359), npc.tile)
    }

    @Test
    fun `Npc can step outside of max range if target is within aggression range`() {
        val npc = createNPC("guard_falador", Tile(3036, 3355))
        npc.tele(3032, 3351)
        val player = createPlayer(tile = Tile(3034, 3350))
        npc.interactPlayer(player, "Attack")
        tick(2)
        assertTrue(npc.mode is CombatMovement)
        assertEquals(Tile(3033, 3350), npc.tile)
    }

    @Test
    fun `Npc doesn't retreat if target within aggression range`() {
        val npc = createNPC("guard_falador", Tile(3036, 3355))
        npc.tele(3032, 3352)
        val player = createPlayer(Tile(3032, 3351))
        npc.mode = CombatMovement(npc, player)
        tick()
        player.walkTo(Tile(3032, 3350))
        tick(2)

        assertEquals(Tile(3032, 3351), npc.tile)
        assertEquals(Tile(3032, 3350), player.tile)
        assertTrue(npc.mode is CombatMovement)
    }

    companion object {
        private const val MAX_EXP = 14000000.0
    }
}
