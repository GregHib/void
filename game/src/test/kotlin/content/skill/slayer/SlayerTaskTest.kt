package content.skill.slayer

import FakeRandom
import WorldTest
import content.entity.death.NPCDeath
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
            player.levels.set(Skill.Slayer, 99)
            player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
            player.equipment.set(EquipSlot.Hat.index, "earmuffs")
            player["smoking_kills"] = "completed"
            player.slayerMaster = "mazchna"
            player.slayerTask = task
            player.slayerStreak = 4
            player.slayerTaskRemaining = 1

            val npc = createNPC(id, player.tile.addY(1))
            player.npcOption(npc, "Attack")
            tickIf { npc.levels.get(Skill.Constitution) > 0 }
            tick(5) // Npc death

            assertEquals("mazchna", player.slayerMaster)
            assertEquals("nothing", player.slayerTask)
            assertEquals(0, player.slayerTaskRemaining)
            assertEquals(5, player.slayerStreak)
            assertEquals(1, player.slayerPoints)
            assertTrue(player.experience.get(Skill.Slayer) > 0)
        }
    }

    @TestFactory
    fun `Vampyres count towards a vampyre task`() = listOf(
        "vampire",
        "vampyre_god_wars",
        "vampyre_juvinate_3",
        "vampyre_juvenile_3",
        "vampyre_juvenile_female",
    ).map { id ->
        dynamicTest("Kill $id on a vampyre task") {
            val player = createPlayer(Tile(3231, 3298))
            player.slayerMaster = "mazchna"
            player.slayerTask = "vampyres"
            player.slayerTaskRemaining = 2
            val npc = createNPC(id, player.tile.addY(1))

            scripts.filterIsInstance<NPCDeath>().first().slay(player, npc)

            assertEquals(1, player.slayerTaskRemaining)
            assertTrue(player.experience.get(Skill.Slayer) > 0)
        }
    }

    @TestFactory
    fun `Monsters without configured slayer experience count towards their task`() = listOf(
        Triple("lesser_demons", "lesser_demon", 79.0),
        Triple("lesser_demons", "lesser_demon_melzars_maze", 79.0),
        Triple("gorak", "gorak", 112.0),
        Triple("fire_giants", "fire_giant_2", 111.0),
        Triple("cave_horrors", "cave_horror_2", 55.0),
        Triple("cyclopes", "cyclopse_club", 100.0),
        Triple("hill_giants", "cyclops_god_wars", 110.0),
        Triple("spiritual_warriors", "spiritual_warrior_zamorak", 110.0),
        Triple("spiritual_mages", "spiritual_mage_zamorak", 75.0),
        Triple("warped_tortoises", "warped_tortoise", 87.0),
    ).map { (task, id, experience) ->
        dynamicTest("Kill $id on a $task task") {
            val player = createPlayer(Tile(3231, 3298))
            player.slayerMaster = "vannaka"
            player.slayerTask = task
            player.slayerTaskRemaining = 2
            val npc = createNPC(id, player.tile.addY(1))

            scripts.filterIsInstance<NPCDeath>().first().slay(player, npc)

            assertEquals(1, player.slayerTaskRemaining)
            assertEquals(experience, player.experience.get(Skill.Slayer))
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
        player.levels.set(Skill.Slayer, 15)
        player.equipment.set(EquipSlot.Weapon.index, "abyssal_whip")
        player["smoking_kills"] = "completed"
        player.slayerMaster = "mazchna"
        player.slayerTask = "banshees"
        player.slayerStreak = 4
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
        assertEquals(5, player.slayerStreak)
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
        player["smoking_kills"] = "completed"
        player.slayerMaster = "mazchna"
        player.slayerTask = "lizards"
        player.slayerStreak = 4
        player.slayerTaskRemaining = 1

        val lizard = createNPC("lizard", player.tile.addY(1))
        player.npcOption(lizard, "Attack")
        tickIf { lizard.levels.get(Skill.Constitution) > 10 }
        tick(1)
        player.itemOnNpc(lizard, 0)
        tick(7) // Npc death

        assertEquals("mazchna", player.slayerMaster)
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(5, player.slayerStreak)
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
        player["smoking_kills"] = "completed"
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
        player["smoking_kills"] = "completed"
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

    @Test
    fun `Chaeldar assigns a task without crashing on its jungle strykewyrm entry`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(Tile(3231, 3298))
        player.levels.set(Skill.Slayer, 99)

        // Chaeldar lists jungle_strykewyrm, which had no slayer_tasks entry and threw
        // "Row not found" while rolling, so the master gave no task at all.
        val (type, amount) = assignTask(player, "chaeldar")!!

        assertTrue(type.isNotEmpty())
        assertTrue(amount > 0)
        assertEquals("chaeldar", player.slayerMaster)
        assertEquals(type, player.slayerTask)
    }
}
