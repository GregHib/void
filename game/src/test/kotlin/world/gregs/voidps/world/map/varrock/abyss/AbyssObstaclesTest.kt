package world.gregs.voidps.world.map.varrock.abyss

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.containsMessage
import world.gregs.voidps.world.script.objectOption
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AbyssObstaclesTest : WorldTest() {

    private lateinit var center: Area
    private lateinit var definitions: ObjectDefinitions

    @BeforeEach
    fun setup() {
        val areas: AreaDefinitions = get()
        definitions = get()
        center = areas["abyss_center"]
    }

    @Test
    fun `Mine rock obstacle`() {
        val player = createPlayer("player", Tile(3026, 4812))
        player["abyss_obstacles"] = 0
        player.levels.set(Skill.Mining, 99)
        player.inventory.add("bronze_pickaxe")

        val obj = objects[Tile(3026, 4813)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(17)

        println(player.tile)// 3042, 4819 - 3026 4812
        assertTrue(player.tile in center)
        assertEquals(25.0, player.experience.get(Skill.Mining))
    }

    @Test
    fun `Fail to mine rock obstacle`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val tile = Tile(3026, 4812)
        val player = createPlayer("player", tile)
        player["abyss_obstacles"] = 0
        player.inventory.add("bronze_pickaxe")

        val obj = objects[Tile(3026, 4813)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(8)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Mining))
        assertTrue(player.containsMessage("fail"))
    }

    @Test
    fun `Can't mine rock obstacle without pickaxe and level`() {
        val tile = Tile(3026, 4812)
        val player = createPlayer("player", tile)
        player.inventory.add("dragon_pickaxe")
        player["abyss_obstacles"] = 0

        val obj = objects[Tile(3026, 4813)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick()

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Mining))
        assertTrue(player.containsMessage("need a pickaxe"))
    }

    @Test
    fun `Chop tendril obstacle`() {
        val player = createPlayer("player", Tile(3017, 4822))
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("bronze_hatchet")

        val obj = objects[Tile(3018, 4821)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(16)

        assertTrue(player.tile in center)
        assertEquals(25.0, player.experience.get(Skill.Woodcutting))
    }

    @Test
    fun `Can't chop tendril obstacle without hatchet and level`() {
        val tile = Tile(3017, 4822)
        val player = createPlayer("player", tile)
        player.inventory.add("dragon_hatchet")

        val obj = objects[Tile(3018, 4821)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(6)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Woodcutting))
        assertTrue(player.containsMessage("need a hatchet"))
    }

    @Test
    fun `Fail to chop tendril obstacle`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val tile = Tile(3017, 4822)
        val player = createPlayer("player", tile)
        player.inventory.add("bronze_hatchet")

        val obj = objects[Tile(3018, 4821)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(7)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Woodcutting))
        assertTrue(player.containsMessage("fail to cut"))
    }

    @Test
    fun `Burn down boil obstacle`() {
        val player = createPlayer("player", Tile(3017, 4835))
        player.levels.set(Skill.Firemaking, 99)
        player.inventory.add("tinderbox")

        val obj = objects[Tile(3018, 4833)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(21)

        assertTrue(player.tile in center)
        assertEquals(25.0, player.experience.get(Skill.Firemaking))
    }

    @Test
    fun `Can't burn down boil obstacle without tinderbox`() {
        val tile = Tile(3017, 4835)
        val player = createPlayer("player", tile)
        player.levels.set(Skill.Firemaking, 99)

        val obj = objects[Tile(3018, 4833)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(4)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Firemaking))
        assertTrue(player.containsMessage("don't have a tinderbox"))
    }

    @Test
    fun `Fail to burn down boil obstacle`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val tile = Tile(3017, 4835)
        val player = createPlayer("player", tile)
        player.inventory.add("tinderbox")

        val obj = objects[Tile(3018, 4833)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(4)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Firemaking))
        assertTrue(player.containsMessage("fail to burn"))
    }

    @Test
    fun `Distract eyes obstacle`() {
        val player = createPlayer("player", Tile(3020, 4842))
        player.levels.set(Skill.Thieving, 99)

        val obj = objects[Tile(3021, 4842)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(16)

        assertTrue(player.tile in center)
        assertEquals(25.0, player.experience.get(Skill.Thieving))
    }

    @Test
    fun `Fail to distract eyes obstacle`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val tile = Tile(3020, 4842)
        val player = createPlayer("player", tile)
        player.levels.set(Skill.Thieving, 99)

        val obj = objects[Tile(3021, 4842)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(3)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertTrue(player.containsMessage("fail to distract"))
    }

    @Test
    fun `Squeeze through gap obstacle`() {
        val player = createPlayer("player", Tile(3030, 4851))
        player.levels.set(Skill.Agility, 99)

        val obj = objects[Tile(3028, 4849)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(16)

        assertTrue(player.tile in center)
        assertEquals(25.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to squeeze through gap obstacle`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until + 1
        })
        val tile = Tile(3030, 4851)
        val player = createPlayer("player", tile)
        player.levels.set(Skill.Agility, 99)

        val obj = objects[Tile(3028, 4849)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(16)

        assertEquals(tile, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.containsMessage("not agile enough"))
    }

    @Test
    fun `Enter passage obstacle`() {
        val player = createPlayer("player", Tile(3038, 4853))

        val obj = objects[Tile(3038, 4853)].first { it.id.startsWith("abyss_obstacle") }

        player.objectOption(obj, optionIndex = 0)
        tick(3)

        assertTrue(player.tile in center)
    }
}