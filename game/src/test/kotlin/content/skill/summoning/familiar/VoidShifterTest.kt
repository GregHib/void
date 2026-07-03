package content.skill.summoning.familiar

import WorldTest
import content.skill.summoning.summonFamiliar
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * The Void shifter yanks its owner to the Void Knights' Outpost the moment their life points drop
 * below 10% of their maximum.
 */
class VoidShifterTest : WorldTest() {

    private val outpost = Tile(2659, 2658, 0)

    private fun summon(familiar: String): Player {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.experience.set(Skill.Constitution, 14_000_000.0) // level 99, max ~990 life points
        player.summonFamiliar(NPCDefinitions.get(familiar), restart = false)
        tick(2) // let the summon queue assign the follower
        return player
    }

    private fun Player.dropTo(lifePoints: Int) {
        levels.drain(Skill.Constitution, levels.get(Skill.Constitution) - lifePoints)
    }

    @Test
    fun `Void shifter teleports the owner to the outpost below 10% life points`() {
        val player = summon("void_shifter_familiar")

        player.dropTo(50) // below 10% of ~990
        tick(2) // let the teleport's movement apply

        assertEquals(outpost, player.tile)
    }

    @Test
    fun `Void shifter does not teleport while life points stay above 10%`() {
        val player = summon("void_shifter_familiar")

        player.dropTo(500) // still above 10%
        tick(2)

        assertNotEquals(outpost, player.tile)
    }

    @Test
    fun `The 10 percent threshold is measured in life points (level 10 = 100 life points)`() {
        // A default player is level 10 Constitution, which is 100 life points, so 10% is 10 life
        // points - proving the threshold scales with the life-point value, not the raw skill level.
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("void_shifter_familiar"), restart = false)
        tick(2)
        assertEquals(100, player.levels.getMax(Skill.Constitution))

        player.dropTo(15) // above the 10-life-point threshold
        tick(2)
        assertNotEquals(outpost, player.tile)

        player.dropTo(5) // below it
        tick(2)
        assertEquals(outpost, player.tile)
    }

    @Test
    fun `A different familiar does not teleport the owner`() {
        val player = summon("spirit_wolf_familiar")

        player.dropTo(50)
        tick(2)

        assertNotEquals(outpost, player.tile)
    }
}
