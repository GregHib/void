package content.area.wilderness.daemonheim

import WorldTest
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.quest.instance
import content.skill.dungeoneering.dungeonMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.type.Tile

class DungeonRejoinTest : WorldTest() {

    @Test
    fun `Solo player logs back into their dungeon`() {
        val player = createPlayer(Tile(3449, 3725), "solo")
        start(player)
        val instance = player.instance()
        val dungeon = player.dungeonMap
        assertNotNull(instance)

        val relogged = relog(player)

        assertTrue(relogged.inDungeoneering)
        assertEquals(instance, relogged.instance())
        assertSame(dungeon, relogged.dungeonMap)
        assertEquals(relogged, relogged.dungeonLeader)
        assertEquals(listOf(relogged), relogged.dungeonMembers)
        assertTrue(relogged.index in dungeon!!.players)
    }

    @Test
    fun `Party member logs back into their party's dungeon`() {
        val leader = createPlayer(Tile(3449, 3725), "leader")
        val member = createPlayer(Tile(3449, 3726), "member")
        DungeoneeringParty.setLeader(leader)
        DungeoneeringParty.join(member, leader)
        start(leader)
        val instance = leader.instance()

        logout(member)
        tick(3)
        assertEquals(listOf(leader), leader.dungeonMembers)

        val relogged = relog(member, loggedOut = true)

        assertEquals(instance, relogged.instance())
        assertEquals(leader, relogged.dungeonLeader)
        assertEquals(listOf(leader, relogged), leader.dungeonMembers)
        assertEquals(listOf(leader, relogged), relogged.dungeonMembers)
    }

    @Test
    fun `Party leader logs back in as a member`() {
        val leader = createPlayer(Tile(3449, 3725), "leader")
        val member = createPlayer(Tile(3449, 3726), "member")
        DungeoneeringParty.setLeader(leader)
        DungeoneeringParty.join(member, leader)
        start(leader)

        logout(leader)
        tick(3)
        assertEquals(member, member.dungeonLeader)

        val relogged = relog(leader, loggedOut = true)

        assertEquals(member.instance(), relogged.instance())
        assertEquals(member, relogged.dungeonLeader)
        assertEquals(listOf(member, relogged), member.dungeonMembers)
    }

    @Test
    fun `Logging in after the dungeon has been freed leaves it`() {
        val player = createPlayer(Tile(3449, 3725), "late")
        start(player)
        val instance = player.instance()!!
        val tile = player.tile
        logout(player)
        tick(3)

        GameLoop.tick += 3001 + Instances.CLEANUP_TICKS
        Instances.cleanup()
        assertFalse(Instances.isInstance(instance))

        val relogged = createPlayer(tile, "late") { it["in_dungeoneering"] = true }
        tick(2)

        assertFalse(relogged.inDungeoneering)
        assertEquals(Tile(3460, 3721, 1), relogged.tile)
    }

    private fun start(player: Player) {
        player.rights = PlayerRights.Admin
        player.inventory.add("ring_of_kinship")
        runTest { Commands.call(player, "start_dungeon 1 small 1") }
        tick(3)
        assertTrue(player.inDungeoneering)
    }

    private fun logout(player: Player) {
        runTest { get<AccountManager>().logout(player, false) }
    }

    private fun relog(player: Player, loggedOut: Boolean = false): Player {
        if (!loggedOut) {
            logout(player)
            tick(3)
        }
        // Persisted state is restored from the save
        val relogged = createPlayer(player.tile, player.accountName) { it["in_dungeoneering"] = true }
        tick()
        return relogged
    }
}
