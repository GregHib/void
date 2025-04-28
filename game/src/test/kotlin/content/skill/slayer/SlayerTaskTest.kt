package content.skill.slayer

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
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

    @TestFactory
    fun `Complete a slayer task`() = listOf(
        "birds" to "chicken",
        "goblins" to "goblin_staff_green",
        "monkeys" to "monkey_brown",
        "rats" to "giant_rat",
        "spiders" to "giant_spider",
        "bats" to "bat",
        "cows" to "cow_calf",
        "dwarves" to "dwarf",
        "minotaurs" to "minotaur",
        "scorpions" to "scorpion",
//        "zombies" to "zombie",
//        "bears" to "",
//        "ghosts" to "",
//        "dogs" to "",
//        "kalphites" to "",
        "skeletons" to "skeleton_sword_shield",
//        "icefiends" to "",
        "wolves" to "white_wolf",
//        "crawling_hands" to "",
//        "cave_bugs" to "",
//        "cave_crawlers" to "",
//        "banshees" to "",
//        "cave_slime" to "",
//        "lizards" to "",
    ).map { (task, npc) ->
        dynamicTest("Complete a $task slayer task") {
            setRandom(object : FakeRandom() {
                override fun nextBits(bitCount: Int): Int = bitCount
            })
            val player = createPlayer(tile = Tile(3231, 3298))
            player.levels.set(Skill.Attack, 99)
            player.levels.set(Skill.Strength, 99)
            player.levels.set(Skill.Defence, 99)
            player.levels.set(Skill.Constitution, 990)
            player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
            player.slayerMaster = "mazchna"
            player.slayerTask = task
            player.slayerTaskRemaining = 1

            val chicken = createNPC(npc, player.tile.addY(1))
            player.npcOption(chicken, "Attack")
            tickIf { chicken.levels.get(Skill.Constitution) > 0 }
            tick(5) // Npc death

            assertEquals("mazchna", player.slayerMaster)
            assertEquals("nothing", player.slayerTask)
            assertEquals(0, player.slayerTaskRemaining)
            assertEquals(1, player.slayerStreak)
            assertEquals(1, player.slayerPoints)
            assertTrue(player.experience.get(Skill.Slayer) > 0)
        }
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