package content.area.wilderness.daemonheim

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DaemonheimRewardsTest : WorldTest() {

    @Test
    fun `Buy reward`() {
        val player = createPlayer(Tile(3444, 3698))
        player["dungeoneering_tokens"] = 2000
        player.levels.set(Skill.Crafting, 25)
        player.levels.set(Skill.Dungeoneering, 25)
        player.open("daemonheim_rewards")

        player.interfaceOption("daemonheim_rewards", "items", "Select", slot = 10)
        player.interfaceOption("daemonheim_rewards", "buy", "Buy")
        player.interfaceOption("daemonheim_rewards", "confirm", "Confirm")

        assertTrue(player.inventory.contains("gem_bag"))
        assertEquals(0, player["dungeoneering_tokens", 0])
    }

    @Test
    fun `Not enough tokens`() {
        val player = createPlayer(Tile(3444, 3698))
        player.open("daemonheim_rewards")

        player.interfaceOption("daemonheim_rewards", "items", "Select", slot = 10)
        player.interfaceOption("daemonheim_rewards", "buy", "Buy")
        player.interfaceOption("daemonheim_rewards", "confirm", "Confirm")

        assertFalse(player.inventory.contains("gem_bag"))
        assertEquals(0, player["dungeoneering_tokens", 0])
    }

    @Test
    fun `Buy experience`() {
        val player = createPlayer(Tile(3444, 3698))
        player["dungeoneering_tokens"] = 1000
        player.open("daemonheim_rewards")

        player.interfaceOption("daemonheim_rewards", "items", "Select", slot = 205)
        player.interfaceOption("daemonheim_rewards", "buy", "Buy")
        (player.suspension as Suspension.IntEntry).resume(500)

        assertEquals(500.0, player.experience.get(Skill.Dungeoneering))
        assertEquals(500, player["dungeoneering_tokens", 0])
    }
}
