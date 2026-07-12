package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.underAttack
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

/**
 * A familiar fighting an npc outside a PvP area has no player-facing "Attack" option (that only
 * appears on its wilderness `_combat` form), but the npc must still be able to retaliate against it.
 */
internal class FamiliarRetaliationTest : WorldTest() {

    @BeforeEach
    fun setup() {
        // Deterministic rolls (nextBits = 0): the familiar always lands its swing and pathing's
        // shuffled() stays in-bounds.
        setRandom(FakeRandom())
    }

    @Test
    fun `Npc fights back against an attacking familiar in a non-pvp area`() {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("spirit_wolf_familiar"), restart = false)
        tick(2)
        val familiar = player.follower!!
        val npc = createNPC("giant_rat", player.tile.addX(3))

        player.commandFamiliarAttack(npc)
        tickIf(20) { !familiar.underAttack }

        assertTrue(npc.underAttack, "familiar attacked the npc")
        assertTrue(familiar.underAttack, "npc retaliated against the familiar")
    }
}
