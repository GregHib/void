package content.entity.player.command

import WorldTest
import content.social.report.isMuted
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Player-targeting commands resolve display names typed with underscores in place of the spaces
 * the console's input filter can't type.
 */
class SpacedNameCommandsTest : WorldTest() {

    @Test
    fun `Tele_to resolves a spaced display name typed with underscores`() {
        val admin = createPlayer(Tile(3222, 3222), name = "tele admin")
        admin.rights = PlayerRights.Admin
        val target = createPlayer(Tile(3164, 3484), name = "eddies fan")

        runTest { Commands.call(admin, "tele_to eddies_fan") }
        tick()

        assertEquals(target.tile, admin.tile)
    }

    @Test
    fun `Mute resolves a spaced display name typed with underscores`() {
        val mod = createPlayer(name = "mute mod")
        mod.rights = PlayerRights.Mod
        val target = createPlayer(name = "noisy neighbour")

        runTest { Commands.call(mod, "mute noisy_neighbour 1") }
        tick()

        assertTrue(target.isMuted)
    }

    @Test
    fun `A full underscore name beats a prefix match on a longer name`() {
        val admin = createPlayer(Tile(3222, 3222), name = "prefix admin")
        admin.rights = PlayerRights.Admin
        createPlayer(Tile(3200, 3200), name = "eddies fanatic")
        val exact = createPlayer(Tile(3164, 3484), name = "eddies fan")

        runTest { Commands.call(admin, "tele_to eddies_fan") }
        tick()

        assertEquals(exact.tile, admin.tile)
    }
}
