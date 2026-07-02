package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.underAttack
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.setRandom

/**
 * An npc away from its spawn (beyond its aggro/leash range) must still retaliate against a familiar
 * attacking it, rather than de-aggroing and walking back to its spawn without fighting.
 */
internal class FamiliarRetaliationLeashTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(FakeRandom())
    }

    @Test
    fun `Npc far from its spawn still fights back against a familiar`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("spirit_wolf_familiar"), restart = false)
        tick(2)
        val familiar = player.follower!!
        // Npc created at its spawn, then moved 15 tiles away (beyond its aggro range of 9) - as if it
        // had wandered - before the familiar engages it there.
        val npc = createNPC("giant_rat", emptyTile)
        npc.tele(emptyTile.addX(15))
        player.tele(emptyTile.addX(13))
        tick()

        player.commandFamiliarAttack(npc)
        tickIf(25) { !familiar.underAttack }

        assertTrue(npc.underAttack, "familiar attacked the npc")
        assertTrue(familiar.underAttack, "npc retaliated despite being away from its spawn")
    }
}
