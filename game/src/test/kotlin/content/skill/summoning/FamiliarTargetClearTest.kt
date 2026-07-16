package content.skill.summoning

import WorldTest
import content.entity.combat.attackers
import content.entity.combat.hit.damage
import content.entity.combat.target
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull

/**
 * A familiar's combat target must clear when the target dies. Combat only emits combatStop for
 * fights whose first swing landed - a titan mid-retaliation (target set, no swing yet) whose
 * target dies gets no stop event, and the stale target would let the titan's called special
 * silently re-engage the same npc after it respawns.
 */
class FamiliarTargetClearTest : WorldTest() {

    @Test
    fun `The titan's target clears when the target dies before its first swing`() {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("iron_titan_familiar"), restart = false)
        tick(2) // let the summon queue assign the follower
        val familiar = player.follower!!
        val rat = createNPC("rat", familiar.tile.addY(4))

        // Mirror Combat.retaliate: the titan is engaged (mode + target) but hasn't swung yet.
        familiar.mode = CombatMovement(familiar, rat)
        familiar.target = rat
        rat.attackers.add(familiar)

        rat.damage(1000, source = player)
        tick(10) // the rat dies and its death queue runs

        assertNull(familiar.target, "the dead target must not stick to the familiar")
    }
}
