package content.area.kandarin.yanille

import FakeRandom
import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class YanilleAgilityDungeonTest : WorldTest() {

    @Test
    fun `Swing south across yanille dungeon monkey bars`() {
        val player = createPlayer(Tile(2598, 9494))
        player.levels.set(Skill.Agility, 57)
        val monkeyBars = GameObjects.find(Tile(2598, 9494), "yanille_dungeon_monkey_bars")

        player.objectOption(monkeyBars, "Swing across")
        tick(12)

        assertEquals(Tile(2599, 9489), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
        assertTrue(player["monkey_business_task", false])
    }

    @Test
    fun `Swing north across yanille dungeon monkey bars`() {
        val player = createPlayer(Tile(2599, 9489))
        player.levels.set(Skill.Agility, 57)
        val monkeyBars = GameObjects.find(Tile(2599, 9489), "yanille_dungeon_monkey_bars")

        player.objectOption(monkeyBars, "Swing across")
        tick(12)

        assertEquals(Tile(2598, 9494), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't swing across yanille dungeon monkey bars without level`() {
        val player = createPlayer(Tile(2598, 9494))
        player.levels.set(Skill.Agility, 56)
        val monkeyBars = GameObjects.find(Tile(2598, 9494), "yanille_dungeon_monkey_bars")

        player.objectOption(monkeyBars, "Swing across")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 57"))
    }

    @Test
    fun `Fail yanille dungeon monkey bars from north`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val player = createPlayer(Tile(2598, 9494))
        player.levels.set(Skill.Agility, 57)
        val monkeyBars = GameObjects.find(Tile(2598, 9494), "yanille_dungeon_monkey_bars")

        player.objectOption(monkeyBars, "Swing across")
        tick(15)

        assertEquals(Tile(2599, 9555), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.containsMessage("You miss the hand hold and fall to the level below."))
        assertTrue(!player["monkey_business_task", false])
    }

    @Test
    fun `Yanille dungeon monkey bars stop failing at level 74`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer(Tile(2598, 9494))
        player.levels.set(Skill.Agility, 74)
        val monkeyBars = GameObjects.find(Tile(2598, 9494), "yanille_dungeon_monkey_bars")

        player.objectOption(monkeyBars, "Swing across")
        tick(12)

        assertEquals(Tile(2599, 9489), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze west through yanille dungeon pipe`() {
        val player = createPlayer(Tile(2578, 9506))
        player.levels.set(Skill.Agility, 49)
        val pipe = GameObjects.find(Tile(2577, 9506), "yanille_dungeon_obstacle_pipe")

        player.objectOption(pipe, "Squeeze-through")
        tick(15)

        assertEquals(Tile(2572, 9506), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze east through yanille dungeon pipe`() {
        val player = createPlayer(Tile(2572, 9506))
        player.levels.set(Skill.Agility, 49)
        val pipe = GameObjects.find(Tile(2573, 9506), "yanille_dungeon_obstacle_pipe")

        player.objectOption(pipe, "Squeeze-through")
        tick(15)

        assertEquals(Tile(2578, 9506), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't squeeze through yanille dungeon pipe without level`() {
        val player = createPlayer(Tile(2578, 9506))
        player.levels.set(Skill.Agility, 48)
        val pipe = GameObjects.find(Tile(2577, 9506), "yanille_dungeon_obstacle_pipe")

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 49"))
    }

    @Test
    fun `Only one player can use yanille dungeon pipe at a time`() {
        val westPlayer = createPlayer(Tile(2578, 9506))
        val eastPlayer = createPlayer(Tile(2572, 9506))
        westPlayer.levels.set(Skill.Agility, 49)
        eastPlayer.levels.set(Skill.Agility, 49)
        val westPipe = GameObjects.find(Tile(2577, 9506), "yanille_dungeon_obstacle_pipe")
        val eastPipe = GameObjects.find(Tile(2573, 9506), "yanille_dungeon_obstacle_pipe")

        westPlayer.objectOption(westPipe, "Squeeze-through")
        tick(1)
        eastPlayer.objectOption(eastPipe, "Squeeze-through")
        tick(2)

        assertTrue(eastPlayer.containsMessage("The pipe is being used."))
    }

    @Test
    fun `Climb up yanille dungeon rubble`() {
        val player = createPlayer(Tile(2616, 9572))
        player.levels.set(Skill.Agility, 67)
        val rubble = GameObjects.find(Tile(2616, 9572), "yanille_dungeon_rubble")

        player.objectOption(rubble, "Climb-up")
        tick(4)

        assertEquals(Tile(2614, 9505), player.tile)
        assertTrue(player.containsMessage("You climb up the pile of rubble."))
    }

    @Test
    fun `Can't climb up yanille dungeon rubble without level`() {
        val player = createPlayer(Tile(2616, 9572))
        player.levels.set(Skill.Agility, 66)
        val rubble = GameObjects.find(Tile(2616, 9572), "yanille_dungeon_rubble")

        player.objectOption(rubble, "Climb-up")
        tick(2)

        assertEquals(Tile(2616, 9572), player.tile)
        assertTrue(player.containsMessage("You need an Agility level of 67"))
    }

    @Test
    fun `Climb down yanille dungeon rubble`() {
        val player = createPlayer(Tile(2615, 9504))
        player.levels.set(Skill.Agility, 67)
        val rubble = GameObjects.find(Tile(2615, 9504), "yanille_dungeon_rubble_2")

        player.objectOption(rubble, "Climb-down")
        tick(4)

        assertEquals(Tile(2616, 9571), player.tile)
        assertTrue(player.containsMessage("You climb down the pile of rubble..."))
    }

    @Test
    fun `Only one player can cross yanille dungeon balancing ledge at a time`() {
        val northPlayer = createPlayer(Tile(2580, 9520))
        val southPlayer = createPlayer(Tile(2580, 9518))
        northPlayer.levels.set(Skill.Agility, 40)
        southPlayer.levels.set(Skill.Agility, 40)
        val ledge = GameObjects.find(Tile(2580, 9519), "yanille_dungeon_balancing_ledge_2")

        northPlayer.objectOption(ledge, "Walk-across")
        tick(1)
        southPlayer.objectOption(ledge, "Walk-across")
        tick(2)

        assertTrue(southPlayer.containsMessage("The ledge is being used."))
    }

    @Test
    fun `Fail yanille dungeon balancing ledge deals twenty percent max constitution damage`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val player = createPlayer(Tile(2580, 9520))
        player.levels.set(Skill.Agility, 40)
        player.levels.set(Skill.Constitution, 75)
        val ledge = GameObjects.find(Tile(2580, 9519), "yanille_dungeon_balancing_ledge_2")

        player.objectOption(ledge, "Walk-across")
        tick(6)

        assertEquals(60, player.levels.get(Skill.Constitution))
        assertTrue(player.containsMessage("You lost your balance!"))
    }

    @Test
    fun `Yanille dungeon balancing ledge stops failing at level 64`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer(Tile(2580, 9520))
        player.levels.set(Skill.Agility, 64)
        val ledge = GameObjects.find(Tile(2580, 9519), "yanille_dungeon_balancing_ledge_2")

        player.objectOption(ledge, "Walk-across")
        tick(10)

        assertEquals(Tile(2580, 9512), player.tile)
        assertEquals(22.5, player.experience.get(Skill.Agility))
        assertTrue(!player.containsMessage("You lost your balance!"))
    }
}
