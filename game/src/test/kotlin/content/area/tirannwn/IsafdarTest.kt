package content.area.tirannwn

import FakeRandom
import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class IsafdarTest : WorldTest() {

    @Test
    fun `Squeeze through dense forest`() {
        val player = createPlayer(Tile(2188, 3167))
        player.levels.set(Skill.Agility, 56)
        val forest = GameObjects.find(Tile(2187, 3169), "dense_forest_hard")

        player.objectOption(forest, "Enter")
        tick(8)

        assertEquals(Tile(2188, 3171), player.tile)
    }

    @Test
    fun `Entering dense forest from an offset tile lines up with the passage first`() {
        val player = createPlayer(Tile(2230, 3150))
        val forest = GameObjects.find(Tile(2232, 3148), "dense_forest")

        player.objectOption(forest, "Enter")
        tick(12)

        assertEquals(Tile(2234, 3149), player.tile)
    }

    @Test
    fun `Entering dense forest from the east walks around its footprint first`() {
        val player = createPlayer(Tile(2274, 3193))
        val forest = GameObjects.find(Tile(2272, 3191), "dense_forest")

        player.objectOption(forest, "Enter")
        tick(12)

        assertEquals(Tile(2271, 3192), player.tile)
    }

    @Test
    fun `Can't enter dense forest without agility level`() {
        val player = createPlayer(Tile(2188, 3167))
        player.levels.set(Skill.Agility, 55)
        val forest = GameObjects.find(Tile(2187, 3169), "dense_forest_hard")

        player.objectOption(forest, "Enter")
        tick(4)

        assertTrue(player.containsMessage("You need an Agility level of 56"))
        assertEquals(Tile(2188, 3168), player.tile)
    }

    @Test
    fun `Cross isafdar log balance`() {
        val player = createPlayer(Tile(2202, 3237))
        player.levels.set(Skill.Agility, 45)
        val log = GameObjects.find(Tile(2201, 3237), "isafdar_log_balance")

        player.objectOption(log, "Cross")
        tick(12)

        assertEquals(Tile(2196, 3237), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Crossing the log never paths south around it`() {
        val near = GameObjects.find(Tile(2259, 3250), "isafdar_log_balance_2")
        val far = GameObjects.find(Tile(2263, 3250), "isafdar_log_balance_2")
        var count = 0
        for ((start, log) in listOf(
            Tile(2255, 3250) to near,
            Tile(2255, 3250) to far,
            Tile(2258, 3250) to near,
            Tile(2258, 3251) to near,
            Tile(2256, 3251) to near,
            Tile(2250, 3250) to near,
            Tile(2252, 3252) to near,
        )) {
            val player = createPlayer(start, "log_crosser_${count++}")
            player.running = true
            player.levels.set(Skill.Agility, 45)
            tick(2)

            player.objectOption(log, "Cross")
            repeat(30) {
                tick()
                assertTrue(player.tile.y >= 3250, "player from $start stepped south of the log at ${player.tile}")
            }

            assertEquals(Tile(2264, 3250), player.tile)
            assertTrue(player.running, "run setting not restored after crossing from $start")
        }
    }

    @Test
    fun `Walking away while approaching the log cancels the crossing`() {
        val player = createPlayer(Tile(2250, 3250))
        player.running = true
        player.levels.set(Skill.Agility, 45)
        val log = GameObjects.find(Tile(2259, 3250), "isafdar_log_balance_2")

        player.objectOption(log, "Cross")
        tick(2)
        player.walk(Tile(2250, 3252))
        tick(20)

        assertEquals(Tile(2250, 3252), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't cross log balance without agility level`() {
        val player = createPlayer(Tile(2202, 3237))
        player.levels.set(Skill.Agility, 44)
        val log = GameObjects.find(Tile(2201, 3237), "isafdar_log_balance")

        player.objectOption(log, "Cross")
        tick(4)

        assertTrue(player.containsMessage("You need an Agility level of 45"))
    }

    @Test
    fun `Step over tripwire`() {
        val player = createPlayer(Tile(2215, 3153))
        player.levels.set(Skill.Agility, 1)
        val wire = GameObjects.find(Tile(2215, 3154), "tripwire")

        player.objectOption(wire, "Step-over")
        tick(8)

        assertEquals(Tile(2215, 3156), player.tile)
        assertTrue(player.containsMessage("You successfully step over the tripwire."))
    }

    @Test
    fun `Failing to step over tripwire deals damage`() {
        val player = createPlayer(Tile(2215, 3153))
        player.levels.set(Skill.Agility, 1)
        val wire = GameObjects.find(Tile(2215, 3154), "tripwire")
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = Int.MAX_VALUE
        })

        player.objectOption(wire, "Step-over")
        tick(8)

        assertEquals(Tile(2215, 3156), player.tile)
        assertTrue(player.containsMessage("You snag the trip wire as you step over it."))
        assertTrue(player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution))
    }

    @Test
    fun `Pass sticks trap`() {
        val player = createPlayer(Tile(2234, 3181))
        val sticks = GameObjects.find(Tile(2235, 3181), "isafdar_sticks_trap")

        player.objectOption(sticks, "Pass")
        tick(10)

        assertEquals(Tile(2237, 3181), player.tile)
        assertTrue(player.containsMessage("You manage to skillfully pass the trap."))
    }

    @Test
    fun `Failing the sticks trap springs the player off it`() {
        val player = createPlayer(Tile(2234, 3181))
        val sticks = GameObjects.find(Tile(2235, 3181), "isafdar_sticks_trap")
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = Int.MAX_VALUE
        })

        player.objectOption(sticks, "Pass")
        tick(10)

        assertEquals(Tile(2238, 3181), player.tile)
        assertTrue(player.containsMessage("You set off the trap as you pass."))
        assertTrue(player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution))
    }

    @Test
    fun `Walking over the sticks trap sets it off`() {
        val player = createPlayer(Tile(2234, 3181))

        player.walk(Tile(2238, 3181))
        tick(10)

        assertEquals(Tile(2238, 3181), player.tile)
        assertTrue(player.containsMessage("You set off the trap as you pass."))
        assertTrue(player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution))
    }

    @Test
    fun `Jump over leaf trap`() {
        val player = createPlayer(Tile(2274, 3172))
        player.levels.set(Skill.Agility, 99)
        val leaves = GameObjects.find(Tile(2274, 3173), "isafdar_leaves_3")

        player.objectOption(leaves, "Jump")
        tick(8)

        assertEquals(Tile(2274, 3176), player.tile)
        assertTrue(player.containsMessage("You safely jump across."))
    }

    @Test
    fun `Walking onto a leaf trap drops the player into the pit`() {
        val player = createPlayer(Tile(2274, 3171))

        player.walk(Tile(2274, 3174))
        tickIf { player.tile.y < 9000 }
        tick(2)

        assertEquals(Tile(2336, 9656), player.tile)
        assertTrue(player.containsMessage("You fall through and onto some spikes."))
        assertTrue(player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution))
    }

    @Test
    fun `Climb out of a leaf trap pit`() {
        val player = createPlayer(Tile(2336, 9656))
        val rocks = GameObjects.find(Tile(2336, 9656), "isafdar_protruding_rocks")

        player.objectOption(rocks, "Climb")
        tick(4)

        assertEquals(Tile(2274, 3172), player.tile)
    }

    @Test
    fun `Climb elven overpass rocks`() {
        val player = createPlayer(Tile(2346, 3300))
        player.levels.set(Skill.Agility, 59)
        val rocks = GameObjects.find(Tile(2346, 3299), "elven_overpass_rocks_up")

        player.objectOption(rocks, "Climb")
        tick(8)

        assertEquals(Tile(2344, 3294), player.tile)
    }

    @Test
    fun `Can't climb elven overpass without agility level`() {
        val player = createPlayer(Tile(2346, 3300))
        player.levels.set(Skill.Agility, 58)
        val rocks = GameObjects.find(Tile(2346, 3299), "elven_overpass_rocks_up")

        player.objectOption(rocks, "Climb")
        tick(4)

        assertTrue(player.containsMessage("You need an Agility level of 59"))
    }

    @Test
    fun `Pass through the lletya tree entrance after roving elves`() {
        val player = createPlayer(Tile(2304, 3191))
        player["roving_elves"] = "completed"
        val tree = GameObjects.find(Tile(2305, 3191), "lletya_tree_entrance")

        player.objectOption(tree, "Pass")
        tick(8)

        assertEquals(Tile(2307, 3191), player.tile)
    }
}
