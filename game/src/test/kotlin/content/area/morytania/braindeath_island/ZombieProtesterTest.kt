package content.area.morytania.braindeath_island

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.Leash
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Protesters heckle and chase whoever comes near, but only as far as their post. Follow has no
 * range limit of its own - it teleports after a target that outruns it or changes level - and
 * Hunting won't reconsider an npc that is already following, so protesters chase on a [Leash]
 * instead and lose interest the moment the player leaves their patch of the island.
 */
class ZombieProtesterTest : WorldTest() {

    @Test
    fun `Zombie protesters chase a nearby player`() {
        val player = createPlayer(Tile(2145, 5088))
        val protester = createNPC("zombie_protester", Tile(2145, 5087))

        tickIf(limit = 20) { protester.mode !is Leash }

        assertTrue(protester.mode is Leash, "a protester should chase a player who walks up to it")
    }

    @Test
    fun `Zombie protesters give up once dragged away from their post`() {
        val player = createPlayer(Tile(2145, 5088))
        val protester = createNPC("zombie_protester", Tile(2145, 5087))
        val post = protester.tile

        tickIf(limit = 20) { protester.mode !is Leash }
        assertTrue(protester.mode is Leash, "the protester should be chasing to begin with")

        // Lead it away across the island; past the leash it should break off
        player.tele(2160, 5070)
        tickIf(limit = 100) { protester.mode is Leash }

        assertFalse(protester.mode is Leash, "the protester should give up the chase")
        assertTrue(protester.tile.within(post, 10), "and stay on its own patch rather than teleporting after the player")

        // and then make its own way back to the protest
        tickIf(limit = 400) { !protester.tile.within(post, 8) }
        assertTrue(protester.tile.within(post, 8), "and return to its post")
    }

    @Test
    fun `Zombie protesters never leave their post`() {
        val player = createPlayer(Tile(2145, 5088))
        val protester = createNPC("zombie_protester", Tile(2145, 5087))
        val post = protester.tile

        tickIf(limit = 20) { protester.mode !is Leash }

        // Walk the length of the island a step at a time; the protester tails then drops off
        repeat(40) {
            player.tele(player.tile.addY(1))
            tick()
            assertTrue(protester.tile.within(post, 10), "a protester should never chase beyond its post")
        }
        assertFalse(protester.mode is Leash, "and should have lost interest long before the end")
    }
}
