package world.gregs.voidps.world.interact.entity.combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.npcOption
import kotlin.random.Random

internal class CombatMovementTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(Random)
    }

    @Test
    fun `Player intercepts moving target`() {
        val player = createPlayer("player", emptyTile)
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

    companion object {
        private const val MAX_EXP = 14000000.0
    }
}