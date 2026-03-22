package content.area.misthalin.zanaris

import FakeRandom
import WorldTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class ZanarisShortcutTest : WorldTest() {

    private val data = listOf(
        Pair(Tile(2400, 4404), Direction.SOUTH),
        Pair(Tile(2400, 4402), Direction.NORTH),
        Pair(Tile(2415, 4403), Direction.SOUTH),
        Pair(Tile(2415, 4401), Direction.NORTH),
        Pair(Tile(2408, 4396), Direction.SOUTH),
        Pair(Tile(2408, 4394), Direction.NORTH),
    )

    @TestFactory
    fun `Shortcut success`() = data.map { (tile, dir) ->
        dynamicTest("$tile shortcut success $dir") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val player = createPlayer(tile)
            player.levels.set(Skill.Agility, 66)
            val wall = GameObjects.find(tile.add(dir), "zanaris_jutting_wall")
            player.interactObject(wall, "Squeeze-past")
            tick(5)
            assertEquals(100, player.levels.get(Skill.Constitution))
            assertEquals(10.0, player.experience.get(Skill.Agility))
            assertEquals(tile.add(dir).add(dir), player.tile)
        }
    }

    @TestFactory
    fun `Shortcut failure`() = data.map { (tile, dir) ->
        dynamicTest("$tile shortcut failure $dir") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = until
            })
            val player = createPlayer(tile)
            player.levels.set(Skill.Agility, 66)
            val wall = GameObjects.find(tile.add(dir), "zanaris_jutting_wall")
            player.interactObject(wall, "Squeeze-past")
            tick(6)
            assertEquals(60, player.levels.get(Skill.Constitution))
            assertEquals(6.0, player.experience.get(Skill.Agility))
            assertEquals(tile.add(dir).add(dir), player.tile)
        }
    }

    @TestFactory
    fun `Shortcut without level`() = data.map { (tile, dir) ->
        dynamicTest("$tile shortcut without level $dir") {
            val player = createPlayer(tile)
            player.levels.set(Skill.Agility, 45)
            val wall = GameObjects.find(tile.add(dir), "zanaris_jutting_wall")
            player.interactObject(wall, "Squeeze-past")
            tick(5)
            assertEquals(100, player.levels.get(Skill.Constitution))
            assertEquals(0.0, player.experience.get(Skill.Agility))
            assertEquals(tile, player.tile)
        }
    }
}
