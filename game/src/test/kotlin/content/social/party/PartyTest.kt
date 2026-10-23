package content.social.party

import WorldTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.client.instruction.EnterName
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class PartyTest : WorldTest() {

    private suspend fun Player.command(text: String) {
        instructions.send(ExecuteCommand(text, automatic = false, tab = false))
        tick()
    }

    private fun Player.meta(): Int = this["party_meta", 0]

    private fun count(player: Player) = player.meta() and 0xF

    private suspend fun party(vararg players: Player) {
        val leader = players.first()
        for (member in players.drop(1)) {
            leader.command("party invite ${member.name.replace(' ', '_')}")
            member.command("party accept")
        }
        tick()
    }

    @Test
    fun `Invite and accept make a party both see`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        party(alice, bob)
        assertEquals(2, count(alice))
        assertEquals(2, count(bob))
        assertEquals(0, alice.meta() shr 8 and 0xF, "alice leads")
        assertEquals(1, bob.meta() shr 4 and 0xF, "bob is slot 1")
        val (hi, lo) = Party.base37("bob")
        assertEquals(hi, alice["party_1_name_hi", 0])
        assertEquals(lo, alice["party_1_name_lo", 0])
        assertEquals(Party.pack(0, 3201, 3200), alice["party_1_tile", 0])
    }

    @Test
    fun `Stats are shown to the others`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        party(alice, bob)
        bob.levels.drain(Skill.Constitution, 10)
        tick()
        val hp = alice["party_1_hp", 0]
        assertEquals(bob.levels.get(Skill.Constitution), hp shr 16)
        assertEquals(bob.levels.getMax(Skill.Constitution), hp and 0xFFFF)
    }

    @Test
    fun `A member in the wilderness has their tile hidden but not their stats`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        party(alice, bob)
        bob["in_wilderness"] = true
        tick()
        assertEquals(-1, alice["party_1_tile", 0])
        assertEquals(1, alice["party_1_flags", 0])
        assertNotEquals(0, alice["party_1_hp", 0])
    }

    @Test
    fun `A party holds eight`() = runTest {
        val players = (0 until 9).map { createPlayer(Tile(3200 + it, 3200), "member$it") }
        party(*players.toTypedArray())
        assertEquals(Party.MAX_MEMBERS, count(players[0]))
        assertEquals(0, players[8].meta())
    }

    @Test
    fun `The next member leads when the leader leaves`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        val carol = createPlayer(Tile(3202, 3200), "carol")
        party(alice, bob, carol)
        alice.command("party leave")
        assertEquals(0, alice.meta())
        assertEquals(2, count(bob))
        assertEquals(0, bob.meta() shr 8 and 0xF, "bob, now slot 0, leads")
        assertEquals(0, bob["party_2_name_hi", 0] or bob["party_2_name_lo", 0], "the old last slot is cleared")
    }

    @Test
    fun `A party of one is disbanded`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        party(alice, bob)
        bob.command("party leave")
        assertEquals(0, alice.meta())
        assertEquals(0, bob.meta())
    }

    @Test
    fun `The leader can kick`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        val carol = createPlayer(Tile(3202, 3200), "carol")
        party(alice, bob, carol)
        bob.command("party kick carol")
        assertEquals(3, count(alice), "only the leader can kick")
        alice.command("party kick carol")
        assertEquals(2, count(alice))
        assertEquals(0, carol.meta())
    }

    @Test
    fun `Invite without a name asks for one in the name box`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        alice.command("party invite")
        alice.instructions.send(EnterName("bob"))
        tick()
        assertEquals(Party.base37("alice").second, bob["party_invite_lo", 0])
    }

    @Test
    fun `The leader can hand the party over`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        val carol = createPlayer(Tile(3202, 3200), "carol")
        party(alice, bob, carol)
        bob.command("party leader carol")
        assertEquals(0, alice.meta() shr 8 and 0xF, "only the leader can")
        alice.command("party leader carol")
        assertEquals(2, alice.meta() shr 8 and 0xF, "carol, slot 2, leads")
        assertEquals(2, bob.meta() shr 8 and 0xF)
        carol.command("party kick alice")
        assertEquals(2, count(carol), "and the new leader can kick")
    }

    @Test
    fun `Pings reach every member and are limited`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        val outsider = createPlayer(Tile(3202, 3200), "outsider")
        party(alice, bob)
        bob.command("ping 3210 3205 0")
        assertEquals(Party.pack(0, 3210, 3205), alice["party_ping_tile", 0])
        val seq = alice["party_ping_seq", 0]
        assertEquals(1, seq ushr 24, "from bob's slot")
        assertEquals(seq, bob["party_ping_seq", 0])
        bob.command("ping 3500 3205 0")
        assertEquals(Party.pack(0, 3210, 3205), alice["party_ping_tile", 0], "too far away")
        outsider.command("ping 3202 3200 0")
        assertEquals(0, outsider["party_ping_seq", 0])
    }

    @Test
    fun `No party variables without a party`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        alice.command("party invite bob")
        assertEquals(0, alice.meta())
        assertEquals(Party.base37("alice").second, bob["party_invite_lo", 0], "bob sees the invite")
        bob.command("party decline")
        assertEquals(0, bob["party_invite_lo", 0])
        bob.command("party accept")
        assertEquals(0, bob.meta())
    }

    @Test
    fun `Logging out leaves the party`() = runTest {
        val alice = createPlayer(Tile(3200, 3200), "alice")
        val bob = createPlayer(Tile(3201, 3200), "bob")
        val carol = createPlayer(Tile(3202, 3200), "carol")
        party(alice, bob, carol)
        Despawn.player(carol)
        tick()
        assertEquals(2, count(alice))
    }
}
