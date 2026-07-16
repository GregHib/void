package content.entity.player.command

import WorldTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class GiveCommandTest : WorldTest() {

    @Test
    fun `Give resolves a spaced display name typed with underscores`() {
        val admin = createPlayer(name = "admin")
        admin.rights = PlayerRights.Admin
        val target = createPlayer(name = "harley gilpin")

        runTest { Commands.call(admin, "give harley_gilpin coins 5") }
        tick()

        assertEquals(5, target.inventory.count("coins"))
    }

    @Test
    fun `Give tells the sender when no player matches`() {
        val admin = createPlayer(name = "admin2")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "give nobody_here coins 5") }
        tick()

        assertEquals(0, admin.inventory.count("coins"))
    }
}
