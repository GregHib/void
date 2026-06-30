package content.entity.combat

import WorldTest
import content.skill.summoning.assistFamiliar
import content.skill.summoning.commandFamiliarAttack
import content.skill.summoning.follower
import npcOption
import world.gregs.voidps.engine.client.variable.start
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import org.rsmod.game.pathfinder.flag.CollisionFlag
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
        assertTrue(npc.mode !is CombatMovement)
        assertEquals(Tile(3032, 3351), npc.tile)
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

    @Test
    fun `Npc spawned under player steps out to attack`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("guard_falador", emptyTile)
        npc.interactPlayer(player, "Attack")
        tick(2)
        assertTrue(npc.tile != emptyTile)
        assertTrue(npc.mode is CombatMovement)
    }

    @Test
    fun `Familiar follower leash anchors to owner not spawn tile`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3032, 3352))
        familiar["owner_index"] = owner.index
        // A spawn far from the fight would de-aggro a normal NPC; the familiar anchors to its owner instead.
        familiar["spawn_tile"] = Tile(3100, 3100)
        val target = createNPC("guard_falador", Tile(3032, 3351))
        familiar.mode = CombatMovement(familiar, target)
        // Owner stays beside the fight, so the owner-anchored leash keeps the familiar engaged
        // even though its spawn tile is far away (a spawn-anchored NPC would drop out here).
        tick(2)
        assertTrue(familiar.mode is CombatMovement)
    }

    @Test
    fun `Owned familiar does not block-move through players`() {
        val familiar = createNPC("spirit_wolf_familiar", Tile(3032, 3352))
        familiar["owner_index"] = 5
        // Phases through players (no BLOCK_PLAYERS) but still collides with npcs (BLOCK_NPCS).
        assertEquals(0, familiar.blockMove and CollisionFlag.BLOCK_PLAYERS)
        assertTrue(familiar.blockMove and CollisionFlag.BLOCK_NPCS != 0)
    }

    @Test
    fun `A regular npc still block-moves through players and npcs`() {
        val npc = createNPC("guard_falador", Tile(3032, 3352))
        assertTrue(npc.blockMove and CollisionFlag.BLOCK_PLAYERS != 0)
        assertTrue(npc.blockMove and CollisionFlag.BLOCK_NPCS != 0)
    }

    @Test
    fun `Familiar can be ordered to attack an npc in single-way`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        // Single-way zone, unengaged NPC: the familiar can be ordered to fight it solo.
        val target = createNPC("guard_falador", Tile(3032, 3351))

        owner.commandFamiliarAttack(target)
        tick(8)

        assertTrue(familiar.mode is CombatMovement)
        assertEquals(target, (familiar.mode as CombatMovement).target)
    }

    @Test
    fun `Familiar returns to following its owner when it cannot reach its target`() {
        val owner = createPlayer(emptyTile.add(0, 10))
        val familiar = createNPC("spirit_wolf_familiar", emptyTile)
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        val target = createNPC("guard_falador", emptyTile.add(10, 0))
        familiar.mode = CombatMovement(familiar, target)
        // Familiar is stuck (can't move), so it can never close the distance to the far target;
        // after the grace period it should give up and fall back to following its owner.
        familiar.start("movement_delay", 100)

        tick(8)

        assertTrue(familiar.mode is Follow, "mode=${familiar.mode}")
    }

    @Test
    fun `Familiar chases its target when it retreats out of range`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        val target = createNPC("guard_falador", Tile(3034, 3352))
        familiar.mode = CombatMovement(familiar, target)
        tick(2)

        // The target retreats well out of attack range.
        target.tele(Tile(3044, 3352))
        val beforeX = familiar.tile.x
        tick(4)

        assertTrue(familiar.mode is CombatMovement)
        assertTrue(familiar.tile.x > beforeX)
    }

    @Test
    fun `Familiar keeps pathing to its target when an obstruction blocks then clears`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3032, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        val target = createNPC("guard_falador", Tile(3046, 3352))
        // A wall of blockers directly between the familiar and its target.
        val blockers = listOf(
            createNPC("guard_falador", Tile(3033, 3351)),
            createNPC("guard_falador", Tile(3033, 3352)),
            createNPC("guard_falador", Tile(3033, 3353)),
        )

        owner.commandFamiliarAttack(target)
        tick(3)
        // It's still trying to reach the target (CombatMovement persists) rather than giving up.
        assertTrue(familiar.mode is CombatMovement)

        // Clear the wall; the familiar must resume closing the distance, not stay frozen.
        blockers.forEach { it.tele(Tile(3033, 3360)) }
        val beforeX = familiar.tile.x
        tick(4)

        assertTrue(familiar.mode is CombatMovement)
        assertTrue(familiar.tile.x > beforeX)
    }

    @Test
    fun `Familiar walks to a distant ordered target in single-way`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3032, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        // 14 tiles east (beyond the ~10-tile approach range, so the interact phase must walk it
        // before combat starts), unobstructed: the familiar must close the distance to attack.
        val target = createNPC("guard_falador", Tile(3046, 3352))
        val startX = familiar.tile.x

        owner.commandFamiliarAttack(target)
        tick(6)

        assertTrue(familiar.tile.x > startX)
    }

    @Test
    fun `Player cannot attack a monster its familiar is fighting in single-way`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        val target = createNPC("guard_falador", Tile(3034, 3352))
        // The familiar is already fighting the target (single-combat tracks one attacker per
        // target), so the owner - a separate attacker - can't also attack it in a single-way zone.
        target.attacker = familiar
        target.start("under_attack", 8)

        assertFalse(Target.attackable(owner, target, message = false))
    }

    @Test
    fun `Familiar does not assist owner in single-way combat`() {
        val owner = createPlayer(Tile(3032, 3352))
        owner.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        // Single-way zone (no in_multi_combat flag): the familiar can still be used, but combat
        // familiars only fight in multi-combat zones, so it must not join the owner's fight.
        val target = createNPC("guard_falador", Tile(3032, 3351))

        owner.npcOption(target, "Attack")
        tick(8)

        assertFalse(familiar.mode is CombatMovement)
    }

    @Test
    fun `Familiar approaches a commanded target beyond its aggro range`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3032, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        // 12 tiles away: beyond the familiar's retreat_range(8) + attackRange(1) = 9 aggro range.
        val target = createNPC("guard_falador", Tile(3044, 3352))
        familiar.mode = CombatMovement(familiar, target)
        val startX = familiar.tile.x
        tick(3)
        // The familiar isn't bound by the aggro leash, so it walks towards the target instead of
        // freezing (a spawn/owner-leashed NPC would drop to EmptyMode here).
        assertTrue(familiar.mode is CombatMovement)
        assertTrue(familiar.tile.x > startX)
    }

    @Test
    fun `Familiar auto-assists owner against multi-combat npc`() {
        val owner = createPlayer(Tile(3032, 3352))
        owner.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        owner["in_multi_combat"] = true
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        val target = createNPC("guard_falador", Tile(3032, 3351))
        target["in_multi_combat"] = true

        owner.npcOption(target, "Attack")
        tick(8)

        assertTrue(familiar.mode is CombatMovement)
        assertEquals(target, (familiar.mode as CombatMovement).target)
    }

    @Test
    fun `Familiar keeps its target when the owner switches targets`() {
        val owner = createPlayer(Tile(3032, 3352))
        owner["in_multi_combat"] = true
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        val first = createNPC("guard_falador", Tile(3034, 3352))
        first["in_multi_combat"] = true
        val second = createNPC("guard_falador", Tile(3032, 3351))
        second["in_multi_combat"] = true
        familiar.mode = CombatMovement(familiar, first)

        // The owner now attacks a different enemy; the familiar must stay on its original target.
        owner.assistFamiliar(second)
        tick()

        assertTrue(familiar.mode is CombatMovement)
        assertEquals(first, (familiar.mode as CombatMovement).target)
    }

    @Test
    fun `Familiar re-acquires the owner's target after being freed mid-fight`() {
        val owner = createPlayer(Tile(3032, 3352))
        owner["in_multi_combat"] = true
        owner.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        val familiar = createNPC("spirit_wolf_familiar", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        familiar["spawn_tile"] = familiar.tile
        owner.follower = familiar
        val target = createNPC("guard_falador", Tile(3032, 3351))
        target["in_multi_combat"] = true
        val dummy = createNPC("guard_falador", Tile(3040, 3352))
        dummy["in_multi_combat"] = true
        // Busy elsewhere when the owner starts the fight, so the initial combatStart assist is missed.
        familiar.mode = CombatMovement(familiar, dummy)
        owner.npcOption(target, "Attack")
        tick(2)

        // Free the familiar; while the owner keeps attacking, it should re-acquire the owner's target.
        familiar.mode = EmptyMode
        tick(6)

        assertTrue(familiar.mode is CombatMovement)
        assertEquals(target, (familiar.mode as CombatMovement).target)
    }

    @Test
    fun `Player cannot attack their own familiar`() {
        val owner = createPlayer(Tile(3032, 3352))
        // PvP combat variant carries the "Attack" option, so ownership is the only blocker here.
        val familiar = createNPC("spirit_wolf_familiar_combat", Tile(3033, 3352))
        familiar["owner_index"] = owner.index
        assertFalse(Target.attackable(owner, familiar, message = false))
    }

    @Test
    fun `Idle familiar resumes following its owner after combat`() {
        val owner = createPlayer(Tile(3032, 3352))
        val familiar = createNPC("spirit_wolf_familiar", Tile(3034, 3352))
        familiar["owner_index"] = owner.index
        owner.follower = familiar
        // Combat ending leaves the familiar idle; it should fall back to following, not stand still.
        familiar.mode = EmptyMode
        tick()
        assertTrue(familiar.mode is Follow)
    }

    companion object {
        private const val MAX_EXP = 14000000.0
    }
}
