package content.skill.slayer

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SlayerTaskTest : WorldTest() {

    @Test
    fun `Obtain a slayer task`() {
        val player = createPlayer(tile = Tile(2930, 3536))
        val turael = createNPC("turael", Tile(2931, 3536))

        player.npcOption(turael, "Get-task")
        tickIf { player.dialogue == null }

        assertEquals("turael", player.slayerMaster)
        assertNotEquals("nothing", player.slayerTask)
        assertTrue(player.slayerTaskRemaining > 0)
    }

    @Test
    fun `Complete a slayer task`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(tile = Tile(3231, 3298))
        player.levels.set(Skill.Attack, 99)
        player.levels.set(Skill.Strength, 99)
        player.levels.set(Skill.Defence, 99)
        player.levels.set(Skill.Constitution, 990)
        player.slayerMaster = "mazchna"
        player.slayerTask = "birds"
        player.slayerTaskRemaining = 1

        val chicken = createNPC("chicken", player.tile.addY(1))
        player.npcOption(chicken, "Attack")
        tickIf { chicken.levels.get(Skill.Constitution) > 0 }
        tick(5) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(1, player.slayerStreak)
        assertEquals(1, player.slayerPoints)
    }

    @Test
    fun `10 task streak gives bonus points`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(tile = Tile(3231, 3298))
        player.levels.set(Skill.Attack, 99)
        player.levels.set(Skill.Strength, 99)
        player.levels.set(Skill.Defence, 99)
        player.levels.set(Skill.Constitution, 990)
        player.slayerMaster = "mazchna"
        player.slayerTask = "birds"
        player.slayerStreak = 29
        player.slayerTaskRemaining = 1

        val chicken = createNPC("chicken", player.tile.addY(1))
        player.npcOption(chicken, "Attack")
        tickIf { chicken.levels.get(Skill.Constitution) > 0 }
        tick(5) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(30, player.slayerStreak)
        assertEquals(5, player.slayerPoints)
    }

    @Test
    fun `50 task streak gives bonus points`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(tile = Tile(3231, 3298))
        player.levels.set(Skill.Attack, 99)
        player.levels.set(Skill.Strength, 99)
        player.levels.set(Skill.Defence, 99)
        player.levels.set(Skill.Constitution, 990)
        player.slayerMaster = "mazchna"
        player.slayerTask = "birds"
        player.slayerStreak = 149
        player.slayerTaskRemaining = 1

        val chicken = createNPC("chicken", player.tile.addY(1))
        player.npcOption(chicken, "Attack")
        tickIf { chicken.levels.get(Skill.Constitution) > 0 }
        tick(5) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(150, player.slayerStreak)
        assertEquals(15, player.slayerPoints)
    }
}