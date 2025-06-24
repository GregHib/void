package content.skill.slayer

import FakeRandom
import WorldTest
import itemOnNpc
import npcOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlayerTaskTest : WorldTest() {

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
        "zombies" to "zombie",
        "bears" to "grizzly_bear",
        "ghosts" to "ghost",
        "dogs" to "jackal",
        "kalphites" to "kalphite_worker",
        "skeletons" to "skeleton_sword_shield",
        "icefiends" to "icefiend",
        "wolves" to "white_wolf",
        "crawling_hands" to "crawling_hand",
        "cave_bugs" to "cave_bug",
        "cave_crawlers" to "cave_crawler",
        "cave_slimes" to "cave_slime",
    ).map { (task, id) ->
        dynamicTest("Complete a $task slayer task") {
            setRandom(object : FakeRandom() {
                override fun nextBits(bitCount: Int): Int = bitCount
            })
            val player = createPlayer(Tile(3231, 3298))
            player.levels.set(Skill.Attack, 99)
            player.levels.set(Skill.Strength, 99)
            player.levels.set(Skill.Defence, 99)
            player.levels.set(Skill.Constitution, 990)
            player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
            player.equipment.set(EquipSlot.Hat.index, "earmuffs")
            player.slayerMaster = "mazchna"
            player.slayerTask = task
            player.slayerTaskRemaining = 1

            val npc = createNPC(id, player.tile.addY(1))
            player.npcOption(npc, "Attack")
            tickIf { npc.levels.get(Skill.Constitution) > 0 }
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
    fun `Banshees drain stats without earmuffs`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
            override fun nextInt(from: Int, until: Int): Int = until
        })
        val player = createPlayer(Tile(3231, 3298))
        player.levels.set(Skill.Attack, 99)
        player.levels.set(Skill.Strength, 99)
        player.levels.set(Skill.Defence, 99)
        player.levels.set(Skill.Prayer, 99)
        player.levels.set(Skill.Constitution, 990)
        player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
        player.slayerMaster = "mazchna"
        player.slayerTask = "banshees"
        player.slayerTaskRemaining = 1

        val banshee = createNPC("banshee", player.tile.addY(1))
        player.npcOption(banshee, "Attack")
        tick(5)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        tickIf { banshee.levels.get(Skill.Constitution) > 0 }
        tick(5) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(1, player.slayerStreak)
        assertEquals(1, player.slayerPoints)
        assertTrue(player.experience.get(Skill.Slayer) > 0)
        assertEquals(80, player.levels.get(Skill.Attack))
        assertEquals(80, player.levels.get(Skill.Strength))
        assertEquals(80, player.levels.get(Skill.Defence))
        assertEquals(90, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Complete a lizard slayer task`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(Tile(3231, 3298))
        player.levels.set(Skill.Attack, 99)
        player.levels.set(Skill.Strength, 99)
        player.levels.set(Skill.Defence, 99)
        player.levels.set(Skill.Constitution, 990)
        player.levels.set(Skill.Slayer, 22)
        player.inventory.add("ice_cooler")
        player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
        player.slayerMaster = "mazchna"
        player.slayerTask = "lizards"
        player.slayerTaskRemaining = 1

        val lizard = createNPC("lizard", player.tile.addY(1))
        player.npcOption(lizard, "Attack")
        tickIf { lizard.levels.get(Skill.Constitution) > 10 }
        tick(1)
        player.itemOnNpc(lizard, 0)
        tick(6) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(1, player.slayerStreak)
        assertEquals(1, player.slayerPoints)
        assertTrue(player.experience.get(Skill.Slayer) > 0)
    }

    @Test
    fun `10 task streak gives bonus points`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(Tile(3231, 3298))
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
        val player = createPlayer(Tile(3231, 3298))
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
